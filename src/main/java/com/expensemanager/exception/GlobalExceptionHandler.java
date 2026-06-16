package com.expensemanager.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(ResourceNotFoundException ex, Model model) {
        logger.warn("Exception Handled: class={}, message={}, cause={}", 
            ex.getClass().getName(), ex.getMessage(), (ex.getCause() != null ? ex.getCause().toString() : "none"));
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(jakarta.persistence.EntityNotFoundException ex, Model model) {
        logger.warn("Exception Handled: class={}, message={}, cause={}", 
            ex.getClass().getName(), ex.getMessage(), (ex.getCause() != null ? ex.getCause().toString() : "none"));
        model.addAttribute("errorMessage", "The requested entity could not be found.");
        return "error/404";
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex, Model model) {
        logger.warn("Exception Handled: class={}, message={}, cause={}", 
            ex.getClass().getName(), ex.getMessage(), (ex.getCause() != null ? ex.getCause().toString() : "none"));
        model.addAttribute("errorMessage", "The requested resource could not be found.");
        model.addAttribute("exceptionDetails", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(org.thymeleaf.exceptions.TemplateInputException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleTemplateInputException(org.thymeleaf.exceptions.TemplateInputException ex, Model model) {
        logger.error("Exception Handled: class={}, message={}, cause={}", 
            ex.getClass().getName(), ex.getMessage(), (ex.getCause() != null ? ex.getCause().toString() : "none"), ex);
        model.addAttribute("errorMessage", "Template parsing error occurred.");
        model.addAttribute("exceptionDetails", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        logger.warn("Exception Handled: class={}, message={}, cause={}", 
            ex.getClass().getName(), ex.getMessage(), (ex.getCause() != null ? ex.getCause().toString() : "none"));
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGlobalException(Exception ex, Model model) {
        logger.error("Exception Handled: class={}, message={}, cause={}", 
            ex.getClass().getName(), ex.getMessage(), (ex.getCause() != null ? ex.getCause().toString() : "none"), ex);
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        model.addAttribute("exceptionDetails", ex.getMessage());
        return "error/500";
    }
}
