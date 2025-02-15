package com.jianzj.ticket.service.frameworks.starter.database.handler;

/**
 * @Author JianZJ
 * @Date 2025/2/16 3:25
 */

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.jianzj.ticket.service.frameworks.starter.distributedid.tools.SnowFlakeIdUtil;

/**
 * 自定义雪花算法生成器
 */
public class CustomSnowFlakeIdGenerator implements IdentifierGenerator {

    @Override
    public Number nextId(Object entity) {
        return SnowFlakeIdUtil.nextId();
    }
}