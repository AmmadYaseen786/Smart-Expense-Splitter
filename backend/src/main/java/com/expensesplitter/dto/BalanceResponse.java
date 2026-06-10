package com.expensesplitter.dto;

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
public class BalanceResponse {
    private Long userId;
    private String userName;
    private String userEmail;
    private Double totalContributions;
    private Double totalShare;
    private Double finalBalance;
}
