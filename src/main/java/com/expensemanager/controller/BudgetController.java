package com.expensemanager.controller;

import com.expensemanager.dto.BudgetDto;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.service.BudgetService;
import com.expensemanager.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final CategoryService categoryService;

    public BudgetController(BudgetService budgetService, CategoryService categoryService) {
        this.budgetService = budgetService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listBudgets(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year,
            Model model) {

        LocalDate today = LocalDate.now();
        int filterMonth = (month != null) ? month : today.getMonthValue();
        int filterYear = (year != null) ? year : today.getYear();

        List<BudgetDto> budgets = budgetService.getBudgetStatusForMonthAndYear(filterMonth, filterYear);
        model.addAttribute("budgets", budgets);
        
        // Pass drop-down selection data
        model.addAttribute("selectedMonth", filterMonth);
        model.addAttribute("selectedYear", filterYear);
        model.addAttribute("months", getMonthList());
        model.addAttribute("years", getYearList());

        return "budget/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        BudgetDto dto = new BudgetDto();
        LocalDate today = LocalDate.now();
        dto.setMonth(today.getMonthValue());
        dto.setYear(today.getYear());

        model.addAttribute("budget", dto);
        model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
        model.addAttribute("months", getMonthList());
        model.addAttribute("years", getYearList());
        return "budget/form";
    }

    @PostMapping("/new")
    public String saveBudget(
            @Valid @ModelAttribute("budget") BudgetDto budgetDto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
            model.addAttribute("months", getMonthList());
            model.addAttribute("years", getYearList());
            return "budget/form";
        }

        try {
            budgetService.saveOrUpdateBudget(budgetDto);
            redirectAttributes.addFlashAttribute("successMessage", "Budget limit saved successfully!");
            return "redirect:/budgets?month=" + budgetDto.getMonth() + "&year=" + budgetDto.getYear();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("categories", categoryService.getCategoriesForCurrentUserAndType(CategoryType.EXPENSE));
            model.addAttribute("months", getMonthList());
            model.addAttribute("years", getYearList());
            return "budget/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteBudget(
            @PathVariable("id") Long id,
            @RequestParam("month") Integer month,
            @RequestParam("year") Integer year,
            RedirectAttributes redirectAttributes) {
        try {
            budgetService.deleteBudget(id);
            redirectAttributes.addFlashAttribute("successMessage", "Budget rule deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/budgets?month=" + month + "&year=" + year;
    }

    private List<Integer> getMonthList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            list.add(i);
        }
        return list;
    }

    private List<Integer> getYearList() {
        List<Integer> list = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int y = currentYear - 2; y <= currentYear + 3; y++) {
            list.add(y);
        }
        return list;
    }
}
