package com.bank.liquidity.repository;

import com.bank.liquidity.model.LiquidityPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiquidityPositionRepository extends JpaRepository<LiquidityPosition, Long> {

    List<LiquidityPosition> findByBranchCode(String branchCode);

    Optional<LiquidityPosition> findByBranchCodeAndCurrency(String branchCode, String currency);

    List<LiquidityPosition> findByStatus(String status);

    List<LiquidityPosition> findByNetLiquidityLessThan(BigDecimal threshold);

    List<LiquidityPosition> findByCalculationDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT lp FROM LiquidityPosition lp WHERE lp.netLiquidity < 0")
    List<LiquidityPosition> findNegativeLiquidityPositions();

    @Query("SELECT SUM(lp.netLiquidity) FROM LiquidityPosition lp WHERE lp.currency = :currency")
    Optional<BigDecimal> getTotalNetLiquidityByCurrency(@Param("currency") String currency);

    @Query("SELECT lp.branchCode, SUM(lp.netLiquidity) FROM LiquidityPosition lp GROUP BY lp.branchCode")
    List<Object[]> getNetLiquidityByBranch();

    @Query("SELECT lp.currency, AVG(lp.liquidityRatio) FROM LiquidityPosition lp GROUP BY lp.currency")
    List<Object[]> getAverageLiquidityRatioByCurrency();

    @Query("SELECT COUNT(lp) FROM LiquidityPosition lp WHERE lp.liquidityRatio < :threshold")
    Long countPositionsBelowLiquidityRatio(@Param("threshold") BigDecimal threshold);

    @Query(value = """
        SELECT * FROM liquidity_positions lp 
        WHERE lp.calculation_date >= :since 
        ORDER BY lp.calculation_date DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<LiquidityPosition> findRecentPositions(@Param("since") LocalDateTime since, @Param("limit") int limit);
}