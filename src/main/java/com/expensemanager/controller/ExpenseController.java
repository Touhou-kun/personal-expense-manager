package com.expensemanager.controller;

import com.expensemanager.dto.ExpenseDto;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.service.CategoryService;
import com.expensemanager.service.ExpenseService;
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
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    public ExpenseController(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listExpenses(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        List<ExpenseDto> expenses = expenseService.searchExpenses(keyword, categoryId, startDate, endDate);
        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
        
        // Pass filter values back to Thymeleaf so they remain filled in the search form
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "expense/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        ExpenseDto dto = new ExpenseDto();
        dto.setTransactionDate(LocalDate.now()); // default to today
        
        model.addAttribute("expense", dto);
        model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
        return "expense/form";
    }

    @PostMapping("/new")
    public String createExpense(
            @Valid @ModelAttribute("expense") ExpenseDto expenseDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
            return "expense/form";
        }

        try {
            expenseService.createExpense(expenseDto);
            redirectAttributes.addFlashAttribute("successMessage", "Expense added successfully!");
            return "redirect:/expenses";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
            return "expense/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        try {
            ExpenseDto expenseDto = expenseService.getExpenseById(id);
            model.addAttribute("expense", expenseDto);
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
            return "expense/form";
        } catch (Exception ex) {
            return "redirect:/expenses";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateExpense(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("expense") ExpenseDto expenseDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
            return "expense/form";
        }

        try {
            expenseService.updateExpense(id, expenseDto);
            redirectAttributes.addFlashAttribute("successMessage", "Expense updated successfully!");
            return "redirect:/expenses";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
            return "expense/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteExpense(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            expenseService.deleteExpense(id);
            redirectAttributes.addFlashAttribute("successMessage", "Expense deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/expenses";
    }
}
