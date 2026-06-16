package com.expensemanager.controller;

import com.expensemanager.dto.UserRegistrationDto;
import com.expensemanager.service.UserService;
import com.expensemanager.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        if (SecurityUtil.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (SecurityUtil.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
            BindingResult result,
            Model model) {
        
        if (SecurityUtil.isAuthenticated()) {
            return "redirect:/dashboard";
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
            return "auth/register";
        }

        try {
            userService.registerNewUser(registrationDto);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("registrationError", ex.getMessage());
            return "auth/register";
        }
    }
}
