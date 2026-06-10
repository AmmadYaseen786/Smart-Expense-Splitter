package com.expensesplitter.service;

import com.expensesplitter.dto.ExpenseRequest;
import com.expensesplitter.dto.ExpenseResponse;

import java.util.List;

public interface ExpenseService {
    ExpenseResponse createExpense(ExpenseRequest expenseRequest);
    List<ExpenseResponse> getExpensesByGroupId(Long groupId);
    ExpenseResponse getExpenseById(Long expenseId);
}
