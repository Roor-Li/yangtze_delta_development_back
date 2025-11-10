package com.opengms.yddback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 城市基础信息实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "city_info")
public class CityInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 城市编码，如 341800
     */
    @Column(name = "city_code", unique = true, nullable = false, length = 50)
    private String cityCode;

//    public void setCityCode(String cityCode) { this.cityCode = cityCode; }

    /**
     * 城市名称，如 宣城市
     */
    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;

    /**
     * 省份编码，如 340000
     */
    @Column(name = "province_code", length = 50)
    private String provinceCode;

    /**
     * 省份名称，如 安徽省
     */
    @Column(name = "province_name", length = 100)
    private String provinceName;

    /**
     * 原始数据中的OBJECTID
     */
    @Column(name = "object_id")
    private Integer objectId;

    /**
     * 原始数据中的ORIG_FID
     */
    @Column(name = "orig_fid")
    private Integer origFid;

    /**
     * 创建时间
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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