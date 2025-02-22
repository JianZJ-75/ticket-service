package com.jianzj.ticket.service.biz.ticketservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jianzj.ticket.service.biz.ticketservice.dao.entity.TrainStationDO;
import com.jianzj.ticket.service.biz.ticketservice.dao.mapper.TrainStationMapper;
import com.jianzj.ticket.service.biz.ticketservice.dto.domain.RouteDTO;
import com.jianzj.ticket.service.biz.ticketservice.service.TrainStationService;
import com.jianzj.ticket.service.biz.ticketservice.tools.StationCalculateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author JianZJ
 * @Date 2025/2/22 12:51
 */

/**
 * 列车站点接口实现层
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainStationServiceImpl implements TrainStationService {

    private final TrainStationMapper trainStationMapper;

    @Override
    public List<TrainStationQueryRespDTO> listTrainStationQuery(String trainId) {
        return null;
    }

    @Override
    public List<RouteDTO> listTrainStationRoute(String trainId, String departure, String arrival) {
        LambdaQueryWrapper<TrainStationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationDO.class)
                .eq(TrainStationDO::getTrainId, trainId)
                .select(TrainStationDO::getDeparture, TrainStationDO::getSequence);
        List<TrainStationDO> trainStationDOList = trainStationMapper.selectList(queryWrapper);
        List<String> trainStationAllList = trainStationDOList.stream().sorted(new Comparator<TrainStationDO>() {
            @Override
            public int compare(TrainStationDO o1, TrainStationDO o2) {
                return (int) (Long.parseLong(o2.getSequence()) - Long.parseLong(o1.getSequence()));
            }
        }).map(TrainStationDO::getDeparture).collect(Collectors.toList());
        return StationCalculateUtil.throughStation(trainStationAllList, departure, arrival);
    }

    @Override
    public List<RouteDTO> listTakeoutTrainStationRoute(String trainId, String departure, String arrival) {
        return null;
    }
}
