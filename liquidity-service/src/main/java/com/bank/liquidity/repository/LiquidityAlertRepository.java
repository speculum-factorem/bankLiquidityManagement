package com.bank.liquidity.repository;

import com.bank.liquidity.model.LiquidityAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LiquidityAlertRepository extends JpaRepository<LiquidityAlert, Long> {

    List<LiquidityAlert> findByBranchCode(String branchCode);

    List<LiquidityAlert> findByStatus(LiquidityAlert.AlertStatus status);

    List<LiquidityAlert> findByAlertTypeAndStatus(LiquidityAlert.AlertType alertType, LiquidityAlert.AlertStatus status);

    List<LiquidityAlert> findBySeverityGreaterThanEqual(Integer minSeverity);

    @Query("SELECT la FROM LiquidityAlert la WHERE la.createdAt >= :since AND la.severity >= :minSeverity")
    List<LiquidityAlert> findRecentHighSeverityAlerts(@Param("since") LocalDateTime since,
                                                      @Param("minSeverity") Integer minSeverity);

    Long countByStatus(LiquidityAlert.AlertStatus status);
}