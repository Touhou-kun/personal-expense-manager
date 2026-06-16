package com.expensemanager.config;

import com.expensemanager.entity.Category;
import com.expensemanager.entity.User;
import com.expensemanager.enums.CategoryType;
import com.expensemanager.enums.UserRole;
import com.expensemanager.repository.CategoryRepository;
import com.expensemanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, 
                          CategoryRepository categoryRepository, 
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing database seeding...");

        // 1. Seed Default Accounts
        seedUserAccount("admin", "admin@expensemanager.com", "admin1234", UserRole.ROLE_ADMIN);
        seedUserAccount("user", "user@expensemanager.com", "user1234", UserRole.ROLE_USER);

        // 2. Seed Global System Categories (where user is null)
        seedGlobalCategory("Food & Dining", CategoryType.EXPENSE);
        seedGlobalCategory("Utilities", CategoryType.EXPENSE);
        seedGlobalCategory("Rent & Housing", CategoryType.EXPENSE);
        seedGlobalCategory("Transportation", CategoryType.EXPENSE);
        seedGlobalCategory("Entertainment", CategoryType.EXPENSE);
        seedGlobalCategory("Medical & Healthcare", CategoryType.EXPENSE);

        seedGlobalCategory("Salary", CategoryType.INCOME);
        seedGlobalCategory("Freelance & Consulting", CategoryType.INCOME);
        seedGlobalCategory("Investments", CategoryType.INCOME);
        seedGlobalCategory("Gifts & Others", CategoryType.INCOME);

        logger.info("Database seeding completed successfully.");
    }

    private void seedUserAccount(String username, String email, String password, UserRole role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .build();
            userRepository.save(user);
            logger.info("Seeded default account: {}", username);
        } else {
            logger.debug("Account {} already exists. Skipping.", username);
        }
    }

    private void seedGlobalCategory(String name, CategoryType type) {
        if (categoryRepository.findByNameAndUserIsNull(name).isEmpty()) {
            Category category = Category.builder()
                    .name(name)
                    .type(type)
                    .user(null) // NULL user represents global system categories
                    .build();
            categoryRepository.save(category);
            logger.info("Seeded global category: {} ({})", name, type);
        } else {
            logger.debug("Global category {} already exists. Skipping.", name);
        }
    }
}
