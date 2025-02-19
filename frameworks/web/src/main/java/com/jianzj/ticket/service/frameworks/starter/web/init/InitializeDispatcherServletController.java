package com.jianzj.ticket.service.frameworks.starter.web.init;

/**
 * @Author JianZJ
 * @Date 2025/2/19 9:35
 */

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.jianzj.ticket.service.frameworks.starter.web.config.ApplicationWebAutoConfiguration.INITIALIZE_PATH;

/**
 * DispatcherServlet预热接口
 */
@Slf4j(topic = "Initialize DispatcherServlet")
@RestController
public class InitializeDispatcherServletController {

    @GetMapping(INITIALIZE_PATH)
    public void initializeDispatcherServlet() {
        log.info("Initialized the dispatcherServlet to improve the first response time of the interface...");
    }
}
