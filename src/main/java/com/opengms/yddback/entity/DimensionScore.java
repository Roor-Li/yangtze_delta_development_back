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
 * 维度得分实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dimension_score",
        uniqueConstraints = @UniqueConstraint(columnNames = {"city_id", "year", "dimension_id"}))
//@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class DimensionScore {

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
     * 维度综合得分
     */
    @Column(name = "dimension_score", nullable = false, precision = 20, scale = 6)
    private BigDecimal dimensionScore;

    /**
     * 该维度在所有城市中的排名（可选）
     */
    @Column(name = "ranking")
    private Integer ranking;

    /**
     * 得分详情（可选）
     */
//    @Type(type = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_detail", columnDefinition = "jsonb")
    private Map<String, Object> scoreDetail;

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
