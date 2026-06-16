package com.expensemanager.mapper;

import com.expensemanager.dto.ExpenseDto;
import com.expensemanager.entity.Expense;

public final class ExpenseMapper {

    private ExpenseMapper() {
        // Prevent instantiation
    }

    public static ExpenseDto toDto(Expense expense) {
        if (expense == null) {
            return null;
        }
        return ExpenseDto.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .description(expense.getDescription())
                .categoryId(expense.getCategory() != null ? expense.getCategory().getId() : null)
                .categoryName(expense.getCategory() != null ? expense.getCategory().getName() : null)
                .transactionDate(expense.getTransactionDate())
                .build();
    }

    public static Expense toEntity(ExpenseDto dto) {
        if (dto == null) {
            return null;
        }
        return Expense.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .transactionDate(dto.getTransactionDate())
                .build();
    }
}
