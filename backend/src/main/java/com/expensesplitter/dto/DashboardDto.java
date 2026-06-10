package com.expensesplitter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {
    private Double personalExpensesTotal;
    private Double groupExpensesTotal;
    private Double outstandingAmount;
    private Double receivableAmount;
    private Integer pendingSettlementsCount;
    private Integer totalSettlementsCompleted;
    private Double totalAmountSettled;
    private List<RecentExpenseDto> recentExpenses;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentExpenseDto {
        private String title;
        private Double amount;
        private String type; // "PERSONAL" or "GROUP"
        private String groupName;
        private LocalDateTime date;
    }
}
