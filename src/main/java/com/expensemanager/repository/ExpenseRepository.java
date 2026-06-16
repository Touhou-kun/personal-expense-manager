package com.expensemanager.repository;

import com.expensemanager.entity.Expense;
import com.expensemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findAllByUserOrderByTransactionDateDesc(User user);
    List<Expense> findTop10ByUserOrderByTransactionDateDesc(User user);

    // Advanced search and filtering
    @Query("SELECT e FROM Expense e WHERE e.user = :user " +
           "AND (:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR e.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR e.transactionDate <= :endDate) " +
           "ORDER BY e.transactionDate DESC, e.id DESC")
    List<Expense> searchExpenses(
            @Param("user") User user,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Aggregations
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user")
    BigDecimal sumTotalByUser(@Param("user") User user);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user AND e.category.id = :categoryId AND e.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalByUserAndCategoryIdAndDateRange(@Param("user") User user, @Param("categoryId") Long categoryId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Expense by Category aggregation
    @Query("SELECT e.category.name, COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user = :user GROUP BY e.category.name")
    List<Object[]> sumExpensesByCategory(@Param("user") User user);

    // Monthly Expense Trend (returns year, month, sum)
    @Query("SELECT YEAR(e.transactionDate), MONTH(e.transactionDate), COALESCE(SUM(e.amount), 0) " +
           "FROM Expense e WHERE e.user = :user " +
           "GROUP BY YEAR(e.transactionDate), MONTH(e.transactionDate) " +
           "ORDER BY YEAR(e.transactionDate) ASC, MONTH(e.transactionDate) ASC")
    List<Object[]> getMonthlyExpenseTrend(@Param("user") User user);
}
