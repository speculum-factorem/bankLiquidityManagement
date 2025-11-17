package com.bank.risk.config;

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
@EnableJpaRepositories(basePackages = "com.bank.risk.repository")
public class DatabaseConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dataSource) {
        log.info("Configuring JPA Entity Manager Factory for Risk Service");
        return builder
                .dataSource(dataSource)
                .packages("com.bank.risk.model")
                .persistenceUnit("riskPU")
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