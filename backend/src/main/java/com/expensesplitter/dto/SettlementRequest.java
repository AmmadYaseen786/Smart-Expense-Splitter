package com.expensesplitter.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementRequest {

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Payer User ID is required")
    private Long payerUserId;

    @NotNull(message = "Receiver User ID is required")
    private Long receiverUserId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Double amount;

    private String note;
}
