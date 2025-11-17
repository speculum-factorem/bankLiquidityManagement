package com.bank.transaction.repository;

import com.bank.transaction.model.TransactionAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionAlertRepository extends JpaRepository<TransactionAlert, Long> {

    List<TransactionAlert> findByAccountNumber(String accountNumber);

    List<TransactionAlert> findByStatus(TransactionAlert.AlertStatus status);

    List<TransactionAlert> findByAlertTypeAndStatus(TransactionAlert.AlertType alertType, TransactionAlert.AlertStatus status);

    List<TransactionAlert> findBySeverityGreaterThanEqual(Integer minSeverity);

    Optional<TransactionAlert> findByTransactionId(String transactionId);

    @Query("SELECT ta FROM TransactionAlert ta WHERE ta.createdAt >= :since AND ta.severity >= :minSeverity")
    List<TransactionAlert> findRecentHighSeverityAlerts(@Param("since") LocalDateTime since,
                                                        @Param("minSeverity") Integer minSeverity);

    @Query("SELECT COUNT(ta) FROM TransactionAlert ta WHERE ta.status = :status")
    Long countByStatus(@Param("status") TransactionAlert.AlertStatus status);

    @Query("SELECT ta.alertType, COUNT(ta) FROM TransactionAlert ta WHERE ta.createdAt >= :since GROUP BY ta.alertType")
    List<Object[]> getAlertCountByTypeSince(@Param("since") LocalDateTime since);
}