package com.jianzj.ticket.service.biz.ticketservice.dto.resp;

/**
 * @Author JianZJ
 * @Date 2025/2/21 21:56
 */

import com.jianzj.ticket.service.biz.ticketservice.dto.domain.TicketListDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 车票分页查询响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class TicketPageQueryRespDTO {

    /**
     * 车次集合数据
     */
    private List<TicketListDTO> trainList;

    /**
     * 车次类型：D-动车 Z-直达 复兴号等
     */
    private List<Integer> trainBrandList;

    /**
     * 出发车站
     */
    private List<String> departureStationList;

    /**
     * 到达车站
     */
    private List<String> arrivalStationList;

    /**
     * 车次席别
     */
    private List<Integer> seatClassTypeList;

}
