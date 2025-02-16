package com.jianzj.ticket.service.frameworks.starter.distributedid.core.snowflake;

/**
 * @Author JianZJ
 * @Date 2025/2/17 2:37
 */

import cn.hutool.core.date.SystemClock;
import com.jianzj.ticket.service.frameworks.starter.distributedid.tools.SnowflakeIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

/**
 * 获取work id模板抽象类
 */
@Slf4j
public abstract class AbstractWorkIdChooseTemplate {

    /**
     * 是否使用 {@link SystemClock} 获取当前时间戳
     */
    @Value("${framework.distributed.id.snowflake.is-use-system-clock:false}")
    private boolean isUseSystemClock;

    /**
     * 根据自定义策略获取 WorkId 生成器
     *
     * @return
     */
    protected abstract WorkIdWrapper chooseWorkId();

    /**
     * 选择 WorkId 并初始化雪花
     */
    public void chooseAndInit() {
        // 模板方法模式: 通过抽象方法获取 WorkId 包装器创建雪花算法
        WorkIdWrapper workIdWrapper = chooseWorkId();
        long workId = workIdWrapper.getWorkId();
        long dataCenterId = workIdWrapper.getDataCenterId();
        Snowflake snowflake = new Snowflake(workId, dataCenterId, isUseSystemClock);
        log.info("Snowflake type: {}, workId: {}, dataCenterId: {}", this.getClass().getSimpleName(), workId, dataCenterId);
        SnowflakeIdUtil.initSnowflake(snowflake);
    }

}