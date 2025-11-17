package com.bank.risk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class RiskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskServiceApplication.class, args);
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Risk Service started successfully");
        log.info("Risk Service is ready to process risk assessments");
    }
}