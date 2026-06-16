package com.expensemanager.repository;

import com.expensemanager.entity.Budget;
import com.expensemanager.entity.Category;
import com.expensemanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    
    List<Budget> findAllByUser(User user);
    
    List<Budget> findAllByUserAndMonthAndYear(User user, Integer month, Integer year);
    
    Optional<Budget> findByUserAndCategoryAndMonthAndYear(User user, Category category, Integer month, Integer year);
    
    boolean existsByUserAndCategoryAndMonthAndYear(User user, Category category, Integer month, Integer year);
}
