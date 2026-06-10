package com.expensesplitter.controller;

import com.expensesplitter.dto.ExpenseRequest;
import com.expensesplitter.dto.ExpenseResponse;
import com.expensesplitter.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest expenseRequest) {
        return new ResponseEntity<>(expenseService.createExpense(expenseRequest), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getExpenses(@RequestParam Long groupId) {
        return ResponseEntity.ok(expenseService.getExpensesByGroupId(groupId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }
}
