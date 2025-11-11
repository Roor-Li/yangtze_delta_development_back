package com.opengms.yddback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 数据导入日志实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "data_import_log")
public class DataImportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 导入年份
     */
    @Column(name = "import_year")
    private Integer importYear;

    /**
     * 导入类型
     */
    @Column(name = "import_type", length = 50)
    private String importType;

    /**
     * 导入文件名
     */
    @Column(name = "file_name")
    private String fileName;

    /**
     * 导入的城市数量
     */
    @Column(name = "city_count")
    private Integer cityCount;

    /**
     * 导入记录数
     */
    @Column(name = "record_count")
    private Integer recordCount;

    /**
     * 成功数量
     */
    @Column(name = "success_count")
    private Integer successCount;

    /**
     * 失败数量
     */
    @Column(name = "failed_count")
    private Integer failedCount;

    /**
     * 错误详情
     */
    @Column(name = "error_detail", columnDefinition = "TEXT")
    private String errorDetail;

    /**
     * 导入状态
     */
    @Column(name = "import_status", length = 20)
    private String importStatus;

    /**
     * 操作人
     */
    @Column(name = "operator", length = 100)
    private String operator;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 导入类型常量
     */
    public static class ImportType {
        public static final String BASIC_DATA = "basic_data";
        public static final String INDICATOR_RESULT = "indicator_result";
        public static final String DIMENSION_SCORE = "dimension_score";
        public static final String GEO_DATA = "geo_data";
        public static final String REPORT = "report";
        public static final String ALL = "all";
    }

    /**
     * 导入状态常量
     */
    public static class ImportStatus {
        public static final String PROCESSING = "processing";
        public static final String SUCCESS = "success";
        public static final String FAILED = "failed";
        public static final String PARTIAL = "partial";
    }
}
