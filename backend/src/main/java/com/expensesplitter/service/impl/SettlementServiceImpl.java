package com.expensesplitter.service.impl;

import com.expensesplitter.dto.SettlementRequest;
import com.expensesplitter.dto.SettlementResponse;
import com.expensesplitter.exception.ResourceNotFoundException;
import com.expensesplitter.model.Group;
import com.expensesplitter.model.SettlementTransaction;
import com.expensesplitter.model.User;
import com.expensesplitter.repository.GroupMemberRepository;
import com.expensesplitter.repository.GroupRepository;
import com.expensesplitter.repository.SettlementRepository;
import com.expensesplitter.repository.UserRepository;
import com.expensesplitter.service.SettlementService;
import com.expensesplitter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;

    @Override
    @Transactional
    public SettlementResponse recordSettlement(SettlementRequest request) {
        User currentUser = userService.getCurrentUser();

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + request.getGroupId()));

        User payer = userRepository.findById(request.getPayerUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found with id: " + request.getPayerUserId()));

        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found with id: " + request.getReceiverUserId()));

        // Validate group memberships
        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), payer.getId())) {
            throw new IllegalArgumentException("Payer does not belong to this group.");
        }
        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), receiver.getId())) {
            throw new IllegalArgumentException("Receiver does not belong to this group.");
        }

        // Validate that the logged in user is either the payer or the receiver (or is in the group)
        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), currentUser.getId())) {
            throw new IllegalArgumentException("Access Denied. You do not belong to this group.");
        }

        SettlementTransaction settlement = SettlementTransaction.builder()
                .group(group)
                .payer(payer)
                .receiver(receiver)
                .amount(request.getAmount())
                .settledAt(LocalDateTime.now())
                .note(request.getNote())
                .build();

        SettlementTransaction saved = settlementRepository.save(settlement);
        return mapToResponse(saved);
    }

    @Override
    public List<SettlementResponse> getMySettlements() {
        User currentUser = userService.getCurrentUser();
        List<SettlementTransaction> list = settlementRepository
                .findByPayerIdOrReceiverIdOrderBySettledAtDesc(currentUser.getId(), currentUser.getId());

        return list.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SettlementResponse mapToResponse(SettlementTransaction st) {
        return SettlementResponse.builder()
                .id(st.getId())
                .groupId(st.getGroup().getId())
                .groupName(st.getGroup().getName())
                .payerUserId(st.getPayer().getId())
                .payerName(st.getPayer().getName())
                .receiverUserId(st.getReceiver().getId())
                .receiverName(st.getReceiver().getName())
                .amount(st.getAmount())
                .settledAt(st.getSettledAt())
                .note(st.getNote())
                .build();
    }
}
