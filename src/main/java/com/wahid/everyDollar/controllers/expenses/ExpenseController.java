package com.wahid.everyDollar.controllers.expenses;

import com.wahid.everyDollar.models.expenses.Expense;
import com.wahid.everyDollar.requests.expenses.ExpenseRequest;
import com.wahid.everyDollar.responses.expenses.ExpenseDTO;
import com.wahid.everyDollar.responses.expenses.ExpenseResponse;
import com.wahid.everyDollar.security.service.UserDetailsImpl;
import com.wahid.everyDollar.services.expenses.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/expenses")
    private ResponseEntity<?> getAllMyExpense(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "category-id", defaultValue = "0") Long categoryId,
            @Valid @RequestParam(name = "page", defaultValue = "0")
            @PositiveOrZero(message = "Page number must be greater than or equal to zero") int page,
            @Valid @RequestParam(name = "page-size", defaultValue = "10")
            @Positive(message = "Page must be greater than zero") int pageSize,
            @RequestParam(name = "sort-by", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sort-direction", defaultValue = "DESC") String sortDirection,
            @Valid @RequestParam(name = "from", required = false) @DateTimeFormat String from,
            @Valid @RequestParam(name = "to", required = false) @DateTimeFormat String to
    ) {
        if (pageSize > 50) {
            pageSize = 50;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDayOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime lastDayOfMonth = now.withDayOfMonth(now.getMonth().length(now.toLocalDate().isLeapYear()))
                .withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        LocalDateTime fromDate = (from != null && !from.isEmpty()) ? LocalDateTime.parse(from + "T00:00:00") : firstDayOfMonth;
        LocalDateTime toDate = (to != null && !to.isEmpty()) ? LocalDateTime.parse(to + "T23:59:59") : lastDayOfMonth;

        Page<Expense> expenses = expenseService.getCurrentCategoryExpenses(
                userDetails.getEmail(), categoryId, page, pageSize, sortDirection, sortBy, fromDate, toDate
        );

        List<ExpenseDTO> expenseDTOs = expenses.stream()
                .map(this::convertExpenseIntoExpenseDto)
                .toList();

        ExpenseResponse expenseResponse = new ExpenseResponse();
        expenseResponse.setExpenseDTOs(expenseDTOs);
        expenseResponse.setPageSize(expenses.getPageable().getPageSize());
        expenseResponse.setPageNumber(expenses.getPageable().getPageNumber());
        expenseResponse.setTotalPages(expenses.getTotalPages());
        expenseResponse.setTotalExpenses(expenses.getTotalElements());

        BigDecimal totalExpense = expenseService.getTotalForCurrentCategory(userDetails.getId(), categoryId, fromDate, toDate);

        Map<String, Object> res = new HashMap<>();
        res.put("data", expenseResponse);
        res.put("totalExpense", totalExpense);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/expenses")
    public ResponseEntity<?> createExpense(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ExpenseRequest expenseRequest
    ) {
        Expense expense = expenseService.createExpense(userDetails.getEmail(), expenseRequest);

        ExpenseDTO expenseDTO = convertExpenseIntoExpenseDto(expense);
        return ResponseEntity.ok(expenseDTO);
    }

    @PutMapping("/expenses")
    public ResponseEntity<?> updateExpense(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "expense-id") @Valid @NotNull(message = "Please provide a valid expense id")
            @Positive(message = "Expense id can not be less than or equal to 0") Long expenseId,
            @Valid @RequestBody ExpenseRequest expenseRequest
    ) {
        Expense expense = expenseService.updateExpense(userDetails.getEmail(), expenseId, expenseRequest);

        ExpenseDTO expenseDTO = convertExpenseIntoExpenseDto(expense);
        return ResponseEntity.ok(expenseDTO);
    }

    @DeleteMapping("/expenses")
    public ResponseEntity<?> updateExpense(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name = "expense-id") @Valid @NotNull(message = "Please provide a valid expense id")
            @Positive(message = "Expense id can not be less than or equal to 0") Long expenseId
    ) {
        expenseService.deleteExpense(userDetails.getEmail(), expenseId);

        return ResponseEntity.ok("Expense successfully deleted");
    }

    private ExpenseDTO convertExpenseIntoExpenseDto(Expense expense) {
        ExpenseDTO expenseDTO = new ExpenseDTO();
        expenseDTO.setId(expense.getId());
        expenseDTO.setTitle(expense.getTitle());
        expenseDTO.setDescription(expense.getDescription());
        expenseDTO.setAmount(expense.getAmount());
        expenseDTO.setTransactionType(expense.getTransactionType());
        expenseDTO.setTransactionMethod(expense.getTransactionMethod());
        expenseDTO.setNotes(expense.getNotes());
        expenseDTO.setCreateAt(expense.getCreatedAt());
        expenseDTO.setUpdatedAt(expense.getUpdatedAt());
        return expenseDTO;
    }
}
