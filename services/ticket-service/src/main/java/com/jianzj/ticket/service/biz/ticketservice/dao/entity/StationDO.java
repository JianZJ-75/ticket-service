package com.jianzj.ticket.service.biz.ticketservice.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.jianzj.ticket.service.frameworks.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author JianZJ
 * @Date 2025/2/21 20:33
 */

/**
 * 车站表实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
@TableName("t_station")
public class StationDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 车站编码
     */
    private String code;

    /**
     * 车站名称
     */
    private String name;

    /**
     * 拼音
     */
    private String spell;

    /**
     * 地区编号
     */
    private String region;

    /**
     * 地区名称
     */
    private String regionName;

}
