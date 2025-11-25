package com.bank.liquidity.controller;

import com.bank.liquidity.dto.ApiResponse;
import com.bank.liquidity.dto.LiquidityPositionRequest;
import com.bank.liquidity.dto.LiquidityPositionResponse;
import com.bank.liquidity.service.LiquidityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/liquidity")
@RequiredArgsConstructor
@Tag(name = "Liquidity Management", description = "API for managing bank liquidity positions")
public class LiquidityController {

    private final LiquidityService liquidityService;

    @Operation(summary = "Создать или обновить позицию ликвидности", 
               description = "Создает новую позицию ликвидности или обновляет существующую для филиала и валюты")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Позиция успешно создана/обновлена"),
        @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/positions")
    public ResponseEntity<ApiResponse<LiquidityPositionResponse>> createPosition(
            @Valid @RequestBody LiquidityPositionRequest request) {

        log.info("Creating liquidity position for branch: {}", request.getBranchCode());

        long startTime = System.currentTimeMillis();
        try {
            LiquidityPositionResponse response = liquidityService.createPosition(request);

            log.info("Liquidity position created successfully for branch: {}", request.getBranchCode());

            return ResponseEntity.ok(ApiResponse.success(response, "Liquidity position created successfully"));

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Position creation completed in {}ms", duration);
        }
    }

    @Operation(summary = "Получить все позиции ликвидности", 
               description = "Возвращает все позиции ликвидности по всем филиалам")
    @GetMapping("/positions")
    public ResponseEntity<ApiResponse<List<LiquidityPositionResponse>>> getAllPositions() {
        log.debug("Fetching all liquidity positions");

        List<LiquidityPositionResponse> positions = liquidityService.getAllPositions();

        log.debug("Retrieved {} liquidity positions", positions.size());

        return ResponseEntity.ok(ApiResponse.success(positions));
    }

    @GetMapping("/positions/branch/{branchCode}")
    public ResponseEntity<ApiResponse<List<LiquidityPositionResponse>>> getPositionsByBranch(
            @PathVariable String branchCode) {

        log.debug("Fetching liquidity positions for branch: {}", branchCode);

        List<LiquidityPositionResponse> positions = liquidityService.getPositionsByBranch(branchCode);

        log.debug("Retrieved {} liquidity positions for branch: {}", positions.size(), branchCode);

        return ResponseEntity.ok(ApiResponse.success(positions));
    }

    @GetMapping("/positions/negative")
    public ResponseEntity<ApiResponse<List<LiquidityPositionResponse>>> getNegativePositions() {
        log.debug("Fetching negative liquidity positions");

        List<LiquidityPositionResponse> positions = liquidityService.getNegativePositions();

        log.debug("Retrieved {} negative liquidity positions", positions.size());

        return ResponseEntity.ok(ApiResponse.success(positions));
    }

    @GetMapping("/summary/{currency}")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalLiquidity(@PathVariable String currency) {
        log.debug("Calculating total liquidity for currency: {}", currency);

        return liquidityService.getTotalNetLiquidityByCurrency(currency)
                .map(total -> {
                    log.debug("Total liquidity for {}: {}", currency, total);
                    return ResponseEntity.ok(ApiResponse.success(total));
                })
                .orElse(ResponseEntity.ok(ApiResponse.error("No liquidity data found for currency: " + currency)));
    }

    @GetMapping("/positions/low-ratio")
    public ResponseEntity<ApiResponse<List<LiquidityPositionResponse>>> getPositionsBelowLiquidityRatio(
            @RequestParam(defaultValue = "1.0") BigDecimal threshold) {

        log.debug("Fetching positions below liquidity ratio: {}", threshold);

        List<LiquidityPositionResponse> positions = liquidityService.getPositionsBelowLiquidityRatio(threshold);

        log.debug("Retrieved {} positions below liquidity ratio {}", positions.size(), threshold);

        return ResponseEntity.ok(ApiResponse.success(positions));
    }
}