package com.expensemanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;

    @Builder.Default
    private List<String> expenseByCategoryLabels = new ArrayList<>();
    @Builder.Default
    private List<BigDecimal> expenseByCategoryValues = new ArrayList<>();

    @Builder.Default
    private List<String> incomeByCategoryLabels = new ArrayList<>();
    @Builder.Default
    private List<BigDecimal> incomeByCategoryValues = new ArrayList<>();

    @Builder.Default
    private List<String> monthlyExpenseTrendLabels = new ArrayList<>();
    @Builder.Default
    private List<BigDecimal> monthlyExpenseTrendValues = new ArrayList<>();

    @Builder.Default
    private List<String> monthlyIncomeTrendLabels = new ArrayList<>();
    @Builder.Default
    private List<BigDecimal> monthlyIncomeTrendValues = new ArrayList<>();

    @Builder.Default
    private List<TransactionDto> recentTransactions = new ArrayList<>();
}
