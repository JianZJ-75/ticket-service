package com.jianzj.ticket.service.frameworks.starter.convention.page;

/**
 * @Author JianZJ
 * @Date 2025/2/12 23:59
 */

import lombok.Data;

/**
 * 分页请求对象
 */
@Data
public class PageRequest {

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页显示数
     */
    private Long size = 10L;

}