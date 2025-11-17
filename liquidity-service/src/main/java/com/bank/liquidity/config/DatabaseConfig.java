package com.bank.liquidity.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.bank.liquidity.repository")
public class DatabaseConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dataSource) {
        log.info("Configuring JPA Entity Manager Factory");
        return builder
                .dataSource(dataSource)
                .packages("com.bank.liquidity.model")
                .persistenceUnit("liquidityPU")
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }
}