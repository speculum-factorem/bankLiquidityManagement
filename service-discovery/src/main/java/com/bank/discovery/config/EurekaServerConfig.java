package com.bank.discovery.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.eureka.server.EurekaServerConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EurekaServerConfig {

    @Bean
    public EurekaServerConfigBean eurekaServerConfig() {
        log.info("Configuring Eureka Server");

        EurekaServerConfigBean config = new EurekaServerConfigBean();
        config.setWaitTimeInMsWhenSyncEmpty(0);
        config.setPeerNodeReadTimeoutMs(20000);
        config.setEnableSelfPreservation(true);
        config.setRenewalPercentThreshold(0.85);

        return config;
    }
}