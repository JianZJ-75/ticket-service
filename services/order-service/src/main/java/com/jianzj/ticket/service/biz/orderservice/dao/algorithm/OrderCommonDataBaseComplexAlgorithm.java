package com.jianzj.ticket.service.biz.orderservice.dao.algorithm;

/**
 * @Author JianZJ
 * @Date 2025/2/23 0:08
 */

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

/**
 * 订单数据库复合分片算法配置
 */
public class OrderCommonDataBaseComplexAlgorithm implements ComplexKeysShardingAlgorithm {

    @Getter
    private Properties props;

    private int shardingCount;
    private int tableShardingCount;

    private static final String SHARDING_COUNT_KEY = "sharding-count";
    private static final String TABLE_SHARDING_COUNT_KEY = "table-sharding-count";

    @Override
    public Collection<String> doSharding(Collection availableTargetNames, ComplexKeysShardingValue shardingValue) {
        // 从 shardingValue 中获取分片键名和对应的值的映射
        Map<String, Collection<Comparable<?>>> columnNameAndShardingValuesMap = shardingValue.getColumnNameAndShardingValuesMap();
        // 初始化结果集合，使用 LinkedHashSet 保证顺序且元素唯一
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        // 检查分片键名和对应值的映射是否不为空
        if (CollUtil.isNotEmpty(columnNameAndShardingValuesMap)) {
            // 定义用户 ID 作为分片键
            String userId = "user_id";
            // 从映射中获取用户 ID 对应的分片值集合
            Collection<Comparable<?>> customerUserIdCollection = columnNameAndShardingValuesMap.get(userId);
            // 检查用户 ID 对应的分片值集合是否不为空
            if (CollUtil.isNotEmpty(customerUserIdCollection)) {
                String dbSuffix;
                // 获取用户 ID 集合中的第一个值
                Comparable<?> comparable = customerUserIdCollection.stream().findFirst().get();
                // 判断该值是否为 String 类型
                if (comparable instanceof String) {
                    // 将值转换为字符串
                    String actualUserId = comparable.toString();
                    // 取用户 ID 的最后 6 位进行哈希计算，然后对分库数量取模，再除以每个库中表的分片数量
                    // 得到数据库后缀
                    dbSuffix = String.valueOf(hashShardingValue(actualUserId.substring(Math.max(actualUserId.length() - 6, 0))) % shardingCount / tableShardingCount);
                } else {
                    dbSuffix = String.valueOf(hashShardingValue((Long) comparable % 1000000) % shardingCount / tableShardingCount);
                }
                result.add("ds_" + dbSuffix);
            } else {
                // 如果用户 ID 对应的分片值集合为空，使用订单编号作为分片键
                String orderSn = "order_sn";
                String dbSuffix;
                Collection<Comparable<?>> orderSnCollection = columnNameAndShardingValuesMap.get(orderSn);
                Comparable<?> comparable = orderSnCollection.stream().findFirst().get();
                if (comparable instanceof String) {
                    String actualOrderSn = comparable.toString();
                    dbSuffix = String.valueOf(hashShardingValue(actualOrderSn.substring(Math.max(actualOrderSn.length() - 6, 0))) % shardingCount / tableShardingCount);
                } else {
                    dbSuffix = String.valueOf(hashShardingValue((Long) comparable % 1000000) % shardingCount / tableShardingCount);
                }
                result.add("ds_" + dbSuffix);
            }
        }
        return result;
    }

    @Override
    public void init(Properties props) {
        this.props = props;
        shardingCount = getShardingCount(props);
        tableShardingCount = getTableShardingCount(props);
    }

    private int getShardingCount(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(SHARDING_COUNT_KEY), () -> new ShardingAlgorithmInitializationException(getType(), "Sharding count cannot be null."));
        return Integer.parseInt(props.getProperty(SHARDING_COUNT_KEY));
    }

    private int getTableShardingCount(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(TABLE_SHARDING_COUNT_KEY), () -> new ShardingAlgorithmInitializationException(getType(), "Table sharding count cannot be null."));
        return Integer.parseInt(props.getProperty(TABLE_SHARDING_COUNT_KEY));
    }

    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }

    @Override
    public String getType() {
        return "CLASS_BASED";
    }
}
