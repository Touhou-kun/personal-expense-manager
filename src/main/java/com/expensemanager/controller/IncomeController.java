package com.expensemanager.controller;

import com.expensemanager.dto.IncomeDto;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.service.CategoryService;
import com.expensemanager.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;
    private final CategoryService categoryService;

    public IncomeController(IncomeService incomeService, CategoryService categoryService) {
        this.incomeService = incomeService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listIncomes(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        List<IncomeDto> incomes = incomeService.searchIncomes(keyword, categoryId, startDate, endDate);
        model.addAttribute("incomes", incomes);
        model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.INCOME));
        
        // Pass filter values back to Thymeleaf so they remain filled in the search form
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "income/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        IncomeDto dto = new IncomeDto();
        dto.setTransactionDate(LocalDate.now()); // default to today
        
        model.addAttribute("income", dto);
        model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.INCOME));
        return "income/form";
    }

    @PostMapping("/new")
    public String createIncome(
            @Valid @ModelAttribute("income") IncomeDto incomeDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.INCOME));
            return "income/form";
        }

        try {
            incomeService.createIncome(incomeDto);
            redirectAttributes.addFlashAttribute("successMessage", "Income added successfully!");
            return "redirect:/incomes";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.INCOME));
            return "income/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        try {
            IncomeDto incomeDto = incomeService.getIncomeById(id);
            model.addAttribute("income", incomeDto);
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.INCOME));
            return "income/form";
        } catch (Exception ex) {
            return "redirect:/incomes";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateIncome(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("income") IncomeDto incomeDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.INCOME));
            return "income/form";
        }

        try {
            incomeService.updateIncome(id, incomeDto);
            redirectAttributes.addFlashAttribute("successMessage", "Income updated successfully!");
            return "redirect:/incomes";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.INCOME));
            return "income/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteIncome(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            incomeService.deleteIncome(id);
            redirectAttributes.addFlashAttribute("successMessage", "Income deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/incomes";
    }
}
