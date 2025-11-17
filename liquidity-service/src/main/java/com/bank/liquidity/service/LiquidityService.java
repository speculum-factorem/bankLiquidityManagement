package com.bank.liquidity.service;

import com.bank.liquidity.dto.LiquidityPositionRequest;
import com.bank.liquidity.dto.LiquidityPositionResponse;
import com.bank.liquidity.model.LiquidityAlert;
import com.bank.liquidity.model.LiquidityPosition;
import com.bank.liquidity.repository.LiquidityAlertRepository;
import com.bank.liquidity.repository.LiquidityPositionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LiquidityService {

    private final LiquidityPositionRepository positionRepository;
    private final LiquidityAlertRepository alertRepository;
    private final LiquidityMetricsService metricsService;

    public LiquidityPositionResponse createPosition(LiquidityPositionRequest request) {
        log.info("Creating liquidity position for branch: {}, currency: {}",
                request.getBranchCode(), request.getCurrency());

        String correlationId = MDC.get("correlationId");

        // Check if position already exists for this branch and currency
        Optional<LiquidityPosition> existingPosition =
                positionRepository.findByBranchCodeAndCurrency(request.getBranchCode(), request.getCurrency());

        LiquidityPosition position;
        if (existingPosition.isPresent()) {
            position = existingPosition.get();
            position.setAvailableCash(request.getAvailableCash());
            position.setRequiredReserves(request.getRequiredReserves());
            log.debug("Updated existing liquidity position: {}", position.getId());
        } else {
            position = LiquidityPosition.builder()
                    .currency(request.getCurrency())
                    .availableCash(request.getAvailableCash())
                    .requiredReserves(request.getRequiredReserves())
                    .branchCode(request.getBranchCode())
                    .build();
            log.debug("Created new liquidity position");
        }

        LiquidityPosition savedPosition = positionRepository.save(position);

        // Check for alerts asynchronously
        checkForAlerts(savedPosition);

        // Record metrics
        metricsService.recordLiquidityPositionCreation(savedPosition);

        log.info("Liquidity position {} created/updated successfully for branch: {}",
                savedPosition.getId(), savedPosition.getBranchCode());

        return mapToResponse(savedPosition);
    }

    @Transactional(readOnly = true)
    public List<LiquidityPositionResponse> getAllPositions() {
        log.debug("Fetching all liquidity positions");
        return positionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LiquidityPositionResponse> getPositionsByBranch(String branchCode) {
        log.debug("Fetching liquidity positions for branch: {}", branchCode);
        return positionRepository.findByBranchCode(branchCode).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LiquidityPositionResponse> getNegativePositions() {
        log.debug("Fetching negative liquidity positions");
        return positionRepository.findNegativeLiquidityPositions().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<BigDecimal> getTotalNetLiquidityByCurrency(String currency) {
        log.debug("Calculating total net liquidity for currency: {}", currency);
        return positionRepository.getTotalNetLiquidityByCurrency(currency);
    }

    @Transactional(readOnly = true)
    public List<LiquidityPositionResponse> getPositionsBelowLiquidityRatio(BigDecimal threshold) {
        log.debug("Fetching positions below liquidity ratio: {}", threshold);
        return positionRepository.findAll().stream()
                .filter(position -> position.getLiquidityRatio().compareTo(threshold) < 0)
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Async("liquidityTaskExecutor")
    public CompletableFuture<Void> checkForAlerts(LiquidityPosition position) {
        log.debug("Checking alerts for position: {} (branch: {}, currency: {})",
                position.getId(), position.getBranchCode(), position.getCurrency());

        // Check for deficit
        if (position.getNetLiquidity().compareTo(BigDecimal.ZERO) < 0) {
            createDeficitAlert(position);
        }

        // Check for low liquidity ratio (below 1.0)
        if (position.getLiquidityRatio().compareTo(BigDecimal.ONE) < 0) {
            createLowLiquidityAlert(position);
        }

        // Check for critical liquidity (below 0.5)
        if (position.getLiquidityRatio().compareTo(new BigDecimal("0.5")) < 0) {
            createCriticalLiquidityAlert(position);
        }

        return CompletableFuture.completedFuture(null);
    }

    private void createDeficitAlert(LiquidityPosition position) {
        LiquidityAlert alert = LiquidityAlert.builder()
                .alertType(LiquidityAlert.AlertType.DEFICIT)
                .status(LiquidityAlert.AlertStatus.ACTIVE)
                .branchCode(position.getBranchCode())
                .currency(position.getCurrency())
                .deficitAmount(position.getNetLiquidity().abs())
                .liquidityRatio(position.getLiquidityRatio())
                .message(String.format("Liquidity deficit of %s %s in branch %s",
                        position.getNetLiquidity().abs(), position.getCurrency(), position.getBranchCode()))
                .severity(8)
                .build();

        alertRepository.save(alert);
        metricsService.recordLiquidityAlert(alert);

        log.warn("Deficit alert created for branch: {}, currency: {}, deficit: {}",
                position.getBranchCode(), position.getCurrency(), position.getNetLiquidity().abs());
    }

    private void createLowLiquidityAlert(LiquidityPosition position) {
        LiquidityAlert alert = LiquidityAlert.builder()
                .alertType(LiquidityAlert.AlertType.LOW_LIQUIDITY)
                .status(LiquidityAlert.AlertStatus.ACTIVE)
                .branchCode(position.getBranchCode())
                .currency(position.getCurrency())
                .deficitAmount(BigDecimal.ZERO)
                .liquidityRatio(position.getLiquidityRatio())
                .message(String.format("Low liquidity ratio: %s in branch %s",
                        position.getLiquidityRatio(), position.getBranchCode()))
                .severity(5)
                .build();

        alertRepository.save(alert);
        metricsService.recordLiquidityAlert(alert);

        log.warn("Low liquidity alert created for branch: {}, currency: {}, ratio: {}",
                position.getBranchCode(), position.getCurrency(), position.getLiquidityRatio());
    }

    private void createCriticalLiquidityAlert(LiquidityPosition position) {
        LiquidityAlert alert = LiquidityAlert.builder()
                .alertType(LiquidityAlert.AlertType.CRITICAL)
                .status(LiquidityAlert.AlertStatus.ACTIVE)
                .branchCode(position.getBranchCode())
                .currency(position.getCurrency())
                .deficitAmount(position.getNetLiquidity().abs())
                .liquidityRatio(position.getLiquidityRatio())
                .message(String.format("Critical liquidity situation in branch %s. Ratio: %s",
                        position.getBranchCode(), position.getLiquidityRatio()))
                .severity(10)
                .build();

        alertRepository.save(alert);
        metricsService.recordLiquidityAlert(alert);

        log.error("CRITICAL liquidity alert created for branch: {}, currency: {}, ratio: {}",
                position.getBranchCode(), position.getCurrency(), position.getLiquidityRatio());
    }

    private LiquidityPositionResponse mapToResponse(LiquidityPosition position) {
        return LiquidityPositionResponse.builder()
                .id(position.getId())
                .currency(position.getCurrency())
                .availableCash(position.getAvailableCash())
                .requiredReserves(position.getRequiredReserves())
                .netLiquidity(position.getNetLiquidity())
                .liquidityRatio(position.getLiquidityRatio())
                .calculationDate(position.getCalculationDate())
                .branchCode(position.getBranchCode())
                .status(position.getStatus())
                .createdAt(position.getCreatedAt())
                .updatedAt(position.getUpdatedAt())
                .build();
    }
}