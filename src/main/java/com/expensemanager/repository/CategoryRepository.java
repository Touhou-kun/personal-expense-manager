package com.expensemanager.repository;

import com.expensemanager.entity.Category;
import com.expensemanager.entity.User;
import com.expensemanager.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Find custom user categories + global default categories
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user = :user")
    List<Category> findAllForUser(@Param("user") User user);
    
    // Find custom user categories + global defaults of a specific type
    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user = :user) AND c.type = :type")
    List<Category> findAllForUserAndType(@Param("user") User user, @Param("type") CategoryType type);

    // Find category by name and user
    Optional<Category> findByNameAndUser(String name, User user);

    // Find global categories by name (where user is null)
    Optional<Category> findByNameAndUserIsNull(String name);
    
    // Check if category name exists for user or globally
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND (c.user = :user OR c.user IS NULL)")
    boolean existsByNameForUserOrGlobal(@Param("name") String name, @Param("user") User user);
}
