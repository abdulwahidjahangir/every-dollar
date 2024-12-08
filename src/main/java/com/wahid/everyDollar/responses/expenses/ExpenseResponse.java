package com.wahid.everyDollar.responses.expenses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExpenseResponse {
    private List<ExpenseDTO> expenseDTOs;
    private int pageNumber;
    private int pageSize;
    private Long totalExpenses;
    private int totalPages;
}
