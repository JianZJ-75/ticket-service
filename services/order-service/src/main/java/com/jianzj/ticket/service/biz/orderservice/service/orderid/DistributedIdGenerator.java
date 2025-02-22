package com.jianzj.ticket.service.biz.orderservice.service.orderid;

/**
 * @Author JianZJ
 * @Date 2025/2/23 0:33
 */

/**
 * 全局唯一订单号生成器
 */
public class DistributedIdGenerator {

    // 定义一个常量 EPOCH，它是一个时间戳，表示自定义的起始时间（2021-01-01 00:00:00），单位为毫秒
    // 后续生成的 ID 中的时间戳部分是相对于这个起始时间的偏移量
    private static final long EPOCH = 1609459200000L;
    // 定义节点 ID 所占的位数，这里设置为 5 位，意味着最多可以支持 2^5 = 32 个不同的节点
    private static final int NODE_BITS = 5;
    // 定义序列号所占的位数，这里设置为 7 位，意味着在同一毫秒内，每个节点最多可以生成 2^7 = 128 个不同的 ID
    private static final int SEQUENCE_BITS = 7;

    private final long nodeID;
    private long lastTimestamp = -1L;
    private long sequence = 0L;

    public DistributedIdGenerator(long nodeID) {
        this.nodeID = nodeID;
    }

    /**
     * 生成唯一的分布式 ID 的方法，使用 synchronized 关键字保证线程安全
     * @return 生成的唯一分布式 ID
     */
    public synchronized long generateId() {
        // 获取当前时间戳，并减去起始时间 EPOCH，得到相对于起始时间的偏移量
        long timestamp = System.currentTimeMillis() - EPOCH;
        // 检查当前时间戳是否小于上一次生成 ID 时的时间戳
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate ID.");
        }
        // 如果当前时间戳等于上一次生成 ID 时的时间戳，说明在同一毫秒内生成 ID
        if (timestamp == lastTimestamp) {
            // 序列号加 1，并通过按位与操作确保序列号不会超过 2^SEQUENCE_BITS - 1
            sequence = (sequence + 1) & ((1 << SEQUENCE_BITS) - 1);
            // 如果序列号达到了最大值（即 0），说明在这一毫秒内已经生成了 2^SEQUENCE_BITS 个 ID
            if (sequence == 0) {
                // 调用 tilNextMillis 方法等待到下一个毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 如果当前时间戳大于上一次生成 ID 时的时间戳，说明进入了新的毫秒
            // 将序列号重置为 0
            sequence = 0L;
        }
        // 更新上一次生成 ID 时的时间戳
        lastTimestamp = timestamp;
        // 通过位运算将时间戳、节点 ID 和序列号组合成一个唯一的 ID
        // 时间戳左移 (NODE_BITS + SEQUENCE_BITS) 位，确保时间戳占据高位
        // 节点 ID 左移 SEQUENCE_BITS 位，放置在中间位置
        // 序列号放在最低位
        // 最后通过按位或操作将它们组合在一起
        return (timestamp << (NODE_BITS + SEQUENCE_BITS)) | (nodeID << SEQUENCE_BITS) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        // 获取当前时间戳，并减去起始时间 EPOCH
        long timestamp = System.currentTimeMillis() - EPOCH;
        // 循环检查当前时间戳是否小于等于上一次生成 ID 时的时间戳
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis() - EPOCH;
        }
        // 当当前时间戳大于上一次生成 ID 时的时间戳，返回该时间戳
        return timestamp;
    }
}
