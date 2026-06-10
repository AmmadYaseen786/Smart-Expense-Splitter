package com.expensesplitter.service;

import com.expensesplitter.dto.SettlementRequest;
import com.expensesplitter.dto.SettlementResponse;

import java.util.List;

public interface SettlementService {
    SettlementResponse recordSettlement(SettlementRequest request);
    List<SettlementResponse> getMySettlements();
}
