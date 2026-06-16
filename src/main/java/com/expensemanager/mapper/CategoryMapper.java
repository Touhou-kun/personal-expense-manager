package com.expensemanager.mapper;

import com.expensemanager.dto.CategoryDto;
import com.expensemanager.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {
        // Prevent instantiation
    }

    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .system(category.getUser() == null)
                .build();
    }

    public static Category toEntity(CategoryDto dto) {
        if (dto == null) {
            return null;
        }
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .type(dto.getType())
                .build();
    }
}
