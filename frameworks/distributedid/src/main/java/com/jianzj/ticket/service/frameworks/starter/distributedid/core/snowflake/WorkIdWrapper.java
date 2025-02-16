package com.jianzj.ticket.service.frameworks.starter.distributedid.core.snowflake;

/**
 * @Author JianZJ
 * @Date 2025/2/17 2:43
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * work id包装器
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkIdWrapper {

    /**
     * 工作ID
     */
    private Long workId;

    /**
     * 数据中心ID
     */
    private Long dataCenterId;

}