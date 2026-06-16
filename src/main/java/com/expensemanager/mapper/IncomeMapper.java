package com.expensemanager.mapper;

import com.expensemanager.dto.IncomeDto;
import com.expensemanager.entity.Income;

public final class IncomeMapper {

    private IncomeMapper() {
        // Prevent instantiation
    }

    public static IncomeDto toDto(Income income) {
        if (income == null) {
            return null;
        }
        return IncomeDto.builder()
                .id(income.getId())
                .title(income.getTitle())
                .amount(income.getAmount())
                .description(income.getDescription())
                .categoryId(income.getCategory() != null ? income.getCategory().getId() : null)
                .categoryName(income.getCategory() != null ? income.getCategory().getName() : null)
                .transactionDate(income.getTransactionDate())
                .build();
    }

    public static Income toEntity(IncomeDto dto) {
        if (dto == null) {
            return null;
        }
        return Income.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .transactionDate(dto.getTransactionDate())
                .build();
    }
}
