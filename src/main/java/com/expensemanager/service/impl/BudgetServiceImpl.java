package com.expensemanager.service.impl;

import com.expensemanager.dto.BudgetDto;
import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.User;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.mapper.BudgetMapper;
import com.expensemanager.repository.BudgetRepository;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.ExpenseRepository;
import com.expensemanager.service.BudgetService;
import com.expensemanager.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public BudgetServiceImpl(BudgetRepository budgetRepository, 
                             CategoryRepository categoryRepository, 
                             ExpenseRepository expenseRepository) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetDto> getAllBudgetsForCurrentUser() {
        User currentUser = SecurityUtil.getCurrentUser();
        List<Budget> budgets = budgetRepository.findAllByUser(currentUser);
        
        return budgets.stream()
                .map(budget -> {
                    BudgetDto dto = BudgetMapper.toDto(budget);
                    populateBudgetMetrics(budget, dto, currentUser);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetDto getBudgetById(Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget rule not found with id: " + id));

        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Budget rule not found with id: " + id);
        }

        BudgetDto dto = BudgetMapper.toDto(budget);
        populateBudgetMetrics(budget, dto, currentUser);
        return dto;
    }

    @Override
    @Transactional
    public BudgetDto saveOrUpdateBudget(BudgetDto dto) {
        User currentUser = SecurityUtil.getCurrentUser();

        // Validate Category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        // Ensure category is user's own (or global) and is EXPENSE
        if (category.getUser() != null && !category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId());
        }
        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException("Budgets can only be set for Expense categories.");
        }

        // Check if budget already exists for Category + Month + Year for this user (Upsert pattern)
        Budget budget = budgetRepository.findByUserAndCategoryAndMonthAndYear(currentUser, category, dto.getMonth(), dto.getYear())
                .orElse(null);

        if (budget != null) {
            // Update existing budget amount
            budget.setAmount(dto.getAmount());
        } else {
            // Create new budget rule
            budget = Budget.builder()
                    .amount(dto.getAmount())
                    .month(dto.getMonth())
                    .year(dto.getYear())
                    .category(category)
                    .user(currentUser)
                    .build();
        }

        Budget savedBudget = budgetRepository.save(budget);
        
        BudgetDto resultDto = BudgetMapper.toDto(savedBudget);
        populateBudgetMetrics(savedBudget, resultDto, currentUser);
        return resultDto;
    }

    @Override
    @Transactional
    public void deleteBudget(Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget rule not found with id: " + id));

        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Budget rule not found with id: " + id);
        }

        budgetRepository.delete(budget);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetDto> getBudgetStatusForMonthAndYear(Integer month, Integer year) {
        User currentUser = SecurityUtil.getCurrentUser();
        List<Budget> budgets = budgetRepository.findAllByUserAndMonthAndYear(currentUser, month, year);

        return budgets.stream()
                .map(budget -> {
                    BudgetDto dto = BudgetMapper.toDto(budget);
                    populateBudgetMetrics(budget, dto, currentUser);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Helper to dynamically query total monthly spending for a category and compute budget delta.
     */
    private void populateBudgetMetrics(Budget budget, BudgetDto dto, User user) {
        LocalDate startDate = LocalDate.of(budget.getYear(), budget.getMonth(), 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        BigDecimal currentSpending = expenseRepository.sumTotalByUserAndCategoryIdAndDateRange(
                user, budget.getCategory().getId(), startDate, endDate);

        dto.setCurrentSpending(currentSpending);

        BigDecimal limitAmount = budget.getAmount();
        BigDecimal difference = limitAmount.subtract(currentSpending);

        if (difference.compareTo(BigDecimal.ZERO) >= 0) {
            dto.setRemainingAmount(difference);
            dto.setExceeded(false);
            dto.setExceededAmount(BigDecimal.ZERO);
        } else {
            dto.setRemainingAmount(BigDecimal.ZERO);
            dto.setExceeded(true);
            dto.setExceededAmount(difference.negate());
        }
    }
}
