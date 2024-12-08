package com.wahid.everyDollar.requests.expenses;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseRequest {

    @NotNull(message = "Title is required for the transaction.")
    @NotEmpty(message = "Title cannot be empty.")
    private String title;

    private String description;

    @NotNull(message = "Category ID is required for the transaction.")
    @Positive(message = "Category ID must be a positive number.")
    private Long categoryId;

    @NotNull(message = "Amount is required for the transaction.")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required.")
    @NotEmpty(message = "Transaction type cannot be empty.")
    private String transactionType;

    @NotNull(message = "Transaction method is required.")
    @NotEmpty(message = "Transaction method cannot be empty.")
    private String transactionMethod;

    private String notes;

    private LocalDateTime createdAt;
}

