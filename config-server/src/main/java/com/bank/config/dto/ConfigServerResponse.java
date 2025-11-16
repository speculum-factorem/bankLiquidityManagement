package com.bank.config.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigServerResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String timestamp;
    private Map<String, Object> metadata;

    public static <T> ConfigServerResponse<T> success(T data) {
        return ConfigServerResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static <T> ConfigServerResponse<T> success(T data, String message) {
        return ConfigServerResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static <T> ConfigServerResponse<T> error(String message) {
        return ConfigServerResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static <T> ConfigServerResponse<T> error(String message, Map<String, Object> metadata) {
        return ConfigServerResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .metadata(metadata)
                .build();
    }
}