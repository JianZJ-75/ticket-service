package com.jianzj.ticket.service.biz.ticketservice.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.SeatStatusEnum;
import com.jianzj.ticket.service.biz.ticketservice.dao.entity.SeatDO;
import com.jianzj.ticket.service.biz.ticketservice.dao.entity.TrainStationRelationDO;
import com.jianzj.ticket.service.biz.ticketservice.dao.mapper.SeatMapper;
import com.jianzj.ticket.service.biz.ticketservice.dao.mapper.TrainStationRelationMapper;
import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import com.jianzj.ticket.service.frameworks.starter.common.tools.ThreadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jianzj.ticket.service.frameworks.starter.convention.result.Result;
import com.jianzj.ticket.service.frameworks.starter.web.Results;

import java.util.List;

import static com.jianzj.ticket.service.biz.ticketservice.common.constant.RedisKeyConstant.TICKET_AVAILABILITY_TOKEN_BUCKET;
import static com.jianzj.ticket.service.biz.ticketservice.common.constant.RedisKeyConstant.TRAIN_STATION_REMAINING_TICKET;

/**
 * @Author JianZJ
 * @Date 2025/2/21 20:26
 */

/**
 * 座位重置控制层
 */
@Deprecated
@RestController
@RequiredArgsConstructor
@Slf4j
public class TempSeatController {

    private final SeatMapper seatMapper;
    private final TrainStationRelationMapper trainStationRelationMapper;
    private final DistributedCache distributedCache;

    /**
     * 座位重置
     */
    @PostMapping("/api/ticket-service/temp/seat/reset")
    public Result<Void> purchaseTickets(@RequestParam String trainId) {
        SeatDO seatDO = new SeatDO();
        // 将座位状态修改为可售
        seatDO.setSeatStatus(SeatStatusEnum.AVAILABLE.getCode());
        // 更新数据库表
        seatMapper.update(seatDO, Wrappers.lambdaUpdate(SeatDO.class).eq(SeatDO::getTrainId, trainId));
        ThreadUtil.sleep(5000);
        // 获取RedisTemplate实例
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        List<TrainStationRelationDO> trainStationRelationDOList = trainStationRelationMapper.selectList(Wrappers.lambdaQuery(TrainStationRelationDO.class)
                .eq(TrainStationRelationDO::getTrainId, trainId));
        // 将redis中该趟车的数据释放
        for (TrainStationRelationDO each : trainStationRelationDOList) {
            String keySuffix = StrUtil.join("_", each.getTrainId(), each.getDeparture(), each.getArrival());
            stringRedisTemplate.delete(TRAIN_STATION_REMAINING_TICKET + keySuffix);
        }
        // 删除车辆余票令牌桶
        stringRedisTemplate.delete(TICKET_AVAILABILITY_TOKEN_BUCKET + trainId);
        return Results.success();
    }

}
