package com.expensesplitter.service.impl;

import com.expensesplitter.dto.BalanceResponse;
import com.expensesplitter.dto.DashboardDto;
import com.expensesplitter.exception.ResourceNotFoundException;
import com.expensesplitter.model.Expense;
import com.expensesplitter.model.ExpenseContribution;
import com.expensesplitter.model.ExpenseParticipant;
import com.expensesplitter.model.Group;
import com.expensesplitter.model.GroupMember;
import com.expensesplitter.model.PersonalExpense;
import com.expensesplitter.model.SettlementTransaction;
import com.expensesplitter.model.User;
import com.expensesplitter.repository.ExpenseContributionRepository;
import com.expensesplitter.repository.ExpenseParticipantRepository;
import com.expensesplitter.repository.ExpenseRepository;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.GroupRepository;
import com.expensesplitter.repository.PersonalExpenseRepository;
import com.expensesplitter.repository.SettlementRepository;
import com.expensesplitter.service.BalanceService;
import com.expensesplitter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private static final double SETTLEMENT_TOLERANCE = 1.00;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseContributionRepository contributionRepository;
    private final ExpenseParticipantRepository participantRepository;
    private final PersonalExpenseRepository personalExpenseRepository;
    private final SettlementRepository settlementRepository;
    private final UserService userService;

    @Override
    public List<BalanceResponse> calculateGroupBalances(Long groupId) {
        User currentUser = userService.getCurrentUser();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied. You do not belong to this group.");
        }

        List<GroupMember> memberships = groupMemberRepository.findByGroupId(groupId);
        List<User> members = memberships.stream().map(GroupMember::getUser).collect(Collectors.toList());

        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        List<SettlementTransaction> settlements = settlementRepository.findByGroupId(groupId);

        Map<Long, Double> contributionsMap = new HashMap<>();
        Map<Long, Double> shareMap = new HashMap<>();
        Map<Long, Double> settlementsPaidMap = new HashMap<>();
        Map<Long, Double> settlementsReceivedMap = new HashMap<>();

        for (User member : members) {
            contributionsMap.put(member.getId(), 0.0);
            shareMap.put(member.getId(), 0.0);
            settlementsPaidMap.put(member.getId(), 0.0);
            settlementsReceivedMap.put(member.getId(), 0.0);
        }

        for (Expense expense : expenses) {
            List<ExpenseContribution> contributions = contributionRepository.findByExpenseId(expense.getId());
            for (ExpenseContribution c : contributions) {
                Long uid = c.getUser().getId();
                if (contributionsMap.containsKey(uid)) {
                    contributionsMap.put(uid, contributionsMap.get(uid) + c.getAmountContributed());
                }
            }

            List<ExpenseParticipant> participants = participantRepository.findByExpenseId(expense.getId());
            int n = participants.size();
            if (n > 0) {
                double share = expense.getAmount() / n;
                for (ExpenseParticipant p : participants) {
                    Long uid = p.getUser().getId();
                    if (shareMap.containsKey(uid)) {
                        shareMap.put(uid, shareMap.get(uid) + share);
                    }
                }
            }
        }

        for (SettlementTransaction st : settlements) {
            Long payerId = st.getPayer().getId();
            Long receiverId = st.getReceiver().getId();
            if (settlementsPaidMap.containsKey(payerId)) {
                settlementsPaidMap.put(payerId, settlementsPaidMap.get(payerId) + st.getAmount());
            }
            if (settlementsReceivedMap.containsKey(receiverId)) {
                settlementsReceivedMap.put(receiverId, settlementsReceivedMap.get(receiverId) + st.getAmount());
            }
        }

        List<BalanceResponse> responses = new ArrayList<>();
        for (User member : members) {
            double contributions = contributionsMap.get(member.getId());
            double share = shareMap.get(member.getId());
            double paid = settlementsPaidMap.get(member.getId());
            double received = settlementsReceivedMap.get(member.getId());
            double balance = contributions - share + paid - received;

            if (Math.abs(balance) <= SETTLEMENT_TOLERANCE) {
                balance = 0.0;
            }

            contributions = Math.round(contributions * 100.0) / 100.0;
            share = Math.round(share * 100.0) / 100.0;
            balance = Math.round(balance * 100.0) / 100.0;

            responses.add(BalanceResponse.builder()
                    .userId(member.getId())
                    .userName(member.getName())
                    .userEmail(member.getEmail())
                    .totalContributions(contributions)
                    .totalShare(share)
                    .finalBalance(balance)
                    .build());
        }

        return responses;
    }

    @Override
    public DashboardDto getDashboardStats() {
        User currentUser = userService.getCurrentUser();

        List<PersonalExpense> personalExpenses = personalExpenseRepository.findByUserId(currentUser.getId());
        double personalTotal = personalExpenses.stream()
                .mapToDouble(PersonalExpense::getAmount)
                .sum();

        List<GroupMember> memberships = groupMemberRepository.findByUserId(currentUser.getId());
        double groupTotalShare = 0.0;
        double outstanding = 0.0;
        double receivable = 0.0;

        for (GroupMember membership : memberships) {
            List<BalanceResponse> groupBalances = calculateGroupBalances(membership.getGroup().getId());
            for (BalanceResponse b : groupBalances) {
                if (b.getUserId().equals(currentUser.getId())) {
                    groupTotalShare += b.getTotalShare();
                    if (b.getFinalBalance() > 0) {
                        receivable += b.getFinalBalance();
                    } else if (b.getFinalBalance() < 0) {
                        outstanding += Math.abs(b.getFinalBalance());
                    }
                    break;
                }
            }
        }

        List<SettlementTransaction> userSettlements = settlementRepository
                .findByPayerIdOrReceiverIdOrderBySettledAtDesc(currentUser.getId(), currentUser.getId());
        int totalCompleted = userSettlements.size();
        double totalAmtSettled = userSettlements.stream()
                .mapToDouble(SettlementTransaction::getAmount)
                .sum();

        int pendingCount = 0;
        for (GroupMember membership : memberships) {
            List<BalanceResponse> groupBalances = calculateGroupBalances(membership.getGroup().getId());
            List<DebtSuggestion> suggestions = computeDebtResolution(groupBalances);
            for (DebtSuggestion s : suggestions) {
                if (s.fromUserId.equals(currentUser.getId()) || s.toUserId.equals(currentUser.getId())) {
                    pendingCount++;
                }
            }
        }

        List<DashboardDto.RecentExpenseDto> recentList = new ArrayList<>();

        for (PersonalExpense pe : personalExpenses) {
            recentList.add(DashboardDto.RecentExpenseDto.builder()
                    .title(pe.getTitle())
                    .amount(pe.getAmount())
                    .type("PERSONAL")
                    .groupName(null)
                    .date(pe.getDate())
                    .build());
        }

        List<ExpenseParticipant> participantRecords = participantRepository.findByUserId(currentUser.getId());
        for (ExpenseParticipant ep : participantRecords) {
            Expense expense = ep.getExpense();
            recentList.add(DashboardDto.RecentExpenseDto.builder()
                    .title(expense.getTitle())
                    .amount(expense.getAmount())
                    .type("GROUP")
                    .groupName(expense.getGroup().getName())
                    .date(expense.getDate())
                    .build());
        }

        List<DashboardDto.RecentExpenseDto> sortedRecent = recentList.stream()
                .sorted(Comparator.comparing(DashboardDto.RecentExpenseDto::getDate).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return DashboardDto.builder()
                .personalExpensesTotal(Math.round(personalTotal * 100.0) / 100.0)
                .groupExpensesTotal(Math.round(groupTotalShare * 100.0) / 100.0)
                .outstandingAmount(Math.round(outstanding * 100.0) / 100.0)
                .receivableAmount(Math.round(receivable * 100.0) / 100.0)
                .pendingSettlementsCount(pendingCount)
                .totalSettlementsCompleted(totalCompleted)
                .totalAmountSettled(Math.round(totalAmtSettled * 100.0) / 100.0)
                .recentExpenses(sortedRecent)
                .build();
    }

    private List<DebtSuggestion> computeDebtResolution(List<BalanceResponse> balances) {
        List<UserBalance> debtors = new ArrayList<>();
        List<UserBalance> creditors = new ArrayList<>();

        for (BalanceResponse b : balances) {
            double bal = b.getFinalBalance();
            if (bal < -SETTLEMENT_TOLERANCE) {
                debtors.add(new UserBalance(b.getUserId(), b.getUserName(), Math.abs(bal)));
            } else if (bal > SETTLEMENT_TOLERANCE) {
                creditors.add(new UserBalance(b.getUserId(), b.getUserName(), bal));
            }
        }

        List<DebtSuggestion> suggestions = new ArrayList<>();
        int iterations = 0;
        int maxIterations = debtors.size() + creditors.size() + 10;

        while (!debtors.isEmpty() && !creditors.isEmpty() && iterations < maxIterations) {
            iterations++;
            debtors.sort((a, b) -> Double.compare(b.balance, a.balance));
            creditors.sort((a, b) -> Double.compare(b.balance, a.balance));

            UserBalance d = debtors.get(0);
            UserBalance c = creditors.get(0);

            double amount = Math.min(d.balance, c.balance);
            if (amount > SETTLEMENT_TOLERANCE) {
                suggestions.add(new DebtSuggestion(d.id, d.name, c.id, c.name, amount));
            }

            d.balance -= amount;
            c.balance -= amount;

            if (d.balance < SETTLEMENT_TOLERANCE) {
                debtors.remove(0);
            }
            if (c.balance < SETTLEMENT_TOLERANCE) {
                creditors.remove(0);
            }
        }

        return suggestions;
    }

    private static class UserBalance {
        Long id;
        String name;
        double balance;

        UserBalance(Long id, String name, double balance) {
            this.id = id;
            this.name = name;
            this.balance = balance;
        }
    }

    private static class DebtSuggestion {
        Long fromUserId;
        String fromUserName;
        Long toUserId;
        String toUserName;
        double amount;

        DebtSuggestion(Long fromUserId, String fromUserName, Long toUserId, String toUserName, double amount) {
            this.fromUserId = fromUserId;
            this.fromUserName = fromUserName;
            this.toUserId = toUserId;
            this.toUserName = toUserName;
            this.amount = amount;
        }
    }
}
