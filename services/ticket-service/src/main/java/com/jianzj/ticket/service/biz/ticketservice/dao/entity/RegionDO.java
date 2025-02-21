package com.jianzj.ticket.service.biz.ticketservice.dao.entity;

/**
 * @Author JianZJ
 * @Date 2025/2/21 20:32
 */

import com.baomidou.mybatisplus.annotation.TableName;
import com.jianzj.ticket.service.frameworks.starter.database.base.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 地区表实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
@TableName("t_region")
public class RegionDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 地区名称
     */
    private String name;

    /**
     * 地区全名
     */
    private String fullName;

    /**
     * 地区编码
     */
    private String code;

    /**
     * 地区首字母
     */
    private String initial;

    /**
     * 拼音
     */
    private String spell;

    /**
     * 热门标识
     */
    private Integer popularFlag;

}
