package com.jianzj.ticket.service.biz.ticketservice.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.SourceEnum;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.TicketChainMarkEnum;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.TicketStatusEnum;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.VehicleTypeEnum;
import com.jianzj.ticket.service.biz.ticketservice.dao.entity.*;
import com.jianzj.ticket.service.biz.ticketservice.dao.mapper.*;
import com.jianzj.ticket.service.biz.ticketservice.dto.domain.*;
import com.jianzj.ticket.service.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.req.TicketPageQueryReqDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.resp.TicketPageQueryRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.resp.TicketPurchaseRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.remote.PayRemoteService;
import com.jianzj.ticket.service.biz.ticketservice.remote.TicketOrderRemoteService;
import com.jianzj.ticket.service.biz.ticketservice.remote.dto.*;
import com.jianzj.ticket.service.biz.ticketservice.service.SeatService;
import com.jianzj.ticket.service.biz.ticketservice.service.TicketService;
import com.jianzj.ticket.service.biz.ticketservice.service.TrainStationService;
import com.jianzj.ticket.service.biz.ticketservice.service.cache.SeatMarginCacheLoader;
import com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.dto.TokenResultDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.select.TrainSeatTypeSelector;
import com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.tokenbucket.TicketAvailabilityTokenBucket;
import com.jianzj.ticket.service.biz.ticketservice.tools.DateUtil;
import com.jianzj.ticket.service.biz.ticketservice.tools.TimeStringComparator;
import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import com.jianzj.ticket.service.frameworks.starter.cache.tool.CacheUtil;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ServiceException;
import com.jianzj.ticket.service.frameworks.starter.convention.result.Result;
import com.jianzj.ticket.service.frameworks.starter.designpattern.chain.AbstractChainContext;
import com.jianzj.ticket.service.frameworks.starter.idempotent.annotation.Idempotent;
import com.jianzj.ticket.service.frameworks.starter.idempotent.enums.IdempotentSceneEnum;
import com.jianzj.ticket.service.frameworks.starter.idempotent.enums.IdempotentTypeEnum;
import com.jianzj.ticket.service.frameworks.starter.log.annotation.ILog;
import com.jianzj.ticket.service.frameworks.starter.user.core.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.jianzj.ticket.service.biz.ticketservice.common.constant.RedisKeyConstant.*;
import static com.jianzj.ticket.service.biz.ticketservice.common.constant.TicketServiceConstant.ADVANCE_TICKET_DAY;
import static com.jianzj.ticket.service.biz.ticketservice.tools.DateUtil.convertDateToLocalTime;

/**
 * @Author JianZJ
 * @Date 2025/2/21 21:52
 */

/**
 * 车票接口实现层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl extends ServiceImpl<TicketMapper, TicketDO> implements TicketService, CommandLineRunner {

    private final TrainMapper trainMapper;
    private final TrainStationRelationMapper trainStationRelationMapper;
    private final TrainStationPriceMapper trainStationPriceMapper;
    private final DistributedCache distributedCache;
    private final TicketOrderRemoteService ticketOrderRemoteService;
    private final PayRemoteService payRemoteService;
    private final StationMapper stationMapper;
    private final SeatService seatService;
    private final TrainStationService trainStationService;
    private final TrainSeatTypeSelector trainSeatTypeSelector;
    private final SeatMarginCacheLoader seatMarginCacheLoader;
    private final AbstractChainContext<TicketPageQueryReqDTO> ticketPageQueryAbstractChainContext;
    private final AbstractChainContext<PurchaseTicketReqDTO> purchaseTicketAbstractChainContext;
    private final AbstractChainContext<RefundTicketReqDTO> refundReqDTOAbstractChainContext;
    private final RedissonClient redissonClient;
    private final ConfigurableEnvironment environment;
    private final TicketAvailabilityTokenBucket ticketAvailabilityTokenBucket;
    private TicketService ticketService;

    @Value("${ticket.availability.cache-update.type:}")
    private String ticketAvailabilityCacheUpdateType;
    @Value("${framework.cache.redis.prefix:}")
    private String cacheRedisPrefix;

    /**
     * 1. 验证数据是否正确
     * 2. 加载城市信息
     * 3. 查询列车站点信息
     * 4. 列车余票信息
     * 5. 构建列车返回数据
     * @param requestParam 分页查询车票请求参数
     * @return
     */
    @Override
    public TicketPageQueryRespDTO pageListTicketQueryV1(TicketPageQueryReqDTO requestParam) {
        // 责任链模式 验证城市名称是否存在、不存在加载缓存以及出发日期不能小于当前日期等等
        ticketPageQueryAbstractChainContext.handler(TicketChainMarkEnum.TRAIN_QUERY_FILTER.name(), requestParam);
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        // 通过批量查询方式获取出发站点和到达站点对应的城市集合
        List<Object> stationDetails = stringRedisTemplate.opsForHash()
                .multiGet(REGION_TRAIN_STATION_MAPPING, Lists.newArrayList(requestParam.getFromStation(), requestParam.getToStation()));
        long count = stationDetails.stream().filter(Objects::isNull).count();
        // 如果城市数据为空，代表缓存中没有这个值，需要从数据库加载
        if (count > 0) {
            // 避免缓存击穿，通过分布式锁方式解决该问题
            RLock lock = redissonClient.getLock(LOCK_REGION_TRAIN_STATION_MAPPING);
            lock.lock();
            try {
                // 双重判定锁，规避数据库无效请求
                stationDetails = stringRedisTemplate.opsForHash()
                        .multiGet(REGION_TRAIN_STATION_MAPPING, Lists.newArrayList(requestParam.getFromStation(), requestParam.getToStation()));
                count = stationDetails.stream().filter(Objects::isNull).count();
                if (count > 0) {
                    // 查询列车站点数据与城市信息
                    List<StationDO> stationDOList = stationMapper.selectList(Wrappers.emptyWrapper());
                    Map<String, String> regionTrainStationMap = new HashMap<>();
                    stationDOList.forEach(each -> regionTrainStationMap.put(each.getCode(), each.getRegionName()));
                    // 通过 putAll 批量保存方式存入 Redis，避免多次 put 网络 IO 消耗
                    stringRedisTemplate.opsForHash().putAll(REGION_TRAIN_STATION_MAPPING, regionTrainStationMap);
                    stationDetails = new ArrayList<>();
                    stationDetails.add(regionTrainStationMap.get(requestParam.getFromStation()));
                    stationDetails.add(regionTrainStationMap.get(requestParam.getToStation()));
                }
            } finally {
                lock.unlock();
            }
        }
        List<TicketListDTO> seatResults = new ArrayList<>();
        // 构建查询 Redis 中 Hash 结构 Key，Key前缀 + 出发城市 + 到达城市
        String buildRegionTrainStationHashKey = String.format(REGION_TRAIN_STATION, stationDetails.get(0), stationDetails.get(1));
        Map<Object, Object> regionTrainStationAllMap = stringRedisTemplate.opsForHash().entries(buildRegionTrainStationHashKey);
        // 如果为空，兜底查询数据库再放入缓存
        if (MapUtil.isEmpty(regionTrainStationAllMap)) {
            RLock lock = redissonClient.getLock(LOCK_REGION_TRAIN_STATION);
            lock.lock();
            try {
                //双重判定锁
                regionTrainStationAllMap = stringRedisTemplate.opsForHash().entries(buildRegionTrainStationHashKey);
                if (MapUtil.isEmpty(regionTrainStationAllMap)) {
                    // 加载数据库列车相关信息，并构建出一趟列车详细记录
                    LambdaQueryWrapper<TrainStationRelationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationRelationDO.class)
                            .eq(TrainStationRelationDO::getStartRegion, stationDetails.get(0))
                            .eq(TrainStationRelationDO::getEndRegion, stationDetails.get(1));
                    List<TrainStationRelationDO> trainStationRelationList = trainStationRelationMapper.selectList(queryWrapper);
                    for (TrainStationRelationDO each : trainStationRelationList) {
                        // 获取一趟列车的信息
                        TrainDO trainDO = distributedCache.safeGet(
                                TRAIN_INFO + each.getTrainId(),
                                TrainDO.class,
                                () -> trainMapper.selectById(each.getTrainId()),
                                ADVANCE_TICKET_DAY,
                                TimeUnit.DAYS);
                        TicketListDTO result = new TicketListDTO();
                        result.setTrainId(String.valueOf(trainDO.getId()));
                        result.setTrainNumber(trainDO.getTrainNumber());
                        result.setDepartureTime(convertDateToLocalTime(each.getDepartureTime(), "HH:mm"));
                        result.setArrivalTime(convertDateToLocalTime(each.getArrivalTime(), "HH:mm"));
                        result.setDuration(DateUtil.calculateHourDifference(each.getDepartureTime(), each.getArrivalTime()));
                        result.setDeparture(each.getDeparture());
                        result.setArrival(each.getArrival());
                        result.setDepartureFlag(each.getDepartureFlag());
                        result.setArrivalFlag(each.getArrivalFlag());
                        result.setTrainType(trainDO.getTrainType());
                        result.setTrainBrand(trainDO.getTrainBrand());
                        if (StrUtil.isNotBlank(trainDO.getTrainTag())) {
                            result.setTrainTags(StrUtil.split(trainDO.getTrainTag(), ","));
                        }
                        long betweenDay = cn.hutool.core.date.DateUtil.betweenDay(each.getDepartureTime(), each.getArrivalTime(), false);
                        result.setDaysArrived((int) betweenDay);
                        result.setSaleStatus(new Date().after(trainDO.getSaleTime()) ? 0 : 1);
                        result.setSaleTime(convertDateToLocalTime(trainDO.getSaleTime(), "MM-dd HH:mm"));
                        seatResults.add(result);
                        regionTrainStationAllMap.put(CacheUtil.buildKey(String.valueOf(each.getTrainId()), each.getDeparture(), each.getArrival()), JSON.toJSONString(result));
                    }
                    stringRedisTemplate.opsForHash().putAll(buildRegionTrainStationHashKey, regionTrainStationAllMap);
                }
            } finally {
                lock.unlock();
            }
        }
        // 查询出来列车基本信息后 开始对列车按照出发时间进行排序
        seatResults = CollUtil.isEmpty(seatResults)
                ? regionTrainStationAllMap.values().stream().map(each -> JSON.parseObject(each.toString(), TicketListDTO.class)).toList()
                : seatResults;
        seatResults = seatResults.stream().sorted(new TimeStringComparator()).toList();
        for (TicketListDTO each : seatResults) {
            // 获取列车数据
            String trainStationPriceStr = distributedCache.safeGet(
                    String.format(TRAIN_STATION_PRICE, each.getTrainId(), each.getDeparture(), each.getArrival()),
                    String.class,
                    () -> {
                        LambdaQueryWrapper<TrainStationPriceDO> trainStationPriceQueryWrapper = Wrappers.lambdaQuery(TrainStationPriceDO.class)
                                .eq(TrainStationPriceDO::getDeparture, each.getDeparture())
                                .eq(TrainStationPriceDO::getArrival, each.getArrival())
                                .eq(TrainStationPriceDO::getTrainId, each.getTrainId());
                        return JSON.toJSONString(trainStationPriceMapper.selectList(trainStationPriceQueryWrapper));
                    },
                    ADVANCE_TICKET_DAY,
                    TimeUnit.DAYS
            );
            // 将从缓存获取到的数据转换为所需类型
            List<TrainStationPriceDO> trainStationPriceDOList = JSON.parseArray(trainStationPriceStr, TrainStationPriceDO.class);
            List<SeatClassDTO> seatClassList = new ArrayList<>();
            trainStationPriceDOList.forEach(item -> {
                String seatType = String.valueOf(item.getSeatType());
                String keySuffix = StrUtil.join("_", each.getTrainId(), item.getDeparture(), item.getArrival());
                Object quantityObj = stringRedisTemplate.opsForHash().get(TRAIN_STATION_REMAINING_TICKET + keySuffix, seatType);
                // 如果在缓存中获取不到该站点余票情况 就去加载缓存
                int quantity = Optional.ofNullable(quantityObj)
                        .map(Object::toString)
                        .map(Integer::parseInt)
                        .orElseGet(() -> {
                            Map<String, String> seatMarginMap = seatMarginCacheLoader.load(String.valueOf(each.getTrainId()), seatType, item.getDeparture(), item.getArrival());
                            return Optional.ofNullable(seatMarginMap.get(String.valueOf(item.getSeatType()))).map(Integer::parseInt).orElse(0);
                        });
                seatClassList.add(new SeatClassDTO(item.getSeatType(), quantity, new BigDecimal(item.getPrice()).divide(new BigDecimal("100"), 1, RoundingMode.HALF_UP), false));
            });
            each.setSeatClassList(seatClassList);
        }
        return TicketPageQueryRespDTO.builder()
                .trainList(seatResults)
                .departureStationList(buildDepartureStationList(seatResults))
                .arrivalStationList(buildArrivalStationList(seatResults))
                .trainBrandList(buildTrainBrandList(seatResults))
                .seatClassTypeList(buildSeatClassList(seatResults))
                .build();
    }

    @Override
    public TicketPageQueryRespDTO pageListTicketQueryV2(TicketPageQueryReqDTO requestParam) {
        // 责任链模式 验证城市名称是否存在、不存在加载缓存以及出发日期不能小于当前日期等等
        ticketPageQueryAbstractChainContext.handler(TicketChainMarkEnum.TRAIN_QUERY_FILTER.name(), requestParam);
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        // 获取出发地和目的地
        List<Object> stationDetails = stringRedisTemplate.opsForHash()
                .multiGet(REGION_TRAIN_STATION_MAPPING, Lists.newArrayList(requestParam.getFromStation(), requestParam.getToStation()));
        String buildRegionTrainStationHashKey = String.format(REGION_TRAIN_STATION, stationDetails.get(0), stationDetails.get(1));
        // 获取两地间站点信息
        Map<Object, Object> regionTrainStationAllMap = stringRedisTemplate.opsForHash().entries(buildRegionTrainStationHashKey);
        // 获取列车信息并排序
        List<TicketListDTO> seatResults = regionTrainStationAllMap.values().stream()
                .map(each -> JSON.parseObject(each.toString(), TicketListDTO.class))
                .sorted(new TimeStringComparator())
                .toList();
        // 创建列车站点价格key
        List<String> trainStationPriceKeys = seatResults.stream()
                .map(each -> String.format(cacheRedisPrefix + TRAIN_STATION_PRICE, each.getTrainId(), each.getDeparture(), each.getArrival()))
                .toList();
        // 通过redis的管道技术一次性获取全部列车站点价格
        List<Object> trainStationPriceObjs = stringRedisTemplate.executePipelined((RedisCallback<String>) connection -> {
            trainStationPriceKeys.forEach(each -> connection.stringCommands().get(each.getBytes()));
            return null;
        });
        List<TrainStationPriceDO> trainStationPriceDOList = new ArrayList<>();
        List<String> trainStationRemainingKeyList = new ArrayList<>();
        // 创建站点余票key
        for (Object each : trainStationPriceObjs) {
            List<TrainStationPriceDO> trainStationPriceList = JSON.parseArray(each.toString(), TrainStationPriceDO.class);
            trainStationPriceDOList.addAll(trainStationPriceList);
            for (TrainStationPriceDO item : trainStationPriceList) {
                String trainStationRemainingKey = cacheRedisPrefix + TRAIN_STATION_REMAINING_TICKET + StrUtil.join("_", item.getTrainId(), item.getDeparture(), item.getArrival());
                trainStationRemainingKeyList.add(trainStationRemainingKey);
            }
        }
        // 通过redis的管道技术一次性获取全部站点余票
        List<Object> trainStationRemainingObjs = stringRedisTemplate.executePipelined((RedisCallback<String>) connection -> {
            for (int i = 0; i < trainStationRemainingKeyList.size(); i++) {
                connection.hashCommands().hGet(trainStationRemainingKeyList.get(i).getBytes(), trainStationPriceDOList.get(i).getSeatType().toString().getBytes());
            }
            return null;
        });
        for (TicketListDTO each : seatResults) {
            // 能对应的原因 管道技术是按照顺序执行的 所以不需要列车id就能获取余票(但是用完的需要清楚才能保证)
            // 处理座位类型信息
            List<Integer> seatTypesByCode = VehicleTypeEnum.findSeatTypesByCode(each.getTrainType());
            // 从 trainStationRemainingObjs 列表的第 0 个元素开始 截取与座位类型相等的子列表
            List<Object> remainingTicket = new ArrayList<>(trainStationRemainingObjs.subList(0, seatTypesByCode.size()));
            // 从 trainStationPriceDOList 列表的第 0 个元素开始 截取与座位类型相等的子列表
            List<TrainStationPriceDO> trainStationPriceDOSub = new ArrayList<>(trainStationPriceDOList.subList(0, seatTypesByCode.size()));
            // 清除已使用的部分数据
            trainStationRemainingObjs.subList(0, seatTypesByCode.size()).clear();
            trainStationPriceDOList.subList(0, seatTypesByCode.size()).clear();
            List<SeatClassDTO> seatClassList = new ArrayList<>();
            for (int i = 0; i < trainStationPriceDOSub.size(); i++) {
                TrainStationPriceDO trainStationPriceDO = trainStationPriceDOSub.get(i);
                SeatClassDTO seatClassDTO = SeatClassDTO.builder()
                        .type(trainStationPriceDO.getSeatType())
                        .quantity(Integer.parseInt(remainingTicket.get(i).toString()))
                        .price(new BigDecimal(trainStationPriceDO.getPrice()).divide(new BigDecimal("100"), 1, RoundingMode.HALF_UP))
                        .candidate(false)
                        .build();
                seatClassList.add(seatClassDTO);
            }
            each.setSeatClassList(seatClassList);
        }
        return TicketPageQueryRespDTO.builder()
                .trainList(seatResults)
                .departureStationList(buildDepartureStationList(seatResults))
                .arrivalStationList(buildArrivalStationList(seatResults))
                .trainBrandList(buildTrainBrandList(seatResults))
                .seatClassTypeList(buildSeatClassList(seatResults))
                .build();
    }

    @ILog
    @Idempotent(
            uniqueKeyPrefix = "ticket-service-ticket:lock_purchase-tickets:",
            key = "T(com.jianzj.ticket.service.frameworks.starter.base.ApplicationContextHolder).getBean('environment').getProperty('unique-name', '')"
                    + "+'_'+"
                    + "T(com.jianzj.ticket.service.frameworks.starter.user.core.UserContext).getUsername()",
            message = "正在执行下单流程，请稍后...",
            scene = IdempotentSceneEnum.RESTAPI,
            type = IdempotentTypeEnum.SPEL
    )
    @Override
    public TicketPurchaseRespDTO purchaseTicketsV1(PurchaseTicketReqDTO requestParam) {
        // 责任链模式，验证 1：参数必填 2：参数正确性 3：乘客是否已买当前车次等...
        purchaseTicketAbstractChainContext.handler(TicketChainMarkEnum.TRAIN_PURCHASE_TICKET_FILTER.name(), requestParam);
        String lockKey = environment.resolvePlaceholders(String.format(LOCK_PURCHASE_TICKETS, requestParam.getTrainId()));
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            return ticketService.executePurchaseTickets(requestParam);
        } finally {
            lock.unlock();
        }
    }

    private final Cache<String, ReentrantLock> localLockMap = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build();

    private final Cache<String, Object> tokenTicketsRefreshMap = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    @ILog
    @Idempotent(
            uniqueKeyPrefix = "ticket-service-ticket:lock_purchase-tickets:",
            key = "T(com.jianzj.ticket.service.frameworks.starter.base.ApplicationContextHolder).getBean('environment').getProperty('unique-name', '')"
                    + "+'_'+"
                    + "T(com.jianzj.ticket.service.frameworks.starter.user.core.UserContext).getUsername()",
            message = "正在执行下单流程，请稍后...",
            scene = IdempotentSceneEnum.RESTAPI,
            type = IdempotentTypeEnum.SPEL
    )
    @Override
    public TicketPurchaseRespDTO purchaseTicketsV2(PurchaseTicketReqDTO requestParam) {
        // 责任链模式，验证 1：参数必填 2：参数正确性 3：乘客是否已买当前车次等...
        purchaseTicketAbstractChainContext.handler(TicketChainMarkEnum.TRAIN_PURCHASE_TICKET_FILTER.name(), requestParam);
        // 为什么需要令牌限流？余票缓存限流不可以么？详情查看：https://nageoffer.com/12306/question
        TokenResultDTO tokenResult = ticketAvailabilityTokenBucket.takeTokenFromBucket(requestParam);
        if (tokenResult.getTokenIsNull()) {
            Object ifPresentObj = tokenTicketsRefreshMap.getIfPresent(requestParam.getTrainId());
            if (ifPresentObj == null) {
                synchronized (TicketService.class) {
                    if (tokenTicketsRefreshMap.getIfPresent(requestParam.getTrainId()) == null) {
                        ifPresentObj = new Object();
                        tokenTicketsRefreshMap.put(requestParam.getTrainId(), ifPresentObj);
                        tokenIsNullRefreshToken(requestParam, tokenResult);
                    }
                }
            }
            throw new ServiceException("列车站点已无余票");
        }
        // v1 版本购票存在 4 个较为严重的问题，v2 版本相比较 v1 版本更具有业务特点以及性能，整体提升较大
        // 写了详细的 v2 版本购票升级指南，详情查看：https://nageoffer.com/12306/question
        List<ReentrantLock> localLockList = new ArrayList<>();
        List<RLock> distributedLockList = new ArrayList<>();
        Map<Integer, List<PurchaseTicketPassengerDetailDTO>> seatTypeMap = requestParam.getPassengers().stream()
                .collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));
        seatTypeMap.forEach((searType, count) -> {
            String lockKey = environment.resolvePlaceholders(String.format(LOCK_PURCHASE_TICKETS_V2, requestParam.getTrainId(), searType));
            ReentrantLock localLock = localLockMap.getIfPresent(lockKey);
            if (localLock == null) {
                synchronized (TicketService.class) {
                    if ((localLock = localLockMap.getIfPresent(lockKey)) == null) {
                        localLock = new ReentrantLock(true);
                        localLockMap.put(lockKey, localLock);
                    }
                }
            }
            localLockList.add(localLock);
            RLock distributedLock = redissonClient.getFairLock(lockKey);
            distributedLockList.add(distributedLock);
        });
        try {
            localLockList.forEach(ReentrantLock::lock);
            distributedLockList.forEach(RLock::lock);
            return ticketService.executePurchaseTickets(requestParam);
        } finally {
            localLockList.forEach(localLock -> {
                try {
                    localLock.unlock();
                } catch (Throwable ignored) {
                }
            });
            distributedLockList.forEach(distributedLock -> {
                try {
                    distributedLock.unlock();
                } catch (Throwable ignored) {
                }
            });
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public TicketPurchaseRespDTO executePurchaseTickets(PurchaseTicketReqDTO requestParam) {
        // 初始化和获取列车信息
        List<TicketOrderDetailRespDTO> ticketOrderDetailResults = new ArrayList<>();
        String trainId = requestParam.getTrainId();
        TrainDO trainDO = distributedCache.safeGet(
                TRAIN_INFO + trainId,
                TrainDO.class,
                () -> trainMapper.selectById(trainId),
                ADVANCE_TICKET_DAY,
                TimeUnit.DAYS);
        // 选择座位
        List<TrainPurchaseTicketRespDTO> trainPurchaseTicketResults = trainSeatTypeSelector.select(trainDO.getTrainType(), requestParam);
        // 创建车票数据对象并保存
        List<TicketDO> ticketDOList = trainPurchaseTicketResults.stream()
                .map(each -> TicketDO.builder()
                        .username(UserContext.getUsername())
                        .trainId(Long.parseLong(requestParam.getTrainId()))
                        .carriageNumber(each.getCarriageNumber())
                        .seatNumber(each.getSeatNumber())
                        .passengerId(each.getPassengerId())
                        .ticketStatus(TicketStatusEnum.UNPAID.getCode())
                        .build())
                .toList();
        saveBatch(ticketDOList);
        // 构建订单远程请求数据
        Result<String> ticketOrderResult;
        try {
            List<TicketOrderItemCreateRemoteReqDTO> orderItemCreateRemoteReqDTOList = new ArrayList<>();
            trainPurchaseTicketResults.forEach(each -> {
                TicketOrderItemCreateRemoteReqDTO orderItemCreateRemoteReqDTO = TicketOrderItemCreateRemoteReqDTO.builder()
                        .amount(each.getAmount())
                        .carriageNumber(each.getCarriageNumber())
                        .seatNumber(each.getSeatNumber())
                        .idCard(each.getIdCard())
                        .idType(each.getIdType())
                        .phone(each.getPhone())
                        .seatType(each.getSeatType())
                        .ticketType(each.getUserType())
                        .realName(each.getRealName())
                        .build();
                TicketOrderDetailRespDTO ticketOrderDetailRespDTO = TicketOrderDetailRespDTO.builder()
                        .amount(each.getAmount())
                        .carriageNumber(each.getCarriageNumber())
                        .seatNumber(each.getSeatNumber())
                        .idCard(each.getIdCard())
                        .idType(each.getIdType())
                        .seatType(each.getSeatType())
                        .ticketType(each.getUserType())
                        .realName(each.getRealName())
                        .build();
                orderItemCreateRemoteReqDTOList.add(orderItemCreateRemoteReqDTO);
                ticketOrderDetailResults.add(ticketOrderDetailRespDTO);
            });
            // 查询列车站点关系数据 获取出发时间 到达时间等信息
            LambdaQueryWrapper<TrainStationRelationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationRelationDO.class)
                    .eq(TrainStationRelationDO::getTrainId, trainId)
                    .eq(TrainStationRelationDO::getDeparture, requestParam.getDeparture())
                    .eq(TrainStationRelationDO::getArrival, requestParam.getArrival());
            TrainStationRelationDO trainStationRelationDO = trainStationRelationMapper.selectOne(queryWrapper);
            TicketOrderCreateRemoteReqDTO orderCreateRemoteReqDTO = TicketOrderCreateRemoteReqDTO.builder()
                    .departure(requestParam.getDeparture())
                    .arrival(requestParam.getArrival())
                    .orderTime(new Date())
                    .source(SourceEnum.INTERNET.getCode())
                    .trainNumber(trainDO.getTrainNumber())
                    .departureTime(trainStationRelationDO.getDepartureTime())
                    .arrivalTime(trainStationRelationDO.getArrivalTime())
                    .ridingDate(trainStationRelationDO.getDepartureTime())
                    .userId(UserContext.getUserId())
                    .username(UserContext.getUsername())
                    .trainId(Long.parseLong(requestParam.getTrainId()))
                    .ticketOrderItems(orderItemCreateRemoteReqDTOList)
                    .build();
            // 远程调用订单服务创建订单
            ticketOrderResult = ticketOrderRemoteService.createTicketOrder(orderCreateRemoteReqDTO);
            if (!ticketOrderResult.isSuccess() || StrUtil.isBlank(ticketOrderResult.getData())) {
                log.error("订单服务调用失败，返回结果：{}", ticketOrderResult.getMessage());
                throw new ServiceException("订单服务调用失败");
            }
        } catch (Throwable ex) {
            log.error("远程调用订单服务创建错误，请求参数：{}", JSON.toJSONString(requestParam), ex);
            throw ex;
        }
        return new TicketPurchaseRespDTO(ticketOrderResult.getData(), ticketOrderDetailResults);
    }

    @Override
    public PayInfoRespDTO getPayInfo(String orderSn) {
        return payRemoteService.getPayInfo(orderSn).getData();
    }

    @ILog
    @Override
    public void cancelTicketOrder(CancelTicketOrderReqDTO requestParam) {
        Result<Void> cancelOrderResult = ticketOrderRemoteService.cancelTicketOrder(requestParam);
        if (cancelOrderResult.isSuccess() && !StrUtil.equals(ticketAvailabilityCacheUpdateType, "binlog")) {
            Result<org.opengoofy.index12306.biz.ticketservice.remote.dto.TicketOrderDetailRespDTO> ticketOrderDetailResult = ticketOrderRemoteService.queryTicketOrderByOrderSn(requestParam.getOrderSn());
            org.opengoofy.index12306.biz.ticketservice.remote.dto.TicketOrderDetailRespDTO ticketOrderDetail = ticketOrderDetailResult.getData();
            String trainId = String.valueOf(ticketOrderDetail.getTrainId());
            String departure = ticketOrderDetail.getDeparture();
            String arrival = ticketOrderDetail.getArrival();
            List<TicketOrderPassengerDetailRespDTO> trainPurchaseTicketResults = ticketOrderDetail.getPassengerDetails();
            try {
                seatService.unlock(trainId, departure, arrival, BeanUtil.convert(trainPurchaseTicketResults, TrainPurchaseTicketRespDTO.class));
            } catch (Throwable ex) {
                log.error("[取消订单] 订单号：{} 回滚列车DB座位状态失败", requestParam.getOrderSn(), ex);
                throw ex;
            }
            ticketAvailabilityTokenBucket.rollbackInBucket(ticketOrderDetail);
            try {
                StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
                Map<Integer, List<TicketOrderPassengerDetailRespDTO>> seatTypeMap = trainPurchaseTicketResults.stream()
                        .collect(Collectors.groupingBy(TicketOrderPassengerDetailRespDTO::getSeatType));
                List<RouteDTO> routeDTOList = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
                routeDTOList.forEach(each -> {
                    String keySuffix = StrUtil.join("_", trainId, each.getStartStation(), each.getEndStation());
                    seatTypeMap.forEach((seatType, ticketOrderPassengerDetailRespDTOList) -> {
                        stringRedisTemplate.opsForHash()
                                .increment(TRAIN_STATION_REMAINING_TICKET + keySuffix, String.valueOf(seatType), ticketOrderPassengerDetailRespDTOList.size());
                    });
                });
            } catch (Throwable ex) {
                log.error("[取消关闭订单] 订单号：{} 回滚列车Cache余票失败", requestParam.getOrderSn(), ex);
                throw ex;
            }
        }
    }

    @Override
    public RefundTicketRespDTO commonTicketRefund(RefundTicketReqDTO requestParam) {
        // 责任链模式，验证 1：参数必填
        refundReqDTOAbstractChainContext.handler(TicketChainMarkEnum.TRAIN_REFUND_TICKET_FILTER.name(), requestParam);
        Result<org.opengoofy.index12306.biz.ticketservice.remote.dto.TicketOrderDetailRespDTO> orderDetailRespDTOResult = ticketOrderRemoteService.queryTicketOrderByOrderSn(requestParam.getOrderSn());
        if (!orderDetailRespDTOResult.isSuccess() && Objects.isNull(orderDetailRespDTOResult.getData())) {
            throw new ServiceException("车票订单不存在");
        }
        org.opengoofy.index12306.biz.ticketservice.remote.dto.TicketOrderDetailRespDTO ticketOrderDetailRespDTO = orderDetailRespDTOResult.getData();
        List<TicketOrderPassengerDetailRespDTO> passengerDetails = ticketOrderDetailRespDTO.getPassengerDetails();
        if (CollectionUtil.isEmpty(passengerDetails)) {
            throw new ServiceException("车票子订单不存在");
        }
        RefundReqDTO refundReqDTO = new RefundReqDTO();
        if (RefundTypeEnum.PARTIAL_REFUND.getType().equals(requestParam.getType())) {
            TicketOrderItemQueryReqDTO ticketOrderItemQueryReqDTO = new TicketOrderItemQueryReqDTO();
            ticketOrderItemQueryReqDTO.setOrderSn(requestParam.getOrderSn());
            ticketOrderItemQueryReqDTO.setOrderItemRecordIds(requestParam.getSubOrderRecordIdReqList());
            Result<List<TicketOrderPassengerDetailRespDTO>> queryTicketItemOrderById = ticketOrderRemoteService.queryTicketItemOrderById(ticketOrderItemQueryReqDTO);
            List<TicketOrderPassengerDetailRespDTO> partialRefundPassengerDetails = passengerDetails.stream()
                    .filter(item -> queryTicketItemOrderById.getData().contains(item))
                    .collect(Collectors.toList());
            refundReqDTO.setRefundTypeEnum(RefundTypeEnum.PARTIAL_REFUND);
            refundReqDTO.setRefundDetailReqDTOList(partialRefundPassengerDetails);
        } else if (RefundTypeEnum.FULL_REFUND.getType().equals(requestParam.getType())) {
            refundReqDTO.setRefundTypeEnum(RefundTypeEnum.FULL_REFUND);
            refundReqDTO.setRefundDetailReqDTOList(passengerDetails);
        }
        if (CollectionUtil.isNotEmpty(passengerDetails)) {
            Integer partialRefundAmount = passengerDetails.stream()
                    .mapToInt(TicketOrderPassengerDetailRespDTO::getAmount)
                    .sum();
            refundReqDTO.setRefundAmount(partialRefundAmount);
        }
        refundReqDTO.setOrderSn(requestParam.getOrderSn());
        Result<RefundRespDTO> refundRespDTOResult = payRemoteService.commonRefund(refundReqDTO);
        if (!refundRespDTOResult.isSuccess() && Objects.isNull(refundRespDTOResult.getData())) {
            throw new ServiceException("车票订单退款失败");
        }
        return null; // 暂时返回空实体
    }

    private List<String> buildDepartureStationList(List<TicketListDTO> seatResults) {
        return seatResults.stream().map(TicketListDTO::getDeparture).distinct().collect(Collectors.toList());
    }

    private List<String> buildArrivalStationList(List<TicketListDTO> seatResults) {
        return seatResults.stream().map(TicketListDTO::getArrival).distinct().collect(Collectors.toList());
    }

    private List<Integer> buildSeatClassList(List<TicketListDTO> seatResults) {
        Set<Integer> resultSeatClassList = new HashSet<>();
        for (TicketListDTO each : seatResults) {
            for (SeatClassDTO item : each.getSeatClassList()) {
                resultSeatClassList.add(item.getType());
            }
        }
        return resultSeatClassList.stream().toList();
    }

    private List<Integer> buildTrainBrandList(List<TicketListDTO> seatResults) {
        Set<Integer> trainBrandSet = new HashSet<>();
        for (TicketListDTO each : seatResults) {
            if (StrUtil.isNotBlank(each.getTrainBrand())) {
                trainBrandSet.addAll(StrUtil.split(each.getTrainBrand(), ",").stream().map(Integer::parseInt).toList());
            }
        }
        return trainBrandSet.stream().toList();
    }

    private final ScheduledExecutorService tokenIsNullRefreshExecutor = Executors.newScheduledThreadPool(1);

    private void tokenIsNullRefreshToken(PurchaseTicketReqDTO requestParam, TokenResultDTO tokenResult) {
        RLock lock = redissonClient.getLock(String.format(LOCK_TOKEN_BUCKET_ISNULL, requestParam.getTrainId()));
        if (!lock.tryLock()) {
            return;
        }
        tokenIsNullRefreshExecutor.schedule(() -> {
            try {
                List<Integer> seatTypes = new ArrayList<>();
                Map<Integer, Integer> tokenCountMap = new HashMap<>();
                tokenResult.getTokenIsNullSeatTypeCounts().stream()
                        .map(each -> each.split("_"))
                        .forEach(split -> {
                            int seatType = Integer.parseInt(split[0]);
                            seatTypes.add(seatType);
                            tokenCountMap.put(seatType, Integer.parseInt(split[1]));
                        });
                List<SeatTypeCountDTO> seatTypeCountDTOList = seatService.listSeatTypeCount(Long.parseLong(requestParam.getTrainId()), requestParam.getDeparture(), requestParam.getArrival(), seatTypes);
                for (SeatTypeCountDTO each : seatTypeCountDTOList) {
                    Integer tokenCount = tokenCountMap.get(each.getSeatType());
                    if (tokenCount < each.getSeatCount()) {
                        ticketAvailabilityTokenBucket.delTokenInBucket(requestParam);
                        break;
                    }
                }
            } finally {
                lock.unlock();
            }
        }, 10, TimeUnit.SECONDS);
    }

    @Override
    public void run(String... args) throws Exception {
        ticketService = ApplicationContextHolder.getBean(TicketService.class);
    }

}
