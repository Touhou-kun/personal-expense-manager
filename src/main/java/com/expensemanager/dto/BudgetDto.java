package com.expensemanager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetDto {

    private Long id;

    @NotNull(message = "Budget limit amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be valid")
    private Integer year;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String categoryName;

    // Dynamically computed fields for status
    private BigDecimal currentSpending;
    private BigDecimal remainingAmount;
    private boolean exceeded;
    private BigDecimal exceededAmount;
}
