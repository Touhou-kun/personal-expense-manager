package com.expensemanager.service.impl;

import com.expensemanager.dto.IncomeDto;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.Income;
import com.expensemanager.entity.User;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.mapper.IncomeMapper;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.IncomeRepository;
import com.expensemanager.service.IncomeService;
import com.expensemanager.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;

    public IncomeServiceImpl(IncomeRepository incomeRepository, CategoryRepository categoryRepository) {
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeDto> getAllIncomesForCurrentUser() {
        User currentUser = SecurityUtil.getCurrentUser();
        return incomeRepository.findAllByUserOrderByTransactionDateDesc(currentUser).stream()
                .map(IncomeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncomeDto> searchIncomes(String keyword, Long categoryId, LocalDate startDate, LocalDate endDate) {
        User currentUser = SecurityUtil.getCurrentUser();
        String keywordParam = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        
        return incomeRepository.searchIncomes(currentUser, keywordParam, categoryId, startDate, endDate).stream()
                .map(IncomeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeDto getIncomeById(Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));

        // Ownership validation
        if (!income.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Income not found with id: " + id);
        }

        return IncomeMapper.toDto(income);
    }

    @Override
    @Transactional
    public IncomeDto createIncome(IncomeDto dto) {
        User currentUser = SecurityUtil.getCurrentUser();

        // Validate Category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        // Ensure category belongs to current user (or is global) and is an INCOME category
        if (category.getUser() != null && !category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId());
        }
        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException("Selected category must be an Income category.");
        }

        Income income = IncomeMapper.toEntity(dto);
        income.setUser(currentUser);
        income.setCategory(category);

        Income savedIncome = incomeRepository.save(income);
        return IncomeMapper.toDto(savedIncome);
    }

    @Override
    @Transactional
    public IncomeDto updateIncome(Long id, IncomeDto dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));

        // Ownership validation
        if (!income.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Income not found with id: " + id);
        }

        // Validate Category
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        // Ensure category is user's own (or global) and is INCOME
        if (category.getUser() != null && !category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId());
        }
        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException("Selected category must be an Income category.");
        }

        income.setTitle(dto.getTitle().trim());
        income.setAmount(dto.getAmount());
        income.setDescription(dto.getDescription());
        income.setCategory(category);
        income.setTransactionDate(dto.getTransactionDate());

        Income updatedIncome = incomeRepository.save(income);
        return IncomeMapper.toDto(updatedIncome);
    }

    @Override
    @Transactional
    public void deleteIncome(Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id: " + id));

        // Ownership validation
        if (!income.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Income not found with id: " + id);
        }

        incomeRepository.delete(income);
    }
}
