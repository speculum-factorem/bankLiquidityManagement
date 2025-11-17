package com.bank.transaction.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic transactionTopic() {
        log.info("Creating Kafka topic for transactions");
        return TopicBuilder.name("transactions")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionAlertsTopic() {
        log.info("Creating Kafka topic for transaction alerts");
        return TopicBuilder.name("transaction-alerts")
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic highValueTransactionsTopic() {
        log.info("Creating Kafka topic for high-value transactions");
        return TopicBuilder.name("high-value-transactions")
                .partitions(2)
                .replicas(1)
                .build();
    }
}