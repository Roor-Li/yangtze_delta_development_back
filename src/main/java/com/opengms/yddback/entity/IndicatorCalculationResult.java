package com.opengms.yddback.entity;

//import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//import org.hibernate.annotations.Type;
//import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 指标计算结果实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "indicator_calculation_result",
        uniqueConstraints = @UniqueConstraint(columnNames = {"city_id", "year", "indicator_id"}))
//@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class IndicatorCalculationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 城市ID
     */
    @Column(name = "city_id", nullable = false)
    private Long cityId;

    /**
     * 年份
     */
    @Column(name = "year", nullable = false)
    private Integer year;

    /**
     * 维度ID
     */
    @Column(name = "dimension_id", nullable = false)
    private Long dimensionId;

    /**
     * 指标ID
     */
    @Column(name = "indicator_id", nullable = false)
    private Long indicatorId;

    /**
     * 指标计算值
     */
    @Column(name = "indicator_value", precision = 20, scale = 6)
    private BigDecimal indicatorValue;

    /**
     * 计算详情（可选）
     */
//    @Type(type = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "calculation_detail", columnDefinition = "jsonb")
    private Map<String, Object> calculationDetail;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 关联城市信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", insertable = false, updatable = false)
    private CityInfo city;

    /**
     * 关联维度信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_id", insertable = false, updatable = false)
    private DimensionDefinition dimension;

    /**
     * 关联指标信息
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id", insertable = false, updatable = false)
    private IndicatorDefinition indicator;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}