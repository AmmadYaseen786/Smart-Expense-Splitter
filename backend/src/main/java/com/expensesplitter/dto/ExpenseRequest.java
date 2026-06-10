package com.expensesplitter.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExpenseRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotEmpty(message = "At least one contributor is required")
    @Valid
    private List<ContributionDto> contributions;

    @NotEmpty(message = "At least one participant is required")
    private List<Long> participantUserIds;
}
