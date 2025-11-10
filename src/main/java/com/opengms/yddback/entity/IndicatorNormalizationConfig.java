package com.opengms.yddback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 指标归一化配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "indicator_normalization_config")
public class IndicatorNormalizationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 指标ID
     */
    @Column(name = "indicator_id", nullable = false)
    private Long indicatorId;

    /**
     * 指标代码（冗余字段）
     */
    @Column(name = "indicator_code", nullable = false, length = 50)
    private String indicatorCode;

    /**
     * 归一化方法：positive/negative/bidirection
     */
    @Column(name = "normalization_method", nullable = false, length = 50)
    private String normalizationMethod;

    /**
     * 归一化参数
     */
    @Column(name = "normalization_param", nullable = false, precision = 20, scale = 10)
    private BigDecimal normalizationParam;

    /**
     * 目标最小值
     */
    @Column(name = "target_min", precision = 10, scale = 6)
    private BigDecimal targetMin;

    /**
     * 目标最大值
     */
    @Column(name = "target_max", precision = 10, scale = 6)
    private BigDecimal targetMax;

    /**
     * 描述说明
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (targetMin == null) {
            targetMin = BigDecimal.ZERO;
        }
        if (targetMax == null) {
            targetMax = BigDecimal.ONE;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}