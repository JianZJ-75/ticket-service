package com.jianzj.ticket.service.frameworks.starter.designpattern.strategy;

import com.jianzj.ticket.service.frameworks.starter.base.ApplicationContextHolder;
import com.jianzj.ticket.service.frameworks.starter.base.init.ApplicationInitializingEvent;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ServiceException;
import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

/**
 * @Author JianZJ
 * @Date 2025/2/13 3:28
 */

/**
 * 执行策略选择器
 */
public class AbstractStrategyChoose implements ApplicationListener<ApplicationInitializingEvent> {

    /**
     * 执行策略集合
     */
    private final Map<String, AbstractExecuteStrategy> abstractExecuteStrategyMap = new HashMap<>();

    /**
     * 根据mark查询具体策略
     * @param mark
     * @param isMatch 是否为范匹配
     * @return
     */
    public AbstractExecuteStrategy choose(String mark, Boolean isMatch) {
        if (isMatch != null && isMatch) {
            return abstractExecuteStrategyMap.values().stream()
                    .filter(each -> StringUtils.hasText(each.patternMatchMark()))
                    .filter(each -> Pattern.compile(each.patternMatchMark()).matcher(mark).matches())
                    .findFirst()
                    .orElseThrow(() -> new ServiceException(String.format("[%s] strategy is undefined.", mark)));
        }
        return Optional.ofNullable(abstractExecuteStrategyMap.get(mark))
                .orElseThrow(() -> new ServiceException(String.format("[%s] strategy is undefined.", mark)));
    }

    /**
     * 选择策略并执行
     * @param mark
     * @param request
     * @param <REQUEST>
     */
    public <REQUEST> void chooseAndExecute(String mark, REQUEST request) {
        AbstractExecuteStrategy executeStrategy = choose(mark, null);
        executeStrategy.execute(request);
    }

    /**
     * 按需选择策略并执行
     * @param mark
     * @param isMatch
     * @param request
     * @param <REQUEST>
     */
    public <REQUEST> void chooseAndExecute(String mark, Boolean isMatch, REQUEST request) {
        AbstractExecuteStrategy executeStrategy = choose(mark, isMatch);
        executeStrategy.execute(request);
    }

    /**
     * 选择策略执行并返回
     * @param mark
     * @param request
     * @return
     * @param <REQUEST>
     * @param <RESPONSE>
     */
    public <REQUEST, RESPONSE> RESPONSE chooseAndExecuteResp(String mark, REQUEST request) {
        AbstractExecuteStrategy executeStrategy = choose(mark, null);
        return (RESPONSE) executeStrategy.executeResp(request);
    }

    /**
     * 按需选择策略执行并返回
     * @param mark
     * @param isMatch
     * @param request
     * @return
     * @param <REQUEST>
     * @param <RESPONSE>
     */
    public <REQUEST, RESPONSE> RESPONSE chooseAndExecuteResp(String mark, Boolean isMatch, REQUEST request) {
        AbstractExecuteStrategy executeStrategy = choose(mark, isMatch);
        return (RESPONSE) executeStrategy.executeResp(request);
    }

    @Override
    public void onApplicationEvent(ApplicationInitializingEvent event) {
        Map<String, AbstractExecuteStrategy> actual = ApplicationContextHolder.getBeansOfType(AbstractExecuteStrategy.class);
        actual.forEach((beanName, bean) -> {
            AbstractExecuteStrategy beanExist = abstractExecuteStrategyMap.get(bean.mark());
            if (beanExist != null) {
                throw new ServiceException(String.format("[%s] Duplicate execution policy", bean.mark()));
            }
            abstractExecuteStrategyMap.put(bean.mark(), bean);
        });
    }
}