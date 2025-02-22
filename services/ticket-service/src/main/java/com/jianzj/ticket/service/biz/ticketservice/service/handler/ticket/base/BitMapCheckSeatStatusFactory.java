package com.jianzj.ticket.service.biz.ticketservice.service.handler.ticket.base;

/**
 * @Author JianZJ
 * @Date 2025/2/22 20:26
 */

import com.jianzj.ticket.service.frameworks.starter.base.SinglePool;

/**
 * 座位通过 BitMap 检测抽象工厂
 */
public abstract class BitMapCheckSeatStatusFactory {

    public static final String TRAIN_BUSINESS = "TRAIN_BUSINESS";
    public static final String TRAIN_FIRST = "TRAIN_FIRST";
    public static final String TRAIN_SECOND = "TRAIN_SECOND";

    /**
     * 获取座位检查方法实例
     * @param mark 座位标识
     * @return 座位检查类
     */
    public static BitMapCheckSeat getInstance(String mark) {
        BitMapCheckSeat instance = null;
        switch (mark) {
            case TRAIN_BUSINESS -> {
                instance = SinglePool.get(TRAIN_BUSINESS);
                if (instance == null) {
                    instance = new TrainBusinessCheckSeat();
                    SinglePool.put(TRAIN_BUSINESS, instance);
                }
            }
            case TRAIN_FIRST -> {
                instance = SinglePool.get(TRAIN_FIRST);
                if (instance == null) {
                    instance = new TrainFirstCheckSeat();
                    SinglePool.put(TRAIN_FIRST, instance);
                }
            }
            case TRAIN_SECOND -> {
                instance = SinglePool.get(TRAIN_SECOND);
                if (instance == null) {
                    instance = new TrainSecondCheckSeat();
                    SinglePool.put(TRAIN_SECOND, instance);
                }
            }
        }
        return instance;
    }
}

