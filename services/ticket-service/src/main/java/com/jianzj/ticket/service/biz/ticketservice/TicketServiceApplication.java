package com.jianzj.ticket.service.biz.ticketservice;

import cn.hippo4j.core.enable.EnableDynamicThreadPool;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 购票服务启动器
 */
@SpringBootApplication
@EnableDynamicThreadPool
@MapperScan("com.jianzj.ticket.service.biz.ticketservice.dao.mapper")
@EnableFeignClients("com.jianzj.ticket.service.biz.ticketservice.remote")
public class TicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
    }

}
