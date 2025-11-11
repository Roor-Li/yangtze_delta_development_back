package com.opengms.yddback.entity;

//import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//import org.hibernate.annotations.Type;
//import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 基础统计数据实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "basic_statistics_data",
        uniqueConstraints = @UniqueConstraint(columnNames = {"city_id", "year", "data_version"}))
//@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class BasicStatisticsData {

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
     * 包含所有基础统计项的JSON对象
     * 格式: {"population": 21540000, "gdp": 4371000000000, ...}
     */
//    @Type(type = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_content", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> dataContent;

    /**
     * 数据版本号，用于同一年多次导入
     */
    @Column(name = "data_version")
    private Integer dataVersion;

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

    @PrePersist
    protected void onCreate() {
        if (dataVersion == null) {
            dataVersion = 1;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}