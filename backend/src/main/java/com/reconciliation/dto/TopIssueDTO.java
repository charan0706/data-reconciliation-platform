package com.reconciliation.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopIssueDTO {
    private String name;
    private Long count;
    private Double percentage;
}

