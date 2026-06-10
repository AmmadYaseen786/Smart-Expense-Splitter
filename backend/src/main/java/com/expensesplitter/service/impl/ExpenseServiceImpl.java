package com.expensesplitter.service.impl;

import com.expensesplitter.dto.ContributionDto;
import com.expensesplitter.dto.ExpenseRequest;
import com.expensesplitter.dto.ExpenseResponse;
import com.expensesplitter.dto.UserDto;
import com.expensesplitter.exception.ResourceNotFoundException;
import com.expensesplitter.model.Expense;
import com.expensesplitter.model.ExpenseContribution;
import com.expensesplitter.model.ExpenseParticipant;
import com.expensesplitter.model.Group;
import com.expensesplitter.model.User;
import com.expensesplitter.repository.ExpenseContributionRepository;
import com.expensesplitter.repository.ExpenseParticipantRepository;
import com.expensesplitter.repository.ExpenseRepository;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.GroupRepository;
import com.expensesplitter.repository.UserRepository;
import com.expensesplitter.service.ExpenseService;
import com.expensesplitter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseContributionRepository contributionRepository;
    private final ExpenseParticipantRepository participantRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest expenseRequest) {
        User currentUser = userService.getCurrentUser();

        Group group = groupRepository.findById(expenseRequest.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + expenseRequest.getGroupId()));

        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied. You do not belong to this group.");
        }

        double totalContributed = expenseRequest.getContributions().stream()
                .mapToDouble(ContributionDto::getAmountContributed)
                .sum();
        if (Math.abs(totalContributed - expenseRequest.getAmount()) > 0.01) {
            throw new IllegalArgumentException("Sum of contributions (Rs. " + totalContributed + ") must equal the total expense amount (Rs. " + expenseRequest.getAmount() + ")");
        }

        Expense expense = Expense.builder()
                .title(expenseRequest.getTitle())
                .description(expenseRequest.getDescription())
                .amount(expenseRequest.getAmount())
                .group(group)
                .createdBy(currentUser)
                .date(LocalDateTime.now())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        for (ContributionDto contribDto : expenseRequest.getContributions()) {
            User contributor = userRepository.findById(contribDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contributor user not found with id: " + contribDto.getUserId()));

            ExpenseContribution contribution = ExpenseContribution.builder()
                    .expense(savedExpense)
                    .user(contributor)
                    .amountContributed(contribDto.getAmountContributed())
                    .build();
            contributionRepository.save(contribution);
        }

        for (Long participantId : expenseRequest.getParticipantUserIds()) {
            User participant = userRepository.findById(participantId)
                    .orElseThrow(() -> new ResourceNotFoundException("Participant user not found with id: " + participantId));

            ExpenseParticipant expenseParticipant = ExpenseParticipant.builder()
                    .expense(savedExpense)
                    .user(participant)
                    .build();
            participantRepository.save(expenseParticipant);
        }

        return mapToResponse(savedExpense);
    }

    @Override
    public List<ExpenseResponse> getExpensesByGroupId(Long groupId) {
        User currentUser = userService.getCurrentUser();

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied. You do not belong to this group.");
        }

        return expenseRepository.findByGroupId(groupId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ExpenseResponse getExpenseById(Long expenseId) {
        User currentUser = userService.getCurrentUser();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        if (!groupMemberRepository.existsByGroupIdAndUserId(expense.getGroup().getId(), currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied. You do not belong to the group associated with this expense.");
        }

        return mapToResponse(expense);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        List<ExpenseContribution> contributions = contributionRepository.findByExpenseId(expense.getId());
        List<ContributionDto> contributionDtos = contributions.stream()
                .map(c -> ContributionDto.builder()
                        .userId(c.getUser().getId())
                        .userName(c.getUser().getName())
                        .userEmail(c.getUser().getEmail())
                        .amountContributed(c.getAmountContributed())
                        .build())
                .collect(Collectors.toList());

        List<ExpenseParticipant> participants = participantRepository.findByExpenseId(expense.getId());
        List<UserDto> participantDtos = participants.stream()
                .map(p -> UserDto.builder()
                        .id(p.getUser().getId())
                        .name(p.getUser().getName())
                        .email(p.getUser().getEmail())
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .groupId(expense.getGroup().getId())
                .groupName(expense.getGroup().getName())
                .createdById(expense.getCreatedBy().getId())
                .createdByName(expense.getCreatedBy().getName())
                .date(expense.getDate())
                .contributions(contributionDtos)
                .participants(participantDtos)
                .build();
    }
}
