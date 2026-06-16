package com.expensemanager.service;

import com.expensemanager.dto.ExpenseDto;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    List<ExpenseDto> getAllExpensesForCurrentUser();
    List<ExpenseDto> searchExpenses(String keyword, Long categoryId, LocalDate startDate, LocalDate endDate);
    ExpenseDto getExpenseById(Long id);
    ExpenseDto createExpense(ExpenseDto dto);
    ExpenseDto updateExpense(Long id, ExpenseDto dto);
    void deleteExpense(Long id);
}
