package com.reconciliation.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendDataDTO {
    private String date;
    private Long count;
    private Double percentage;
    private String label;
}

