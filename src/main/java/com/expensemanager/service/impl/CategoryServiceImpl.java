package com.expensemanager.service.impl;

import com.expensemanager.dto.CategoryDto;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.User;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.exception.ResourceNotFoundException;
import com.expensemanager.mapper.CategoryMapper;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.service.CategoryService;
import com.expensemanager.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategoriesForCurrentUser() {
        User currentUser = SecurityUtil.getCurrentUser();
        return categoryRepository.findAllForUser(currentUser).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesForCurrentUserAndType(CategoryType type) {
        User currentUser = SecurityUtil.getCurrentUser();
        return categoryRepository.findAllForUserAndType(currentUser, type).stream()
                .map(CategoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Ensure category is either global or belongs to the current user
        if (category.getUser() != null && !category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        return CategoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        User currentUser = SecurityUtil.getCurrentUser();

        // Check if category name already exists for user (or globally)
        if (categoryRepository.existsByNameForUserOrGlobal(dto.getName().trim(), currentUser)) {
            throw new IllegalArgumentException("Category with name '" + dto.getName() + "' already exists.");
        }

        Category category = CategoryMapper.toEntity(dto);
        category.setUser(currentUser); // Associate with the current user

        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        User currentUser = SecurityUtil.getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Global system categories cannot be modified by users
        if (category.getUser() == null) {
            throw new IllegalArgumentException("System categories cannot be modified.");
        }

        // Check ownership
        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        // Check if name is taken by another category for this user
        categoryRepository.findByNameAndUser(dto.getName().trim(), currentUser)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new IllegalArgumentException("Category with name '" + dto.getName() + "' already exists.");
                    }
                });

        category.setName(dto.getName().trim());
        category.setType(dto.getType());

        Category updatedCategory = categoryRepository.save(category);
        return CategoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        User currentUser = SecurityUtil.getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Global system categories cannot be deleted by users
        if (category.getUser() == null) {
            throw new IllegalArgumentException("System categories cannot be deleted.");
        }

        // Check ownership
        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        categoryRepository.delete(category);
    }
}
