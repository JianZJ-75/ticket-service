package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.select;

/**
 * @Author JianZJ
 * @Date 2025/2/22 16:17
 */

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.VehicleSeatTypeEnum;
import com.jianzj.ticket.service.biz.ticketservice.common.enums.VehicleTypeEnum;
import com.jianzj.ticket.service.biz.ticketservice.dao.entity.TrainStationPriceDO;
import com.jianzj.ticket.service.biz.ticketservice.dao.mapper.TrainStationPriceMapper;
import com.jianzj.ticket.service.biz.ticketservice.dto.domain.PurchaseTicketPassengerDetailDTO;
import com.jianzj.ticket.service.biz.ticketservice.dto.req.PurchaseTicketReqDTO;
import com.jianzj.ticket.service.biz.ticketservice.remote.UserRemoteService;
import com.jianzj.ticket.service.biz.ticketservice.remote.dto.PassengerRespDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.SeatService;
import com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.dto.SelectSeatDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.dto.TrainPurchaseTicketRespDTO;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.RemoteException;
import com.jianzj.ticket.service.frameworks.starter.convention.exception.ServiceException;
import com.jianzj.ticket.service.frameworks.starter.convention.result.Result;
import com.jianzj.ticket.service.frameworks.starter.designpattern.strategy.AbstractStrategyChoose;
import com.jianzj.ticket.service.frameworks.starter.user.core.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 购票时列车座位选择器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class TrainSeatTypeSelector {

    private final SeatService seatService;
    private final UserRemoteService userRemoteService;
    private final TrainStationPriceMapper trainStationPriceMapper;
    private final AbstractStrategyChoose abstractStrategyChoose;
    private final ThreadPoolExecutor selectSeatThreadPoolExecutor;

    public List<TrainPurchaseTicketRespDTO> select(Integer trainType, PurchaseTicketReqDTO requestParam) {
        // 获取请求中的乘客详情列表
        List<PurchaseTicketPassengerDetailDTO> passengerDetails = requestParam.getPassengers();
        // 根据座位类型对乘客详情进行分组
        Map<Integer, List<PurchaseTicketPassengerDetailDTO>> seatTypeMap = passengerDetails.stream()
                .collect(Collectors.groupingBy(PurchaseTicketPassengerDetailDTO::getSeatType));
        // 创建一个同步列表，用于存储最终的购票结果
        List<TrainPurchaseTicketRespDTO> actualResult = Collections.synchronizedList(new ArrayList<>(seatTypeMap.size()));
        // 如果有多种座位类型
        if (seatTypeMap.size() > 1) {
            // 用于存储异步任务的未来结果
            List<Future<List<TrainPurchaseTicketRespDTO>>> futureResults = new ArrayList<>(seatTypeMap.size());
            // 遍历每种座位类型的乘客详情
            seatTypeMap.forEach((seatType, passengerSeatDetails) -> {
                // 提交异步任务到线程池，进行座位分配
                Future<List<TrainPurchaseTicketRespDTO>> completableFuture = selectSeatThreadPoolExecutor
                        .submit(() -> distributeSeats(trainType, seatType, requestParam, passengerSeatDetails));
                futureResults.add(completableFuture);
            });
            // 并行处理异步任务的结果
            futureResults.parallelStream().forEach(completableFuture -> {
                try {
                    // 将每个异步任务的结果添加到最终结果列表中
                    actualResult.addAll(completableFuture.get());
                } catch (Exception e) {
                    throw new ServiceException("站点余票不足，请尝试更换座位类型或选择其它站点");
                }
            });
        } else {
            // 如果只有一种座位类型，直接进行座位分配
            seatTypeMap.forEach((seatType, passengerSeatDetails) -> {
                List<TrainPurchaseTicketRespDTO> aggregationResult = distributeSeats(trainType, seatType, requestParam, passengerSeatDetails);
                actualResult.addAll(aggregationResult);
            });
        }
        // 检查最终结果是否为空或结果数量与乘客数量不匹配
        if (CollUtil.isEmpty(actualResult) || !Objects.equals(actualResult.size(), passengerDetails.size())) {
            throw new ServiceException("站点余票不足，请尝试更换座位类型或选择其它站点");
        }
        // 从最终结果中提取乘客 ID 列表
        List<String> passengerIds = actualResult.stream()
                .map(TrainPurchaseTicketRespDTO::getPassengerId)
                .collect(Collectors.toList());
        // 调用远程服务，查询乘客信息
        Result<List<PassengerRespDTO>> passengerRemoteResult;
        List<PassengerRespDTO> passengerRemoteResultList;
        try {
            passengerRemoteResult = userRemoteService.listPassengerQueryByIds(UserContext.getUsername(), passengerIds);
            if (!passengerRemoteResult.isSuccess() || CollUtil.isEmpty(passengerRemoteResultList = passengerRemoteResult.getData())) {
                throw new RemoteException("用户服务远程调用查询乘车人相关信息错误");
            }
        } catch (Throwable ex) {
            if (ex instanceof RemoteException) {
                log.error("用户服务远程调用查询乘车人相关信息错误，当前用户：{}，请求参数：{}", UserContext.getUsername(), passengerIds);
            } else {
                log.error("用户服务远程调用查询乘车人相关信息错误，当前用户：{}，请求参数：{}", UserContext.getUsername(), passengerIds, ex);
            }
            throw ex;
        }
        // 将查询到的乘客信息补充到最终结果中
        actualResult.forEach(each -> {
            String passengerId = each.getPassengerId();
            passengerRemoteResultList.stream()
                    .filter(item -> Objects.equals(item.getId(), passengerId))
                    .findFirst()
                    .ifPresent(passenger -> {
                        each.setIdCard(passenger.getIdCard());
                        each.setPhone(passenger.getPhone());
                        each.setUserType(passenger.getDiscountType());
                        each.setIdType(passenger.getIdType());
                        each.setRealName(passenger.getRealName());
                    });
            // 查询火车票价格信息
            LambdaQueryWrapper<TrainStationPriceDO> lambdaQueryWrapper = Wrappers.lambdaQuery(TrainStationPriceDO.class)
                    .eq(TrainStationPriceDO::getTrainId, requestParam.getTrainId())
                    .eq(TrainStationPriceDO::getDeparture, requestParam.getDeparture())
                    .eq(TrainStationPriceDO::getArrival, requestParam.getArrival())
                    .eq(TrainStationPriceDO::getSeatType, each.getSeatType())
                    .select(TrainStationPriceDO::getPrice);
            TrainStationPriceDO trainStationPriceDO = trainStationPriceMapper.selectOne(lambdaQueryWrapper);
            each.setAmount(trainStationPriceDO.getPrice());
        });
        // 锁定已分配的座位
        seatService.lockSeat(requestParam.getTrainId(), requestParam.getDeparture(), requestParam.getArrival(), actualResult);
        return actualResult;
    }

    private List<TrainPurchaseTicketRespDTO> distributeSeats(Integer trainType, Integer seatType, PurchaseTicketReqDTO requestParam, List<PurchaseTicketPassengerDetailDTO> passengerSeatDetails) {
        // 构建策略键
        String buildStrategyKey = VehicleTypeEnum.findNameByCode(trainType) + VehicleSeatTypeEnum.findNameByCode(seatType);
        // 创建座位选择 DTO
        SelectSeatDTO selectSeatDTO = SelectSeatDTO.builder()
                .seatType(seatType)
                .passengerSeatDetails(passengerSeatDetails)
                .requestParam(requestParam)
                .build();
        try {
            return abstractStrategyChoose.chooseAndExecuteResp(buildStrategyKey, selectSeatDTO);
        } catch (ServiceException ex) {
            throw new ServiceException("当前车次列车类型暂未适配，请购买G35或G39车次");
        }
    }
}