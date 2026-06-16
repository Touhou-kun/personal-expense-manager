package com.expensemanager.mapper;

import com.expensemanager.dto.BudgetDto;
import com.expensemanager.entity.Budget;

public final class BudgetMapper {

    private BudgetMapper() {
        // Prevent instantiation
    }

    public static BudgetDto toDto(Budget budget) {
        if (budget == null) {
            return null;
        }
        return BudgetDto.builder()
                .id(budget.getId())
                .amount(budget.getAmount())
                .month(budget.getMonth())
                .year(budget.getYear())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : null)
                .build();
    }

    public static Budget toEntity(BudgetDto dto) {
        if (dto == null) {
            return null;
        }
        return Budget.builder()
                .id(dto.getId())
                .amount(dto.getAmount())
                .month(dto.getMonth())
                .year(dto.getYear())
                .build();
    }
}
