package com.expensemanager.util;

import com.expensemanager.entity.User;
import com.expensemanager.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
        // Prevent instantiation
    }

    /**
     * Get the currently logged-in User details.
     */
    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Get the currently logged-in User entity.
     */
    public static User getCurrentUser() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getUser() : null;
    }

    /**
     * Get the username of the currently logged-in user.
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * Check if user is authenticated.
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
