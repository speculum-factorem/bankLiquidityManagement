package com.bank.risk.repository;

import com.bank.risk.model.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RiskAlertRepository extends JpaRepository<RiskAlert, Long> {

    List<RiskAlert> findByBranchCode(String branchCode);

    List<RiskAlert> findByStatus(RiskAlert.AlertStatus status);

    List<RiskAlert> findByAlertTypeAndStatus(RiskAlert.AlertType alertType, RiskAlert.AlertStatus status);

    List<RiskAlert> findBySeverityGreaterThanEqual(Integer minSeverity);

    List<RiskAlert> findByRiskLevelAndCreatedAtAfter(String riskLevel, LocalDateTime since);

    @Query("SELECT ra FROM RiskAlert ra WHERE ra.createdAt >= :since AND ra.severity >= :minSeverity")
    List<RiskAlert> findRecentHighSeverityAlerts(@Param("since") LocalDateTime since,
                                                 @Param("minSeverity") Integer minSeverity);

    @Query("SELECT COUNT(ra) FROM RiskAlert ra WHERE ra.status = :status")
    Long countByStatus(@Param("status") RiskAlert.AlertStatus status);

    @Query("SELECT ra.alertType, COUNT(ra) FROM RiskAlert ra WHERE ra.createdAt >= :since GROUP BY ra.alertType")
    List<Object[]> getAlertCountByTypeSince(@Param("since") LocalDateTime since);
}