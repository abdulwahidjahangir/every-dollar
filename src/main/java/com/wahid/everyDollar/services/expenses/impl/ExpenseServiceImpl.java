package com.wahid.everyDollar.services.expenses.impl;

import com.wahid.everyDollar.errors.CustomException;
import com.wahid.everyDollar.models.categories.Category;
import com.wahid.everyDollar.models.expenses.Expense;
import com.wahid.everyDollar.models.expenses.TransactionMethod;
import com.wahid.everyDollar.models.expenses.TransactionType;
import com.wahid.everyDollar.models.users.User;
import com.wahid.everyDollar.repositories.expenses.ExpenseRepository;
import com.wahid.everyDollar.requests.expenses.ExpenseRequest;
import com.wahid.everyDollar.services.categories.CategoryService;
import com.wahid.everyDollar.services.expenses.ExpenseService;
import com.wahid.everyDollar.services.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional
@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public Map<Long, BigDecimal> getSumByCategories(List<Long> categoryIds, Long userId,
                                                    LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> result = expenseRepository.getSumByCategoryAndDateRange(categoryIds, userId, startDate, endDate);

        Map<Long, BigDecimal> categorySumMap = result.stream()
                .collect(Collectors.toMap(
                        map -> (Long) map.get("categoryId"),
                        map -> (BigDecimal) map.get("totalAmount")
                ));

        return categorySumMap;
    }

    @Override
    public Page<Expense> getCurrentCategoryExpenses(String userEmail, Long categoryId, int page, int size, String sortDirection,
                                                    String sortBy, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userService.getUser(userEmail);

        Sort.Direction sortDir = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));

        Page<Expense> expenses = null;
        if (categoryId == 0) {
            expenses = expenseRepository.findByUserId(
                    pageable, user.getId(), startDate, endDate);
        } else {
            Category category = categoryService.getCategoryIfItBelongsToTheUser(user.getId(), categoryId);
            expenses = expenseRepository.findByUserIdAndCategoryIdAndDateRange(
                    pageable, user.getId(), category.getId(), startDate, endDate);
        }

        return expenses;
    }

    @Override
    public BigDecimal getTotalForCurrentCategory(Long userId, Long categoryId,
                                                 LocalDateTime startDate, LocalDateTime endDate) {
        if (categoryId == 0) {
            return expenseRepository.getSumForCurrentUser(userId, startDate, endDate)
                    .orElse(new BigDecimal("0.0"));
        }
        return expenseRepository.getSumForCurrentCategory(userId, categoryId, startDate, endDate)
                .orElse(new BigDecimal("0.0"));
    }

    @Override
    public Expense createExpense(String userEmail, ExpenseRequest expenseRequest) {
        User user = userService.getUser(userEmail);

        Category category = categoryService.getCategoryIfItBelongsToTheUser(user.getId(), expenseRequest.getCategoryId());

        Expense expense = new Expense();
        expense.setTitle(expenseRequest.getTitle());
        expense.setDescription(expenseRequest.getDescription());
        expense.setCategory(category);
        expense.setAmount(expenseRequest.getAmount());
        expense.setNotes(expenseRequest.getNotes());
        expense.setCreatedAt(expenseRequest.getCreatedAt());

        try {
            TransactionMethod transactionMethod = TransactionMethod.valueOf(expenseRequest.getTransactionMethod().toUpperCase());
            expense.setTransactionMethod(transactionMethod);

            TransactionType transactionType = TransactionType.valueOf(expenseRequest.getTransactionType().toUpperCase());
            expense.setTransactionType(transactionType);

            expense.setUser(user);

            return expenseRepository.save(expense);
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid transaction type or method provided.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new CustomException("Error while saving transaction.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Expense updateExpense(String userEmail, Long expenseId, ExpenseRequest expenseRequest) {
        User user = userService.getUser(userEmail);

        Category category = categoryService.getCategoryIfItBelongsToTheUser(user.getId(), expenseRequest.getCategoryId());

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new CustomException("Invalid transaction id", HttpStatus.BAD_REQUEST));

        if (!expense.getUser().equals(user)) {
            throw new CustomException("Invalid transaction id", HttpStatus.BAD_REQUEST);
        }

        expense.setTitle(expenseRequest.getTitle());
        expense.setDescription(expenseRequest.getDescription());
        expense.setCategory(category);
        expense.setAmount(expenseRequest.getAmount());
        expense.setNotes(expenseRequest.getNotes());

        try {
            TransactionMethod transactionMethod = TransactionMethod.valueOf(expenseRequest.getTransactionMethod().toUpperCase());
            expense.setTransactionMethod(transactionMethod);

            TransactionType transactionType = TransactionType.valueOf(expenseRequest.getTransactionType().toUpperCase());
            expense.setTransactionType(transactionType);

            return expenseRepository.save(expense);
        } catch (IllegalArgumentException e) {
            throw new CustomException("Invalid transaction type or method provided.", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            throw new CustomException("Error while saving transaction.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void deleteExpense(String userEmail, Long expenseId) {
        User user = userService.getUser(userEmail);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new CustomException("Invalid transaction id", HttpStatus.BAD_REQUEST));

        if (!expense.getUser().equals(user)) {
            throw new CustomException("Invalid transaction id", HttpStatus.BAD_REQUEST);
        }

        try {
            expenseRepository.deleteById(expense.getId());
        } catch (Exception e) {
            throw new CustomException("Error while saving transaction.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
