package com.expensemanager.repository;

import com.expensemanager.entity.Income;
import com.expensemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findAllByUserOrderByTransactionDateDesc(User user);
    List<Income> findTop10ByUserOrderByTransactionDateDesc(User user);

    // Advanced search and filtering
    @Query("SELECT i FROM Income i WHERE i.user = :user " +
           "AND (:keyword IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR i.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR i.transactionDate <= :endDate) " +
           "ORDER BY i.transactionDate DESC, i.id DESC")
    List<Income> searchIncomes(
            @Param("user") User user,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Aggregations
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user")
    BigDecimal sumTotalByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user AND i.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Income by Category aggregation
    @Query("SELECT i.category.name, COALESCE(SUM(i.amount), 0) FROM Income i WHERE i.user = :user GROUP BY i.category.name")
    List<Object[]> sumIncomesByCategory(@Param("user") User user);

    // Monthly Income Trend (returns year, month, sum)
    @Query("SELECT YEAR(i.transactionDate), MONTH(i.transactionDate), COALESCE(SUM(i.amount), 0) " +
           "FROM Income i WHERE i.user = :user " +
           "GROUP BY YEAR(i.transactionDate), MONTH(i.transactionDate) " +
           "ORDER BY YEAR(i.transactionDate) ASC, MONTH(i.transactionDate) ASC")
    List<Object[]> getMonthlyIncomeTrend(@Param("user") User user);
}
