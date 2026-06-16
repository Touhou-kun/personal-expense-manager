package com.expensemanager.service.impl;

import com.expensemanager.dto.DashboardDto;
import com.expensemanager.dto.TransactionDto;
import com.expensemanager.entity.Expense;
import com.expensemanager.entity.Income;
import com.expensemanager.entity.User;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.repository.IncomeRepository;
import com.expensemanager.service.DashboardService;
import com.expensemanager.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    private static final String[] MONTH_NAMES = {
        "", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    public DashboardServiceImpl(ExpenseRepository expenseRepository, IncomeRepository incomeRepository) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getDashboardData() {
        User currentUser = SecurityUtil.getCurrentUser();

        // 1. KPI Cards
        BigDecimal totalIncome = incomeRepository.sumTotalByUser(currentUser);
        BigDecimal totalExpense = expenseRepository.sumTotalByUser(currentUser);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        DashboardDto dto = DashboardDto.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();

        // 2. Expense by Category Distribution
        List<Object[]> expenseCatData = expenseRepository.sumExpensesByCategory(currentUser);
        for (Object[] row : expenseCatData) {
            dto.getExpenseByCategoryLabels().add((String) row[0]);
            dto.getExpenseByCategoryValues().add((BigDecimal) row[1]);
        }

        // 3. Income by Category Distribution
        List<Object[]> incomeCatData = incomeRepository.sumIncomesByCategory(currentUser);
        for (Object[] row : incomeCatData) {
            dto.getIncomeByCategoryLabels().add((String) row[0]);
            dto.getIncomeByCategoryValues().add((BigDecimal) row[1]);
        }

        // 4. Monthly Expense Trend
        List<Object[]> expenseTrend = expenseRepository.getMonthlyExpenseTrend(currentUser);
        for (Object[] row : expenseTrend) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal amount = (BigDecimal) row[2];
            dto.getMonthlyExpenseTrendLabels().add(formatMonthLabel(month, year));
            dto.getMonthlyExpenseTrendValues().add(amount);
        }

        // 5. Monthly Income Trend
        List<Object[]> incomeTrend = incomeRepository.getMonthlyIncomeTrend(currentUser);
        for (Object[] row : incomeTrend) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal amount = (BigDecimal) row[2];
            dto.getMonthlyIncomeTrendLabels().add(formatMonthLabel(month, year));
            dto.getMonthlyIncomeTrendValues().add(amount);
        }

        // 6. Merged Recent Transactions Feed
        List<Expense> recentExpenses = expenseRepository.findTop10ByUserOrderByTransactionDateDesc(currentUser);
        List<Income> recentIncomes = incomeRepository.findTop10ByUserOrderByTransactionDateDesc(currentUser);

        List<TransactionDto> transactions = new ArrayList<>();
        for (Expense e : recentExpenses) {
            transactions.add(TransactionDto.builder()
                    .id(e.getId())
                    .title(e.getTitle())
                    .amount(e.getAmount())
                    .categoryName(e.getCategory() != null ? e.getCategory().getName() : "General")
                    .transactionDate(e.getTransactionDate())
                    .type(CategoryType.EXPENSE)
                    .description(e.getDescription())
                    .build());
        }

        for (Income i : recentIncomes) {
            transactions.add(TransactionDto.builder()
                    .id(i.getId())
                    .title(i.getTitle())
                    .amount(i.getAmount())
                    .categoryName(i.getCategory() != null ? i.getCategory().getName() : "General")
                    .transactionDate(i.getTransactionDate())
                    .type(CategoryType.INCOME)
                    .description(i.getDescription())
                    .build());
        }

        // Sort combined transactions descending
        Collections.sort(transactions);

        // Limit feed to the top 10 items
        int limit = Math.min(transactions.size(), 10);
        dto.setRecentTransactions(transactions.subList(0, limit));

        return dto;
    }

    private String formatMonthLabel(int month, int year) {
        if (month >= 1 && month <= 12) {
            return MONTH_NAMES[month] + " " + year;
        }
        return month + "/" + year;
    }
}
