package com.expensesplitter.dto;

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
public class ContributionDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String userName;
    private String userEmail;

    @NotNull(message = "Contribution amount is required")
    private Double amountContributed;
}
