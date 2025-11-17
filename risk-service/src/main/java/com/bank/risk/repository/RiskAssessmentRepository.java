package com.bank.risk.repository;

import com.bank.risk.model.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    List<RiskAssessment> findByBranchCode(String branchCode);

    List<RiskAssessment> findByBranchCodeAndCurrency(String branchCode, String currency);

    List<RiskAssessment> findByRiskLevel(RiskAssessment.RiskLevel riskLevel);

    List<RiskAssessment> findByRiskScoreGreaterThanEqual(BigDecimal minScore);

    List<RiskAssessment> findByAssessmentDateBetween(LocalDateTime start, LocalDateTime end);

    Optional<RiskAssessment> findFirstByBranchCodeAndCurrencyOrderByAssessmentDateDesc(String branchCode, String currency);

    @Query("SELECT ra FROM RiskAssessment ra WHERE ra.riskScore >= :threshold ORDER BY ra.riskScore DESC")
    List<RiskAssessment> findHighRiskAssessments(@Param("threshold") BigDecimal threshold);

    @Query("SELECT ra.branchCode, AVG(ra.riskScore) FROM RiskAssessment ra GROUP BY ra.branchCode")
    List<Object[]> getAverageRiskScoreByBranch();

    @Query("SELECT ra.riskLevel, COUNT(ra) FROM RiskAssessment ra GROUP BY ra.riskLevel")
    List<Object[]> getRiskLevelDistribution();

    @Query("SELECT ra.branchCode, ra.currency, ra.riskScore FROM RiskAssessment ra WHERE ra.assessmentDate = (SELECT MAX(ra2.assessmentDate) FROM RiskAssessment ra2 WHERE ra2.branchCode = ra.branchCode AND ra2.currency = ra.currency)")
    List<Object[]> getLatestRiskScores();

    @Query("SELECT COUNT(ra) FROM RiskAssessment ra WHERE ra.riskLevel = :riskLevel")
    Long countByRiskLevel(@Param("riskLevel") RiskAssessment.RiskLevel riskLevel);

    @Query(value = """
        SELECT * FROM risk_assessments ra 
        WHERE ra.branch_code = :branchCode 
        AND ra.currency = :currency 
        ORDER BY ra.assessment_date DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<RiskAssessment> findRecentAssessments(@Param("branchCode") String branchCode,
                                               @Param("currency") String currency,
                                               @Param("limit") int limit);
}