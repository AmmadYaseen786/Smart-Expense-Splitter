package com.expensesplitter.controller;

import com.expensesplitter.dto.BalanceResponse;
import com.expensesplitter.dto.DashboardDto;
import com.expensesplitter.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/balances")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<BalanceResponse>> getGroupBalances(@PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.calculateGroupBalances(groupId));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboardStats() {
        return ResponseEntity.ok(balanceService.getDashboardStats());
    }
}
