package com.expensemanager.service.impl;

import com.expensemanager.dto.ExpenseDto;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Expense;
import com.expensemanager.entity.User;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.mapper.ExpenseMapper;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.service.ExpenseService;
import com.expensemanager.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> getAllExpensesForCurrentUser() {
        User currentUser = SecurityUtil.getCurrentUser();
        return expenseRepository.findAllByUserOrderByTransactionDateDesc(currentUser).stream()
                .map(ExpenseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseDto> searchExpenses(String keyword, Long categoryId, LocalDate startDate, LocalDate endDate) {
        User currentUser = SecurityUtil.getCurrentUser();
        String keywordParam = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        
        return expenseRepository.searchExpenses(currentUser, keywordParam, categoryId, startDate, endDate).stream()
                .map(ExpenseMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseDto getExpenseById(Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        // Ownership validation
        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }

        return ExpenseMapper.toDto(expense);
    }

    @Override
    @Transactional
    public ExpenseDto createExpense(ExpenseDto dto) {
        User currentUser = SecurityUtil.getCurrentUser();

        // Validate Category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        // Ensure category belongs to current user (or is global) and is an EXPENSE category
        if (category.getUser() != null && !category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId());
        }
        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException("Selected category must be an Expense category.");
        }

        Expense expense = ExpenseMapper.toEntity(dto);
        expense.setUser(currentUser);
        expense.setCategory(category);

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseMapper.toDto(savedExpense);
    }

    @Override
    @Transactional
    public ExpenseDto updateExpense(Long id, ExpenseDto dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        // Ownership validation
        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }

        // Validate Category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        // Ensure category is user's own (or global) and is EXPENSE
        if (category.getUser() != null && !category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId());
        }
        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException("Selected category must be an Expense category.");
        }

        expense.setTitle(dto.getTitle().trim());
        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        expense.setCategory(category);
        expense.setTransactionDate(dto.getTransactionDate());

        Expense updatedExpense = expenseRepository.save(expense);
        return ExpenseMapper.toDto(updatedExpense);
    }

    @Override
    @Transactional
    public void deleteExpense(Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        // Ownership validation
        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }

        expenseRepository.delete(expense);
    }
}
