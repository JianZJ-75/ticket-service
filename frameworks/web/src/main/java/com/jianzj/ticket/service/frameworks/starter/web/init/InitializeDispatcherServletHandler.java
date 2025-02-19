package com.jianzj.ticket.service.frameworks.starter.web.init;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import static com.jianzj.ticket.service.frameworks.starter.web.config.ApplicationWebAutoConfiguration.INITIALIZE_PATH;

/**
 * @Author JianZJ
 * @Date 2025/2/19 9:35
 */

/**
 * DispatcherServlet预热执行器
 */
@RequiredArgsConstructor
public class InitializeDispatcherServletHandler implements CommandLineRunner {

    private final RestTemplate restTemplate;

    private final ConfigurableEnvironment configurableEnvironment;

    @Override
    public void run(String... args) throws Exception {
        String url = String.format("http://127.0.0.1:%s%s",
                configurableEnvironment.getProperty("server.port", "8080") + configurableEnvironment.getProperty("server.servlet.context-path", ""),
                INITIALIZE_PATH);
        try {
            restTemplate.execute(url, HttpMethod.GET, null, null);
        } catch (Throwable ignored) {
        }
    }
}
