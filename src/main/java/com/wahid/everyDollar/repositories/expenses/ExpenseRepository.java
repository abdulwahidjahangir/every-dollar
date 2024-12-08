package com.wahid.everyDollar.repositories.expenses;

import com.wahid.everyDollar.models.expenses.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findById(Long id);

    @Query("SELECT e.category.id AS categoryId, SUM(e.amount) AS totalAmount " +
            "FROM Expense e " +
            "WHERE e.category.id IN :categoryIds " +
            "AND e.user.id = :userId " +
            "AND e.createdAt >= :startDate " +
            "AND e.createdAt <= :endDate " +
            "GROUP BY e.category.id")
    List<Map<String, Object>> getSumByCategoryAndDateRange(List<Long> categoryIds, Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT e FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.category.id = :categoryId " +
            "AND e.createdAt >= :startDate " +
            "AND e.createdAt <= :endDate")
    Page<Expense> findByUserIdAndCategoryIdAndDateRange(Pageable pageable, Long userId, Long categoryId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT e FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.createdAt >= :startDate " +
            "AND e.createdAt <= :endDate")
    Page<Expense> findByUserId(Pageable pageable, Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(e.amount) " +
            "FROM Expense e " +
            "WHERE e.category.id = :categoryId " +
            "AND e.user.id = :userId " +
            "AND e.createdAt >= :startDate " +
            "AND e.createdAt <= :endDate ")
    Optional<BigDecimal> getSumForCurrentCategory(Long userId, Long categoryId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(e.amount) " +
            "FROM Expense e " +
            "WHERE e.user.id = :userId " +
            "AND e.createdAt >= :startDate " +
            "AND e.createdAt <= :endDate ")
    Optional<BigDecimal> getSumForCurrentUser(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
