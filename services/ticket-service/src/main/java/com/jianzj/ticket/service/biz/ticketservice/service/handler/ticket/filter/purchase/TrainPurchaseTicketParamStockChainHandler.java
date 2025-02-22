package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.filter.purchase;

/**
 * @Author JianZJ
 * @Date 2025/2/22 14:47
 */

import cn.hutool.core.util.StrUtil;
import com.jianzj.ticket.service.biz.ticketservice.dto.domain.PurchaseTicketPassengerDetailDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.cache.SeatMarginCacheLoader;
import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.jianzj.ticket.service.biz.ticketservice.common.constant.RedisKeyConstant.TRAIN_STATION_REMAINING_TICKET;

/**
 * 购票流程过滤器之验证列车站点库存是否充足
 * 公众号：马丁玩编程，回复：加群，添加马哥微信（备注：12306）获取项目资料
 */
@Component
@RequiredArgsConstructor
public class TrainPurchaseTicketParamStockChainHandler implements TrainPurchaseTicketChainFilter<PurchaseTicketReqDTO> {

    private final SeatMarginCacheLoader seatMarginCacheLoader;
    private final DistributedCache distributedCache;

    @Override
    public void handler(PurchaseTicketReqDTO requestParam) {
        // 车次站点是否还有余票。如果用户提交多个乘车人非同一座位类型，拆分验证
        String keySuffix = StrUtil.join("_", requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival());
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        List<PurchaseTicketPassengerDetailDTO> passengerDetails = requestParam.getPassengers();
        Map<Integer, List<PurchaseTicketPassengerDetailDTO>> seatTypeMap = passengerDetails.stream()
                .collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));
        seatTypeMap.forEach((seatType, passengerSeatDetails) -> {
            Object stockObj = stringRedisTemplate.opsForHash().get(TRAIN_STATION_REMAINING_TICKET + keySuffix, String.valueOf(seatType));
            int stock = Optional.ofNullable(stockObj).map(each -> Integer.parseInt(each.toString())).orElseGet(() -> {
                Map<String, String> seatMarginMap = seatMarginCacheLoader.load(String.valueOf(requestParam.getTrainId()), String.valueOf(seatType), requestParam.getDeparture(), requestParam.getArrival());
                return Optional.ofNullable(seatMarginMap.get(String.valueOf(seatType))).map(Integer::parseInt).orElse(0);
            });
            if (stock >= passengerSeatDetails.size()) {
                return;
            }
            throw new ClientException("列车站点已无余票");
        });
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
