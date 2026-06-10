package com.expensesplitter.controller;

import com.expensesplitter.dto.PersonalExpenseDto;
import com.expensesplitter.service.PersonalExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/personal-expenses")
@RequiredArgsConstructor
public class PersonalExpenseController {

    private final PersonalExpenseService personalExpenseService;

    @PostMapping
    public ResponseEntity<PersonalExpenseDto> create(@Valid @RequestBody PersonalExpenseDto dto) {
        return new ResponseEntity<>(personalExpenseService.createPersonalExpense(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PersonalExpenseDto>> getMyExpenses() {
        return ResponseEntity.ok(personalExpenseService.getMyPersonalExpenses());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        personalExpenseService.deletePersonalExpense(id);
        return ResponseEntity.noContent().build();
    }
}
