package com.expensesplitter.controller;

import com.expensesplitter.dto.SettlementRequest;
import com.expensesplitter.dto.SettlementResponse;
import com.expensesplitter.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @PostMapping
    public ResponseEntity<SettlementResponse> recordSettlement(@Valid @RequestBody SettlementRequest request) {
        return new ResponseEntity<>(settlementService.recordSettlement(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> getMySettlements() {
        return ResponseEntity.ok(settlementService.getMySettlements());
    }
}
