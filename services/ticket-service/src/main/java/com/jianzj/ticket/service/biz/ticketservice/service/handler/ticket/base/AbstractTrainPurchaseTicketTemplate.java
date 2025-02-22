package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.base;

/**
 * @Author JianZJ
 * @Date 2025/2/22 20:15
 */

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.jianzj.ticket.service.biz.ticketservice.dto.domain.RouteDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.domain.TrainSeatBaseDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.TrainStationService;
import com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.dto.SelectSeatDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;
import com.jianzj.ticket.service.frameworks.starter.base.ApplicationContextHolder;
import com.jianzj.ticket.service.frameworks.starter.cache.DistributedCache;
import com.jianzj.ticket.service.frameworks.starter.designpattern.strategy.AbstractExecuteStrategy;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static com.jianzj.ticket.service.biz.ticketservice.common.constant.RedisKeyConstant.TRAIN_STATION_REMAINING_TICKET;

/**
 * 抽象高铁购票模板基础服务
 */
public abstract class AbstractTrainPurchaseTicketTemplate implements IPurchaseTicket, CommandLineRunner, AbstractExecuteStrategy<SelectSeatDTO, List<TrainPurchaseTicketRespDTO>> {

    private DistributedCache distributedCache;
    private String ticketAvailabilityCacheUpdateType;
    private TrainStationService trainStationService;

    /**
     * 选择座位
     * @param requestParam 购票请求入参
     * @return 乘车人座位
     */
    protected abstract List<TrainPurchaseTicketRespDTO> selectSeats(SelectSeatDTO requestParam);

    /**
     * 构建列车座位基础信息 DTO
     * 从购票请求参数中提取必要信息，封装到 TrainSeatBaseDTO 对象中
     * @param requestParam 购票请求入参
     * @return 列车座位基础信息 DTO
     */
    protected TrainSeatBaseDTO buildTrainSeatBaseDTO(SelectSeatDTO requestParam) {
        return TrainSeatBaseDTO.builder()
                .trainId(requestParam.getRequestParam().getTrainId())
                .departure(requestParam.getRequestParam().getDeparture())
                .arrival(requestParam.getRequestParam().getArrival())
                .chooseSeatList(requestParam.getRequestParam().getChooseSeats())
                .passengerSeatDetails(requestParam.getPassengerSeatDetails())
                .build();
    }

    /**
     * 执行购票响应逻辑
     * 调用 selectSeats 方法选择座位，并根据情况更新车票可用性缓存
     * @param requestParam 购票请求入参
     * @return 乘车人座位信息列表
     */
    @Override
    public List<TrainPurchaseTicketRespDTO> executeResp(SelectSeatDTO requestParam) {
        List<TrainPurchaseTicketRespDTO> actualResult = selectSeats(requestParam);
        // 扣减车厢余票缓存，扣减站点余票缓存
        if (CollUtil.isNotEmpty(actualResult) && !StrUtil.equals(ticketAvailabilityCacheUpdateType, "binlog")) {
            String trainId = requestParam.getRequestParam().getTrainId();
            String departure = requestParam.getRequestParam().getDeparture();
            String arrival = requestParam.getRequestParam().getArrival();
            StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();
            List<RouteDTO> routeDTOList = trainStationService.listTakeoutTrainStationRoute(trainId, departure, arrival);
            routeDTOList.forEach(each -> {
                String keySuffix = StrUtil.join("_", trainId, each.getStartStation(), each.getEndStation());
                // 从 Redis 缓存中获取对应路线的车票剩余数量，并根据选择的座位数量进行扣减
                stringRedisTemplate.opsForHash().increment(TRAIN_STATION_REMAINING_TICKET + keySuffix, String.valueOf(requestParam.getSeatType()), -actualResult.size());
            });
        }
        return actualResult;
    }

    @Override
    public void run(String... args) throws Exception {
        distributedCache = ApplicationContextHolder.getBean(DistributedCache.class);
        trainStationService = ApplicationContextHolder.getBean(TrainStationService.class);
        ConfigurableEnvironment configurableEnvironment = ApplicationContextHolder.getBean(ConfigurableEnvironment.class);
        ticketAvailabilityCacheUpdateType = configurableEnvironment.getProperty("ticket.availability.cache-update.type", "");
    }
}
