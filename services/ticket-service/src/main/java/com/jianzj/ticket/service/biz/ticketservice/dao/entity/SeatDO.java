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
 * 座位表实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
@TableName("t_seat")
public class SeatDO extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 列车id
     */
    private Long trainId;

    /**
     * 车厢号
     */
    private String carriageNumber;

    /**
     * 座位号
     */
    private String seatNumber;

    /**
     * 座位类型
     */
    private Integer seatType;

    /**
     * 起始站
     */
    private String startStation;

    /**
     * 终点站
     */
    private String endStation;

    /**
     * 座位状态
     */
    private Integer seatStatus;

    /**
     * 车票价格
     */
    private Integer price;

}
