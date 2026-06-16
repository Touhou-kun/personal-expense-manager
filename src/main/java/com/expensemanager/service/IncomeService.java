package com.expensemanager.service;

import com.expensemanager.dto.IncomeDto;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {
    List<IncomeDto> getAllIncomesForCurrentUser();
    List<IncomeDto> searchIncomes(String keyword, Long categoryId, LocalDate startDate, LocalDate endDate);
    IncomeDto getIncomeById(Long id);
    IncomeDto createIncome(IncomeDto dto);
    IncomeDto updateIncome(Long id, IncomeDto dto);
    void deleteIncome(Long id);
}
