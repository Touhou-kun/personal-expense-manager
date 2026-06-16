package com.expensemanager.controller;

import com.expensemanager.dto.CategoryDto;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategoriesForCurrentUser());
        return "category/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryDto());
        model.addAttribute("types", CategoryType.values());
        return "category/form";
    }

    @PostMapping("/new")
    public String createCategory(
            @Valid @ModelAttribute("category") CategoryDto categoryDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("types", CategoryType.values());
            return "category/form";
        }

        try {
            categoryService.createCategory(categoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully!");
            return "redirect:/categories";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("types", CategoryType.values());
            return "category/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        try {
            CategoryDto categoryDto = categoryService.getCategoryById(id);
            model.addAttribute("category", categoryDto);
            model.addAttribute("types", CategoryType.values());
            return "category/form";
        } catch (Exception ex) {
            return "redirect:/categories";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("category") CategoryDto categoryDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("types", CategoryType.values());
            return "category/form";
        }

        try {
            categoryService.updateCategory(id, categoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully!");
            return "redirect:/categories";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("types", CategoryType.values());
            return "category/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }
}
