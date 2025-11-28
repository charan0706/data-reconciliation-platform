package com.reconciliation.dto;

import com.reconciliation.enums.ComparisonType;
import com.reconciliation.enums.DiscrepancySeverity;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeMappingDTO {
    
    private Long id;
    
    @NotBlank(message = "Source attribute is required")
    private String sourceAttribute;
    
    @NotBlank(message = "Target attribute is required")
    private String targetAttribute;
    
    private String displayName;
    private String dataType;
    
    @NotNull(message = "Comparison type is required")
    private ComparisonType comparisonType;
    
    private Double toleranceValue;
    private String toleranceType;
    private String transformationExpression;
    private String sourceTransformation;
    private String targetTransformation;
    private Boolean isKeyAttribute;
    private Boolean isRequired;
    private Boolean isEnabled;
    private Integer sortOrder;
    private DiscrepancySeverity mismatchSeverity;
    private String customValidationRule;
    private String nullHandling;
    private String formatPattern;
}

