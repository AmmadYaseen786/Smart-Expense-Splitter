package com.expensesplitter.service;

import com.expensesplitter.dto.PersonalExpenseDto;

import java.util.List;

public interface PersonalExpenseService {
    PersonalExpenseDto createPersonalExpense(PersonalExpenseDto dto);
    List<PersonalExpenseDto> getMyPersonalExpenses();
    void deletePersonalExpense(Long id);
}
