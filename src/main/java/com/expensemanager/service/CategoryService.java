package com.expensemanager.service;

import com.expensemanager.dto.CategoryDto;
import com.expensemanager.entity.User;
import com.expensemanager.enums.CategoryType;
import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategoriesForCurrentUser();
    List<CategoryDto> getCategoriesForCurrentUserAndType(CategoryType type);
    CategoryDto getCategoryById(Long id);
    CategoryDto createCategory(CategoryDto dto);
    CategoryDto updateCategory(Long id, CategoryDto dto);
    void deleteCategory(Long id);
}
