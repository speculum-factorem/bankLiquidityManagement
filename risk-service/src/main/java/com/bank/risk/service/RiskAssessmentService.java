package com.bank.risk.service;

import com.bank.risk.config.RiskConfig;
import com.bank.risk.dto.RiskAssessmentRequest;
import com.bank.risk.dto.RiskAssessmentResponse;
import com.bank.risk.dto.RiskSummaryResponse;
import com.bank.risk.model.RiskAlert;
import com.bank.risk.model.RiskAssessment;
import com.bank.risk.repository.RiskAlertRepository;
import com.bank.risk.repository.RiskAssessmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RiskAssessmentService {

    private final RiskAssessmentRepository assessmentRepository;
    private final RiskAlertRepository alertRepository;
    private final RiskMetricsService metricsService;
    private final RiskConfig riskConfig;

    public RiskAssessmentResponse createAssessment(RiskAssessmentRequest request) {
        log.info("Creating risk assessment for branch: {}, currency: {}",
                request.getBranchCode(), request.getCurrency());

        String correlationId = MDC.get("correlationId");

        // Расчет общего балла риска с использованием взвешенного среднего
        BigDecimal overallRiskScore = calculateWeightedRiskScore(request);

        // Построение описания факторов риска
        String riskFactors = buildRiskFactorsDescription(request);

        // Генерация рекомендаций
        String recommendations = generateRecommendations(overallRiskScore, request);

        RiskAssessment assessment = RiskAssessment.builder()
                .branchCode(request.getBranchCode())
                .currency(request.getCurrency())
                .liquidityRisk(request.getLiquidityRisk())
                .volatilityRisk(request.getVolatilityRisk())
                .concentrationRisk(request.getConcentrationRisk())
                .marketRisk(request.getMarketRisk())
                .riskScore(overallRiskScore)
                .riskFactors(riskFactors)
                .recommendations(recommendations)
                .build();

        RiskAssessment savedAssessment = assessmentRepository.save(assessment);

        // Проверка на наличие алертов риска
        checkForRiskAlerts(savedAssessment);

        // Запись метрик
        metricsService.recordRiskAssessment(savedAssessment);

        log.info("Risk assessment created successfully for branch: {}, currency: {}, score: {}",
                savedAssessment.getBranchCode(), savedAssessment.getCurrency(), savedAssessment.getRiskScore());

        return mapToResponse(savedAssessment);
    }

    @Transactional(readOnly = true)
    public List<RiskAssessmentResponse> getAllAssessments() {
        log.debug("Fetching all risk assessments");
        return assessmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RiskAssessmentResponse> getAssessmentsByBranch(String branchCode) {
        log.debug("Fetching risk assessments for branch: {}", branchCode);
        return assessmentRepository.findByBranchCode(branchCode).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RiskAssessmentResponse> getLatestAssessment(String branchCode, String currency) {
        log.debug("Fetching latest risk assessment for branch: {}, currency: {}", branchCode, currency);
        return assessmentRepository.findFirstByBranchCodeAndCurrencyOrderByAssessmentDateDesc(branchCode, currency)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<RiskAssessmentResponse> getHighRiskAssessments() {
        log.debug("Fetching high risk assessments");
        BigDecimal threshold = riskConfig.getThresholds().getHighRiskMax();
        return assessmentRepository.findHighRiskAssessments(threshold).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RiskAssessmentResponse> getCriticalRiskAssessments() {
        log.debug("Fetching critical risk assessments");
        return assessmentRepository.findByRiskLevel(RiskAssessment.RiskLevel.CRITICAL).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RiskSummaryResponse getRiskSummary(String branchCode, String currency) {
        log.debug("Generating risk summary for branch: {}, currency: {}", branchCode, currency);

        List<RiskAssessment> assessments = assessmentRepository.findByBranchCodeAndCurrency(branchCode, currency);

        if (assessments.isEmpty()) {
            return RiskSummaryResponse.builder()
                    .branchCode(branchCode)
                    .currency(currency)
                    .assessmentCount(0)
                    .summary("No risk assessments available")
                    .build();
        }

        // Расчет среднего балла риска
        BigDecimal averageScore = assessments.stream()
                .map(RiskAssessment::getRiskScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(assessments.size()), 2, RoundingMode.HALF_UP);

        // Получение последней оценки
        RiskAssessment latest = assessments.get(0);

        // Расчет тренда риска (сравнение с предыдущей оценкой, если доступна)
        BigDecimal riskTrend = calculateRiskTrend(assessments);

        // Расчет распределения уровней риска
        Map<RiskAssessment.RiskLevel, Long> distribution = assessments.stream()
                .collect(Collectors.groupingBy(RiskAssessment::getRiskLevel, Collectors.counting()));

        return RiskSummaryResponse.builder()
                .branchCode(branchCode)
                .currency(currency)
                .averageRiskScore(averageScore)
                .currentRiskLevel(latest.getRiskLevel())
                .assessmentCount(assessments.size())
                .riskLevelDistribution(distribution)
                .riskTrend(riskTrend)
                .summary(generateSummary(latest, averageScore, riskTrend))
                .build();
    }

    private BigDecimal calculateWeightedRiskScore(RiskAssessmentRequest request) {
        BigDecimal weightedScore = BigDecimal.ZERO;

        weightedScore = weightedScore.add(request.getLiquidityRisk().multiply(riskConfig.getWeights().getLiquidityWeight()));
        weightedScore = weightedScore.add(request.getVolatilityRisk().multiply(riskConfig.getWeights().getVolatilityWeight()));
        weightedScore = weightedScore.add(request.getConcentrationRisk().multiply(riskConfig.getWeights().getConcentrationWeight()));
        weightedScore = weightedScore.add(request.getMarketRisk().multiply(riskConfig.getWeights().getMarketWeight()));

        return weightedScore.setScale(2, RoundingMode.HALF_UP);
    }

    private String buildRiskFactorsDescription(RiskAssessmentRequest request) {
        StringBuilder factors = new StringBuilder();

        if (request.getLiquidityRisk().compareTo(new BigDecimal("50")) > 0) {
            factors.append("High liquidity risk; ");
        }
        if (request.getVolatilityRisk().compareTo(new BigDecimal("50")) > 0) {
            factors.append("High volatility risk; ");
        }
        if (request.getConcentrationRisk().compareTo(new BigDecimal("50")) > 0) {
            factors.append("High concentration risk; ");
        }
        if (request.getMarketRisk().compareTo(new BigDecimal("50")) > 0) {
            factors.append("High market risk; ");
        }

        if (factors.length() == 0) {
            factors.append("Moderate risk factors across all categories");
        }

        return factors.toString();
    }

    private String generateRecommendations(BigDecimal overallScore, RiskAssessmentRequest request) {
        StringBuilder recommendations = new StringBuilder();

        if (overallScore.compareTo(riskConfig.getThresholds().getCriticalRiskMin()) >= 0) {
            recommendations.append("CRITICAL: Immediate action required. ");
            recommendations.append("Consider reducing exposure and increasing reserves. ");
        } else if (overallScore.compareTo(riskConfig.getThresholds().getHighRiskMax()) >= 0) {
            recommendations.append("HIGH: Close monitoring required. ");
            recommendations.append("Review risk mitigation strategies. ");
        } else if (overallScore.compareTo(riskConfig.getThresholds().getMediumRiskMax()) >= 0) {
            recommendations.append("MEDIUM: Standard monitoring. ");
            recommendations.append("Maintain current risk controls. ");
        } else {
            recommendations.append("LOW: Normal operations. ");
            recommendations.append("Continue periodic risk reviews. ");
        }

        // Специфические рекомендации на основе факторов риска
        if (request.getLiquidityRisk().compareTo(new BigDecimal("70")) > 0) {
            recommendations.append("Focus on liquidity management and reserve adequacy. ");
        }
        if (request.getConcentrationRisk().compareTo(new BigDecimal("70")) > 0) {
            recommendations.append("Diversify exposures to reduce concentration risk. ");
        }

        return recommendations.toString();
    }

    // Расчет тренда риска
    private BigDecimal calculateRiskTrend(List<RiskAssessment> assessments) {
        if (assessments.size() < 2) {
            return BigDecimal.ZERO;
        }

        // Сравнение последней оценки с предыдущей
        RiskAssessment latest = assessments.get(0);
        RiskAssessment previous = assessments.get(1);

        return latest.getRiskScore().subtract(previous.getRiskScore());
    }

    private String generateSummary(RiskAssessment latest, BigDecimal averageScore, BigDecimal trend) {
        String trendDirection = trend.compareTo(BigDecimal.ZERO) > 0 ? "increasing" :
                trend.compareTo(BigDecimal.ZERO) < 0 ? "decreasing" : "stable";

        return String.format(
                "Current risk level: %s. Average historical score: %.2f. Trend: %s (%.2f). %s",
                latest.getRiskLevel(), averageScore, trendDirection, trend, latest.getRecommendations()
        );
    }

    // Проверка оценки риска на наличие алертов
    private void checkForRiskAlerts(RiskAssessment assessment) {
        // Проверка на критический риск
        if (assessment.getRiskLevel() == RiskAssessment.RiskLevel.CRITICAL) {
            createCriticalRiskAlert(assessment);
        }

        // Проверка на превышение порога высокого риска
        if (assessment.getRiskScore().compareTo(riskConfig.getThresholds().getHighRiskMax()) >= 0) {
            createHighRiskAlert(assessment);
        }

        // Проверка на значительное увеличение риска по сравнению с предыдущей оценкой
        checkForRiskIncreaseAlert(assessment);
    }

    private void createCriticalRiskAlert(RiskAssessment assessment) {
        RiskAlert alert = RiskAlert.builder()
                .alertType(RiskAlert.AlertType.RISK_THRESHOLD_BREACH)
                .status(RiskAlert.AlertStatus.ACTIVE)
                .branchCode(assessment.getBranchCode())
                .currency(assessment.getCurrency())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel().name())
                .message(String.format("CRITICAL risk level detected in branch %s for currency %s. Score: %.2f",
                        assessment.getBranchCode(), assessment.getCurrency(), assessment.getRiskScore()))
                .severity(10)
                .details(assessment.getRiskFactors())
                .mitigationSteps(assessment.getRecommendations())
                .build();

        alertRepository.save(alert);
        metricsService.recordRiskAlert(alert);

        log.error("CRITICAL risk alert created for branch: {}, currency: {}, score: {}",
                assessment.getBranchCode(), assessment.getCurrency(), assessment.getRiskScore());
    }

    private void createHighRiskAlert(RiskAssessment assessment) {
        RiskAlert alert = RiskAlert.builder()
                .alertType(RiskAlert.AlertType.RISK_THRESHOLD_BREACH)
                .status(RiskAlert.AlertStatus.ACTIVE)
                .branchCode(assessment.getBranchCode())
                .currency(assessment.getCurrency())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel().name())
                .message(String.format("HIGH risk level detected in branch %s for currency %s",
                        assessment.getBranchCode(), assessment.getCurrency()))
                .severity(7)
                .details(assessment.getRiskFactors())
                .mitigationSteps(assessment.getRecommendations())
                .build();

        alertRepository.save(alert);
        metricsService.recordRiskAlert(alert);

        log.warn("High risk alert created for branch: {}, currency: {}, score: {}",
                assessment.getBranchCode(), assessment.getCurrency(), assessment.getRiskScore());
    }

    private void checkForRiskIncreaseAlert(RiskAssessment assessment) {
        List<RiskAssessment> recentAssessments = assessmentRepository.findRecentAssessments(
                assessment.getBranchCode(), assessment.getCurrency(), 2);

        if (recentAssessments.size() == 2) {
            RiskAssessment previous = recentAssessments.get(1);
            BigDecimal riskIncrease = assessment.getRiskScore().subtract(previous.getRiskScore());

            if (riskIncrease.compareTo(new BigDecimal("15")) > 0) { // 15% increase threshold
                createRiskIncreaseAlert(assessment, previous, riskIncrease);
            }
        }
    }

    private void createRiskIncreaseAlert(RiskAssessment current, RiskAssessment previous, BigDecimal increase) {
        RiskAlert alert = RiskAlert.builder()
                .alertType(RiskAlert.AlertType.RISK_INCREASE)
                .status(RiskAlert.AlertStatus.ACTIVE)
                .branchCode(current.getBranchCode())
                .currency(current.getCurrency())
                .riskScore(current.getRiskScore())
                .riskLevel(current.getRiskLevel().name())
                .previousRiskScore(previous.getRiskScore())
                .message(String.format("Significant risk increase in branch %s for currency %s. Increase: %.2f points",
                        current.getBranchCode(), current.getCurrency(), increase))
                .severity(8)
                .details(String.format("Risk increased from %.2f to %.2f. Factors: %s",
                        previous.getRiskScore(), current.getRiskScore(), current.getRiskFactors()))
                .mitigationSteps(current.getRecommendations())
                .build();

        alertRepository.save(alert);
        metricsService.recordRiskAlert(alert);

        log.warn("Risk increase alert created for branch: {}, currency: {}, increase: {}",
                current.getBranchCode(), current.getCurrency(), increase);
    }

    private RiskAssessmentResponse mapToResponse(RiskAssessment assessment) {
        return RiskAssessmentResponse.builder()
                .id(assessment.getId())
                .branchCode(assessment.getBranchCode())
                .currency(assessment.getCurrency())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .assessmentDate(assessment.getAssessmentDate())
                .liquidityRisk(assessment.getLiquidityRisk())
                .volatilityRisk(assessment.getVolatilityRisk())
                .concentrationRisk(assessment.getConcentrationRisk())
                .marketRisk(assessment.getMarketRisk())
                .recommendations(assessment.getRecommendations())
                .riskFactors(assessment.getRiskFactors())
                .createdAt(assessment.getCreatedAt())
                .updatedAt(assessment.getUpdatedAt())
                .build();
    }
}