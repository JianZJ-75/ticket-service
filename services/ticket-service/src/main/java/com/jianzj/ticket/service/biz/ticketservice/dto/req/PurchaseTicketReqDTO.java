package com.jianzj.ticket.service.biz.ticketservice.dto.req;

import com.jianzj.ticket.service.biz.ticketservice.dto.domain.PurchaseTicketPassengerDetailDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author JianZJ
 * @Date 2025/2/22 13:46
 */

/**
 * 车票购买请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseTicketReqDTO {

    /**
     * 车次 ID
     */
    private String trainId;

    /**
     * 乘车人
     */
    private List<PurchaseTicketPassengerDetailDTO> passengers;

    /**
     * 选择座位
     */
    private List<String> chooseSeats;

    /**
     * 出发站点
     */
    private String departure;

    /**
     * 到达站点
     */
    private String arrival;

}
