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
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 城市地理数据实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "city_geometry",
        uniqueConstraints = @UniqueConstraint(columnNames = {"city_id", "geometry_type"}))
//@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CityGeometry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 城市ID
     */
    @Column(name = "city_id", nullable = false)
    private Long cityId;

    /**
     * 几何类型: boundary-边界, center_point-中心点
     */
    @Column(name = "geometry_type", nullable = false, length = 20)
    private String geometryType;

    /**
     * 完整的GeoJSON Feature对象
     */
//    @Type(type = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "geojson_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> geojsonData;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 关联城市信息（可选，用于级联查询）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", insertable = false, updatable = false)
    private CityInfo city;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 几何类型常量
     */
    public static class GeometryType {
        public static final String BOUNDARY = "boundary";
        public static final String CENTER_POINT = "center_point";
    }
}
