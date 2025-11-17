package com.bank.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Service Discovery (Eureka Server) started successfully on port: 8761");
        log.info("Service Discovery is ready to register microservices");
    }
}