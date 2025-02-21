package com.jianzj.ticket.service.frameworks.starter.designpattern.chain;

import com.jianzj.ticket.service.frameworks.starter.base.ApplicationContextHolder;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ServiceException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author JianZJ
 * @Date 2025/2/13 2:50
 */

/**
 * 抽象责任链上下文
 * @param <T>
 */
public final class AbstractChainContext<T> implements CommandLineRunner {

    private final Map<String, List<AbstractChainHandler>> abstractChainHolderContainer = new HashMap<>();

    /**
     * 执行责任链
     * @param mark
     * @param requestParam
     */
    public void handler(String mark, T requestParam) {
        List<AbstractChainHandler> abstractChainHolders = abstractChainHolderContainer.get(mark);
        if (CollectionUtils.isEmpty(abstractChainHolders)) {
            throw new ServiceException(String.format("[%s] Chain of Responsibility ID is undefined.", mark));
        }
        abstractChainHolders.forEach(each -> each.handle(requestParam));
    }

    @Override
    public void run(String... args) throws Exception {
        // 获取所有责任链的bean
        Map<String, AbstractChainHandler> chainMap = ApplicationContextHolder.getBeansOfType(AbstractChainHandler.class);
        // 处理bean
        chainMap.forEach((beanName, bean) -> {
            // 根据mark获取对应链路
            List<AbstractChainHandler> abstractChainHolders = abstractChainHolderContainer.get(bean.mark());
            if (CollectionUtils.isEmpty(abstractChainHolders)) {
                abstractChainHolders = new ArrayList<>();
            }
            abstractChainHolders.add(bean);
            // 根据ordered将责任链排序
            List<AbstractChainHandler> actualAbstractChainHolders = abstractChainHolders.stream()
                    .sorted(Comparator.comparing(Ordered::getOrder))
                    .collect(Collectors.toList());
            abstractChainHolderContainer.put(bean.mark(), actualAbstractChainHolders);
        });
    }
}