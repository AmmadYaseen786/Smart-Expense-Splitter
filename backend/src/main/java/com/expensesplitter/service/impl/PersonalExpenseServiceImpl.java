package com.expensesplitter.service.impl;

import com.expensesplitter.dto.PersonalExpenseDto;
import com.expensesplitter.exception.ResourceNotFoundException;
import com.expensesplitter.model.PersonalExpense;
import com.expensesplitter.model.User;
import com.expensesplitter.repository.PersonalExpenseRepository;
import com.expensesplitter.service.PersonalExpenseService;
import com.expensesplitter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonalExpenseServiceImpl implements PersonalExpenseService {

    private final PersonalExpenseRepository personalExpenseRepository;
    private final UserService userService;

    @Override
    public PersonalExpenseDto createPersonalExpense(PersonalExpenseDto dto) {
        User currentUser = userService.getCurrentUser();

        PersonalExpense pe = PersonalExpense.builder()
                .title(dto.getTitle())
                .amount(dto.getAmount())
                .category(dto.getCategory())
                .date(dto.getDate() != null ? dto.getDate() : LocalDateTime.now())
                .user(currentUser)
                .build();

        PersonalExpense saved = personalExpenseRepository.save(pe);
        return mapToDto(saved);
    }

    @Override
    public List<PersonalExpenseDto> getMyPersonalExpenses() {
        User currentUser = userService.getCurrentUser();
        return personalExpenseRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePersonalExpense(Long id) {
        User currentUser = userService.getCurrentUser();
        PersonalExpense pe = personalExpenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personal expense not found with id: " + id));

        if (!pe.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied. You do not own this personal expense.");
        }

        personalExpenseRepository.delete(pe);
    }

    private PersonalExpenseDto mapToDto(PersonalExpense pe) {
        return PersonalExpenseDto.builder()
                .id(pe.getId())
                .title(pe.getTitle())
                .amount(pe.getAmount())
                .category(pe.getCategory())
                .date(pe.getDate())
                .userId(pe.getUser().getId())
                .build();
    }
}
