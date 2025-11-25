package com.bank.risk.config;

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

    // Конфигурация Swagger/OpenAPI для документации API сервиса оценки рисков
    @Bean
    public OpenAPI riskServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Bank Risk Assessment API")
                .description("API для оценки и управления рисками банка")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Bank Development Team")
                    .email("dev@bank.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://bank.com/license")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8083")
                    .description("Локальный сервер разработки"),
                new Server()
                    .url("http://api-gateway:8080")
                    .description("Сервер API Gateway")));
    }
}

