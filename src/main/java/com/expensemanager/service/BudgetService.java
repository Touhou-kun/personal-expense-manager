package com.expensemanager.service;

import com.expensemanager.dto.BudgetDto;
import java.util.List;

public interface BudgetService {
    List<BudgetDto> getAllBudgetsForCurrentUser();
    BudgetDto getBudgetById(Long id);
    BudgetDto saveOrUpdateBudget(BudgetDto dto);
    void deleteBudget(Long id);
    List<BudgetDto> getBudgetStatusForMonthAndYear(Integer month, Integer year);
}
