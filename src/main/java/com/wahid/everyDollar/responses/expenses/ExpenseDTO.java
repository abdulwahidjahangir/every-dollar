package com.wahid.everyDollar.responses.expenses;

import com.wahid.everyDollar.models.expenses.TransactionMethod;
import com.wahid.everyDollar.models.expenses.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionMethod transactionMethod;
    private String notes;
    private LocalDateTime createAt;
    private LocalDateTime updatedAt;
}
