package com.bank.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Transaction Service started successfully");
        log.info("Transaction Service is ready to process financial transactions");
    }
}