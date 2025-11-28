package com.reconciliation.entity;

import com.reconciliation.enums.ComparisonType;
import com.reconciliation.enums.DiscrepancySeverity;
import lombok.*;

import javax.persistence.*;

/**
 * Defines mapping between source and target attributes for comparison.
 */
@Entity
@Table(name = "attribute_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeMapping extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_config_id", nullable = false)
    private ReconciliationConfig reconciliationConfig;

    @Column(name = "source_attribute", nullable = false, length = 200)
    private String sourceAttribute;

    @Column(name = "target_attribute", nullable = false, length = 200)
    private String targetAttribute;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Column(name = "data_type", length = 50)
    private String dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_type", nullable = false, length = 50)
    private ComparisonType comparisonType;

    @Column(name = "tolerance_value")
    private Double toleranceValue;

    @Column(name = "tolerance_type", length = 50)
    private String toleranceType;

    @Column(name = "transformation_expression", length = 1000)
    private String transformationExpression;

    @Column(name = "source_transformation", length = 1000)
    private String sourceTransformation;

    @Column(name = "target_transformation", length = 1000)
    private String targetTransformation;

    @Column(name = "is_key_attribute")
    private Boolean isKeyAttribute = false;

    @Column(name = "is_required")
    private Boolean isRequired = true;

    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "mismatch_severity", length = 20)
    private DiscrepancySeverity mismatchSeverity = DiscrepancySeverity.MEDIUM;

    @Column(name = "custom_validation_rule", length = 1000)
    private String customValidationRule;

    @Column(name = "null_handling", length = 50)
    private String nullHandling;

    @Column(name = "format_pattern", length = 200)
    private String formatPattern;
}

