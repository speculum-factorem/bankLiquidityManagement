package com.bank.transaction.repository;

import com.bank.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByTransactionId(String transactionId);

    List<Transaction> findByAccountNumber(String accountNumber);

    List<Transaction> findByAccountNumberAndTransactionDateBetween(
            String accountNumber, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    List<Transaction> findByTypeAndStatus(Transaction.TransactionType type, Transaction.TransactionStatus status);

    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByAmountGreaterThanEqual(BigDecimal minAmount);

    List<Transaction> findByAccountNumberAndTypeAndTransactionDateAfter(
            String accountNumber, Transaction.TransactionType type, LocalDateTime since);

    @Query("SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber ORDER BY t.transactionDate DESC LIMIT :limit")
    List<Transaction> findRecentTransactionsByAccount(@Param("accountNumber") String accountNumber,
                                                      @Param("limit") int limit);

    @Query("SELECT t FROM Transaction t WHERE t.amount >= :threshold AND t.status = 'COMPLETED'")
    List<Transaction> findHighValueCompletedTransactions(@Param("threshold") BigDecimal threshold);

    @Query("SELECT t.type, COUNT(t), SUM(t.amount) FROM Transaction t WHERE t.transactionDate BETWEEN :start AND :end GROUP BY t.type")
    List<Object[]> getTransactionStatisticsByType(@Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);

    @Query("SELECT t.accountNumber, COUNT(t), SUM(t.amount) FROM Transaction t WHERE t.transactionDate BETWEEN :start AND :end GROUP BY t.accountNumber HAVING COUNT(t) > :minCount")
    List<Object[]> getActiveAccounts(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end,
                                     @Param("minCount") Long minCount);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.accountNumber = :accountNumber AND t.status = 'FAILED' AND t.transactionDate >= :since")
    Long countFailedTransactionsSince(@Param("accountNumber") String accountNumber,
                                      @Param("since") LocalDateTime since);

    @Query(value = """
        SELECT * FROM transactions t 
        WHERE t.transaction_date >= :since 
        AND t.amount >= :minAmount 
        ORDER BY t.amount DESC 
        LIMIT :limit
        """, nativeQuery = true)
    List<Transaction> findLargeTransactionsSince(@Param("since") LocalDateTime since,
                                                 @Param("minAmount") BigDecimal minAmount,
                                                 @Param("limit") int limit);
}