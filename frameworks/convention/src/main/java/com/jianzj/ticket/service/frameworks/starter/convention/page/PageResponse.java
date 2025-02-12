package com.jianzj.ticket.service.frameworks.starter.convention.page;

/**
 * @Author JianZJ
 * @Date 2025/2/13 0:01
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 请求响应对象
 * @param <T>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private Long current = 1L;

    /**
     * 每页显示数
     */
    private Long size = 10L;

    /**
     * 总数
     */
    private Long total;

    /**
     * 数据
     */
    private List<T> records = Collections.emptyList();

    public PageResponse(Long current, Long size, Long total) {
        if (current > 1) {
            this.current = current;
        }
        this.size = size;
        this.total = total;
    }

    public PageResponse(Long current, Long size) {
        this(current, size, 0L);
    }

    public PageResponse setRecords(List<T> records) {
        this.records = records;
        return this;
    }

    public <R> PageResponse<R> setRecords(Function<? super T, ? extends R> mapper) {
        List<R> collect = this.getRecords().stream().map(mapper).collect(Collectors.toList());
        return ((PageResponse<R>) this).setRecords(collect);
    }

}