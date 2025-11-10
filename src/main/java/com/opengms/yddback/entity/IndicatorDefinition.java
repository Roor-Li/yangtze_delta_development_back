package com.opengms.yddback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 指标定义实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "indicator_definition")
public class IndicatorDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 维度ID
     */
    @Column(name = "dimension_id", nullable = false)
    private Long dimensionId;

    /**
     * 指标编码，如 D1_I1
     */
    @Column(name = "indicator_code", unique = true, nullable = false, length = 50)
    private String indicatorCode;

    /**
     * 指标名称
     */
    @Column(name = "indicator_name", nullable = false, length = 100)
    private String indicatorName;

    /**
     * 指标描述
     */
    @Column(name = "indicator_description", columnDefinition = "TEXT")
    private String indicatorDescription;

    /**
     * 计算说明
     */
    @Column(name = "calculation_note", columnDefinition = "TEXT")
    private String calculationNote;

    /**
     * 显示顺序
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    /**
     * 是否启用
     */
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 关联维度信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_id", insertable = false, updatable = false)
    private DimensionDefinition dimension;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}