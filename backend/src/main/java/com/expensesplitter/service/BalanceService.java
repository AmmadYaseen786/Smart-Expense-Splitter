package com.expensesplitter.service;

import com.expensesplitter.dto.BalanceResponse;
import com.expensesplitter.dto.DashboardDto;

import java.util.List;

public interface BalanceService {
    List<BalanceResponse> calculateGroupBalances(Long groupId);
    DashboardDto getDashboardStats();
}
