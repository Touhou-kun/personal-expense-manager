package com.expensemanager.controller;

import com.expensemanager.dto.DashboardDto;
import com.expensemanager.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardDto dashboardData = dashboardService.getDashboardData();
        model.addAttribute("dashboard", dashboardData);
        return "dashboard/index";
    }
}
