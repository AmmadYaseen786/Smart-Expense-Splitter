package com.expensesplitter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDto {
    private Long id;

    @NotBlank(message = "Group name is required")
    private String name;

    private String description;
    private Long createdById;
    private String createdByName;
    private List<UserDto> members;
}
