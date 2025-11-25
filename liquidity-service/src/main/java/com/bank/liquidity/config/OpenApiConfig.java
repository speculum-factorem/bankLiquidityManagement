package com.bank.liquidity.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    // Конфигурация Swagger/OpenAPI для документации API сервиса ликвидности
    @Bean
    public OpenAPI liquidityServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Bank Liquidity Management API")
                .description("API для управления позициями ликвидности банка и алертами")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Bank Development Team")
                    .email("dev@bank.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://bank.com/license")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8081")
                    .description("Локальный сервер разработки"),
                new Server()
                    .url("http://api-gateway:8080")
                    .description("Сервер API Gateway")));
    }
}

