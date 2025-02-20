package com.jianzj.ticket.service.biz.gatewayservice.config;

/**
 * @Author JianZJ
 * @Date 2025/2/20 19:53
 */

import lombok.Data;

import java.util.List;

/**
 * 过滤器配置
 */
@Data
public class FilterConfig {

    /**
     * 黑名单前置路径
     */
    private List<String> blackPathPre;

}
