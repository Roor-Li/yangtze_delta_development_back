package com.opengms.yddback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 维度定义实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dimension_definition")
public class DimensionDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 维度编码，如 D1, D2
     */
    @Column(name = "dimension_code", unique = true, nullable = false, length = 50)
    private String dimensionCode;

    /**
     * 维度名称，如 经济发展
     */
    @Column(name = "dimension_name", nullable = false, length = 100)
    private String dimensionName;

    /**
     * 维度描述
     */
    @Column(name = "dimension_description", columnDefinition = "TEXT")
    private String dimensionDescription;

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
