package com.expensesplitter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponse {
    private Long id;
    private Long groupId;
    private String groupName;
    private Long payerUserId;
    private String payerName;
    private Long receiverUserId;
    private String receiverName;
    private Double amount;
    private LocalDateTime settledAt;
    private String note;
}
