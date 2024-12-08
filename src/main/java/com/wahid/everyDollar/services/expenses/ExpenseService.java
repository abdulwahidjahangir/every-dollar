package com.wahid.everyDollar.services.expenses;

import com.wahid.everyDollar.models.expenses.Expense;
import com.wahid.everyDollar.requests.expenses.ExpenseRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ExpenseService {
    Map<Long, BigDecimal> getSumByCategories(List<Long> categoryIds, Long userId, LocalDateTime startDate, LocalDateTime endDate);

    Page<Expense> getCurrentCategoryExpenses(String userEmail, Long categoryId, int page, int size, String sortDirection,
                                             String sortBy, LocalDateTime startDate, LocalDateTime endDate);

    BigDecimal getTotalForCurrentCategory(Long userId, Long categoryId,
                                          LocalDateTime startDate, LocalDateTime endDate);

    Expense createExpense(String userEmail, ExpenseRequest expenseRequest);

    Expense updateExpense(String userEmail, Long expenseId, ExpenseRequest expenseRequest);

    void deleteExpense(String userEmail, Long expenseId);
}
