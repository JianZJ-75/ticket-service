package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.tokenbucket;

/**
 * @Author JianZJ
 * @Date 2025/2/23 4:37
 */

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.VehicleTypeEnum;
import com.jianzj.ticket.service.biz.ticketservice.dao.entity.TrainDO;
import com.jianzj.ticket.service.biz.ticketservice.dao.mapper.TrainMapper;
import com.jianzj.ticket.service.biz.ticketservice.dto.domain.RouteDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.domain.SeatTypeCountDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import com.jianzj.ticket.service.biz.ticketservice.remote.dto.TicketOrderDetailRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.remote.dto.TicketOrderPassengerDetailRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.SeatService;
import com.jianzj.ticket.service.biz.ticketservice.service.TrainStationService;
import com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.dto.TokenResultDTO;
import com.jianzj.ticket.service.frameworks.starter.base.SinglePool;
import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.jianzj.ticket.service.biz.ticketservice.dto.domain.PurchaseTicketPassengerDetailDTO;
import static com.jianzj.ticket.service.biz.ticketservice.common.constant.RedisKeyConstant.*;
import static com.jianzj.ticket.service.biz.ticketservice.common.constant.TicketServiceConstant.ADVANCE_TICKET_DAY;

/**
 * 列车车票余量令牌桶，应对海量并发场景下满足并行、限流以及防超卖等场景
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class TicketAvailabilityTokenBucket {

    private final TrainStationService trainStationService;
    private final DistributedCache distributedCache;
    private final RedissonClient redissonClient;
    private final SeatService seatService;
    private final TrainMapper trainMapper;

    private static final String LUA_TICKET_AVAILABILITY_TOKEN_BUCKET_PATH = "lua/ticket_availability_token_bucket.lua";
    private static final String LUA_TICKET_AVAILABILITY_ROLLBACK_TOKEN_BUCKET_PATH = "lua/ticket_availability_rollback_token_bucket.lua";

    /**
     * 获取车站间令牌桶中的令牌访问
     * 如果返回 {@link Boolean#TRUE} 代表可以参与接下来的购票下单流程
     * 如果返回 {@link Boolean#FALSE} 代表当前访问出发站点和到达站点令牌已被拿完，无法参与购票下单等逻辑
     * @param requestParam 购票请求参数入参
     * @return 是否获取列车车票余量令牌桶中的令牌返回结果
     */
    public TokenResultDTO takeTokenFromBucket(PurchaseTicketReqDTO requestParam) {
        // 从缓存中获取列车信息，如果缓存中不存在，则从数据库中查询并缓存
        TrainDO trainDO = distributedCache.safeGet(
                TRAIN_INFO + requestParam.getTrainId(),
                TrainDO.class,
                () -> trainMapper.selectById(requestParam.getTrainId()),
                ADVANCE_TICKET_DAY,
                TimeUnit.DAYS);
        // 获取列车经过的所有站点路线信息
        List<RouteDTO> routeDTOList = trainStationService
                .listTrainStationRoute(requestParam.getTrainId(), trainDO.getStartStation(), trainDO.getEndStation());
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        // 构建令牌桶的哈希键
        String tokenBucketHashKey = TICKET_AVAILABILITY_TOKEN_BUCKET + requestParam.getTrainId();
        // 检查令牌桶是否存在
        Boolean hasKey = distributedCache.hasKey(tokenBucketHashKey);
        if (!hasKey) {
            // 如果令牌桶不存在，获取分布式锁
            RLock lock = redissonClient.getLock(String.format(LOCK_TICKET_AVAILABILITY_TOKEN_BUCKET, requestParam.getTrainId()));
            if (!lock.tryLock()) {
                throw new ServiceException("购票异常，请稍候再试");
            }
            try {
                // 再次检查令牌桶是否存在，避免其他线程已经创建
                Boolean hasKeyTwo = distributedCache.hasKey(tokenBucketHashKey);
                if (!hasKeyTwo) {
                    // 获取列车支持的座位类型
                    List<Integer> seatTypes = VehicleTypeEnum.findSeatTypesByCode(trainDO.getTrainType());
                    // 用于存储令牌桶的初始数据
                    Map<String, String> ticketAvailabilityTokenMap = new HashMap<>();
                    for (RouteDTO each : routeDTOList) {
                        // 获取每个站点区间的座位类型和数量信息
                        List<SeatTypeCountDTO> seatTypeCountDTOList = seatService.listSeatTypeCount(Long.parseLong(requestParam.getTrainId()), each.getStartStation(), each.getEndStation(), seatTypes);
                        for (SeatTypeCountDTO eachSeatTypeCountDTO : seatTypeCountDTOList) {
                            // 构建缓存键，格式为 起始站_终点站_座位类型
                            String buildCacheKey = StrUtil.join("_", each.getStartStation(), each.getEndStation(), eachSeatTypeCountDTO.getSeatType());
                            // 将座位数量存入令牌桶数据中
                            ticketAvailabilityTokenMap.put(buildCacheKey, String.valueOf(eachSeatTypeCountDTO.getSeatCount()));
                        }
                    }
                    // 将令牌桶数据存入 Redis
                    stringRedisTemplate.opsForHash().putAll(TICKET_AVAILABILITY_TOKEN_BUCKET + requestParam.getTrainId(), ticketAvailabilityTokenMap);
                }
            } finally {
                lock.unlock();
            }
        }
        // 获取获取令牌的 Lua 脚本
        DefaultRedisScript<String> actual = SinglePool.get(LUA_TICKET_AVAILABILITY_TOKEN_BUCKET_PATH, () -> {
            DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_TICKET_AVAILABILITY_TOKEN_BUCKET_PATH)));
            redisScript.setResultType(String.class);
            return redisScript;
        });
        // 确保脚本不为空
        Assert.notNull(actual);
        // 统计每个座位类型的乘客数量
        Map<Integer, Long> seatTypeCountMap = requestParam.getPassengers().stream()
                .collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType, Collectors.counting()));
        // 将座位类型和数量信息转换为 JSON 数组
        JSONArray seatTypeCountArray = seatTypeCountMap.entrySet().stream()
                .map(entry -> {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("seatType", String.valueOf(entry.getKey()));
                    jsonObject.put("count", String.valueOf(entry.getValue()));
                    return jsonObject;
                })
                .collect(Collectors.toCollection(JSONArray::new));
        // 获取用户购票的站点区间路线信息
        List<RouteDTO> takeoutRouteDTOList = trainStationService
                .listTakeoutTrainStationRoute(requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival());
        // 构建 Lua 脚本的键
        String luaScriptKey = StrUtil.join("_", requestParam.getDeparture(), requestParam.getArrival());
        // 执行 Lua 脚本，尝试获取令牌
        String resultStr = stringRedisTemplate.execute(actual, Lists.newArrayList(tokenBucketHashKey, luaScriptKey), JSON.toJSONString(seatTypeCountArray), JSON.toJSONString(takeoutRouteDTOList));
        // 将脚本执行结果转换为 TokenResultDTO 对象
        TokenResultDTO result = JSON.parseObject(resultStr, TokenResultDTO.class);
        // 如果结果为空，返回令牌为空的结果
        return result == null
                ? TokenResultDTO.builder().tokenIsNull(Boolean.TRUE).build()
                : result;
    }

    /**
     * 回滚列车余量令牌，一般为订单取消或长时间未支付触发
     * @param requestParam 回滚列车余量令牌入参
     */
    public void rollbackInBucket(TicketOrderDetailRespDTO requestParam) {
        // 获取回滚令牌的 Lua 脚本
        DefaultRedisScript<Long> actual = SinglePool.get(LUA_TICKET_AVAILABILITY_ROLLBACK_TOKEN_BUCKET_PATH, () -> {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(LUA_TICKET_AVAILABILITY_ROLLBACK_TOKEN_BUCKET_PATH)));
            redisScript.setResultType(Long.class);
            return redisScript;
        });
        Assert.notNull(actual);
        // 统计每个座位类型的乘客数量
        List<TicketOrderPassengerDetailRespDTO> passengerDetails = requestParam.getPassengerDetails();
        Map<Integer, Long> seatTypeCountMap = passengerDetails.stream()
                .collect(Collectors.groupingBy(TicketOrderPassengerDetailRespDTO::getSeatType, Collectors.counting()));
        // 将座位类型和数量信息转换为 JSON 数组
        JSONArray seatTypeCountArray = seatTypeCountMap.entrySet().stream()
                .map(entry -> {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("seatType", String.valueOf(entry.getKey()));
                    jsonObject.put("count", String.valueOf(entry.getValue()));
                    return jsonObject;
                })
                .collect(Collectors.toCollection(JSONArray::new));
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        String actualHashKey = TICKET_AVAILABILITY_TOKEN_BUCKET + requestParam.getTrainId();
        String luaScriptKey = StrUtil.join("_", requestParam.getDeparture(), requestParam.getArrival());
        List<RouteDTO> takeoutRouteDTOList = trainStationService.listTakeoutTrainStationRoute(String.valueOf(requestParam.getTrainId()), requestParam.getDeparture(), requestParam.getArrival());
        Long result = stringRedisTemplate.execute(actual, Lists.newArrayList(actualHashKey, luaScriptKey), JSON.toJSONString(seatTypeCountArray), JSON.toJSONString(takeoutRouteDTOList));
        if (result == null || !Objects.equals(result, 0L)) {
            // 如果回滚失败，记录错误日志并抛出异常
            log.error("回滚列车余票令牌失败，订单信息：{}", JSON.toJSONString(requestParam));
            throw new ServiceException("回滚列车余票令牌失败");
        }
    }

    /**
     * 删除令牌，一般在令牌与数据库不一致情况下触发
     *
     * @param requestParam 删除令牌容器参数
     */
    public void delTokenInBucket(PurchaseTicketReqDTO requestParam) {
        StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
        String tokenBucketHashKey = TICKET_AVAILABILITY_TOKEN_BUCKET + requestParam.getTrainId();
        stringRedisTemplate.delete(tokenBucketHashKey);
    }

    public void putTokenInBucket() {

    }

    public void initializeTokens() {

    }
}

