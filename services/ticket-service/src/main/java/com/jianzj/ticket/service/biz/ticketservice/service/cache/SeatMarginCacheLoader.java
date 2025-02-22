package com.jianzj.ticket.service.biz.ticketservice.service.cache;

/**
 * @Author JianZJ
 * @Date 2025/2/22 12:03
 */

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.SeatStatusEnum;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.VehicleTypeEnum;
import com.jianzj.ticket.service.biz.ticketservice.dao.entity.SeatDO;
import com.jianzj.ticket.service.biz.ticketservice.dao.entity.TrainDO;
import com.jianzj.ticket.service.biz.ticketservice.dao.mapper.SeatMapper;
import com.jianzj.ticket.service.biz.ticketservice.dao.mapper.TrainMapper;
import com.jianzj.ticket.service.biz.ticketservice.dto.domain.RouteDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.TrainStationService;
import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import com.jianzj.ticket.service.frameworks.starter.cache.tool.CacheUtil;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.jianzj.ticket.service.biz.ticketservice.common.constant.RedisKeyConstant.*;
import static com.jianzj.ticket.service.biz.ticketservice.common.constant.TicketServiceConstant.ADVANCE_TICKET_DAY;

/**
 * 座位余量缓存加载
 */
@Component
@RequiredArgsConstructor
public class SeatMarginCacheLoader {

    private final TrainMapper trainMapper;
    private final SeatMapper seatMapper;
    private final DistributedCache distributedCache;
    private final RedissonClient redissonClient;
    private final TrainStationService trainStationService;

    public Map<String, String> load(String trainId, String seatType, String departure, String arrival) {
        Map<String, Map<String, String>> trainStationRemainingTicketMaps = new LinkedHashMap<>();
        // 构造缓存key
        String keySuffix = CacheUtil.buildKey(trainId, departure, arrival);
        RLock lock = redissonClient.getLock(String.format(LOCK_SAFE_LOAD_SEAT_MARGIN_GET, keySuffix));
        lock.lock();
        try {
            // 双重判断
            StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
            Object quantityObj = stringRedisTemplate.opsForHash().get(TRAIN_STATION_REMAINING_TICKET + keySuffix, seatType);
            if (CacheUtil.isNullOrBlank(quantityObj)) {
                // 获取列车信息
                TrainDO trainDO = distributedCache.safeGet(
                        TRAIN_INFO + trainId,
                        TrainDO.class,
                        () -> trainMapper.selectById(trainId),
                        ADVANCE_TICKET_DAY,
                        TimeUnit.DAYS
                );
                // 获取该列车的所有站点
                List<RouteDTO> routeDTOList = trainStationService.listTrainStationRoute(trainId, trainDO.getStartStation(), trainDO.getEndStation());
                if (CollUtil.isNotEmpty(routeDTOList)) {
                    switch (trainDO.getTrainType()) {
                        case 0 -> {
                            for (RouteDTO each : routeDTOList) {
                                Map<String, String> trainStationRemainingTicket = new LinkedHashMap<>();
                                trainStationRemainingTicket.put("0", selectSeatMargin(trainId, 0, each.getStartStation(), each.getEndStation()));
                                trainStationRemainingTicket.put("1", selectSeatMargin(trainId, 1, each.getStartStation(), each.getEndStation()));
                                trainStationRemainingTicket.put("2", selectSeatMargin(trainId, 2, each.getStartStation(), each.getEndStation()));
                                String actualKeySuffix = CacheUtil.buildKey(trainId, each.getStartStation(), each.getEndStation());
                                trainStationRemainingTicketMaps.put(TRAIN_STATION_REMAINING_TICKET + actualKeySuffix, trainStationRemainingTicket);
                            }
                        }
                        case 1 -> {
                            for (RouteDTO each : routeDTOList) {
                                Map<String, String> trainStationRemainingTicket = new LinkedHashMap<>();
                                trainStationRemainingTicket.put("3", selectSeatMargin(trainId, 3, each.getStartStation(), each.getEndStation()));
                                trainStationRemainingTicket.put("4", selectSeatMargin(trainId, 4, each.getStartStation(), each.getEndStation()));
                                trainStationRemainingTicket.put("5", selectSeatMargin(trainId, 5, each.getStartStation(), each.getEndStation()));
                                trainStationRemainingTicket.put("13", selectSeatMargin(trainId, 13, each.getStartStation(), each.getEndStation()));
                                String actualKeySuffix = CacheUtil.buildKey(trainId, each.getStartStation(), each.getEndStation());
                                trainStationRemainingTicketMaps.put(TRAIN_STATION_REMAINING_TICKET + actualKeySuffix, trainStationRemainingTicket);
                            }
                        }
                        case 2 -> {
                            for (RouteDTO each : routeDTOList) {
                                Map<String, String> trainStationRemainingTicket = new LinkedHashMap<>();
                                trainStationRemainingTicket.put("6", selectSeatMargin(trainId, 6, each.getStartStation(), each.getEndStation()));
                                trainStationRemainingTicket.put("7", selectSeatMargin(trainId, 7, each.getStartStation(), each.getEndStation()));
                                trainStationRemainingTicket.put("8", selectSeatMargin(trainId, 8, each.getStartStation(), each.getEndStation()));
                                trainStationRemainingTicket.put("13", selectSeatMargin(trainId, 13, each.getStartStation(), each.getEndStation()));
                                String actualKeySuffix = CacheUtil.buildKey(trainId, each.getStartStation(), each.getEndStation());
                                trainStationRemainingTicketMaps.put(TRAIN_STATION_REMAINING_TICKET + actualKeySuffix, trainStationRemainingTicket);
                            }
                        }
                    }
                } else {
                    Map<String, String> trainStationRemainingTicket = new LinkedHashMap<>();
                    VehicleTypeEnum.findSeatTypesByCode(trainDO.getTrainType())
                            .forEach(each -> trainStationRemainingTicket.put(String.valueOf(each), "0"));
                    trainStationRemainingTicketMaps.put(TRAIN_STATION_REMAINING_TICKET + keySuffix, trainStationRemainingTicket);
                }
                // TODO LUA 脚本执行
                trainStationRemainingTicketMaps.forEach((cacheKey, cacheMap) -> stringRedisTemplate.opsForHash().putAll(cacheKey, cacheMap));
            }
        } finally {
            lock.unlock();
        }
        return Optional.ofNullable(trainStationRemainingTicketMaps.get(TRAIN_STATION_REMAINING_TICKET + keySuffix))
                .orElse(new LinkedHashMap<>());
    }

    /**
     * 查询列车某个站点座位的余量
     * @param trainId
     * @param type
     * @param departure
     * @param arrival
     * @return
     */
    private String selectSeatMargin(String trainId, Integer type, String departure, String arrival) {
        LambdaQueryWrapper<SeatDO> queryWrapper = Wrappers.lambdaQuery(SeatDO.class)
                .eq(SeatDO::getTrainId, trainId)
                .eq(SeatDO::getSeatType, type)
                .eq(SeatDO::getSeatStatus, SeatStatusEnum.AVAILABLE.getCode())
                .eq(SeatDO::getStartStation, departure)
                .eq(SeatDO::getEndStation, arrival);
        return Optional.ofNullable(seatMapper.selectCount(queryWrapper))
                .map(String::valueOf)
                .orElse("0");
    }

}
