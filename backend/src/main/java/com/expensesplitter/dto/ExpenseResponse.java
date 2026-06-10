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
public class ExpenseResponse {
    private Long id;
    private String title;
    private String description;
    private Double amount;
    private Long groupId;
    private String groupName;
    private Long createdById;
    private String createdByName;
    private LocalDateTime date;
    private List<ContributionDto> contributions;
    private List<UserDto> participants;
}
