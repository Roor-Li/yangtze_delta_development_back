package com.opengms.yddback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 年度报告实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "annual_report")
public class AnnualReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 年份
     */
    @Column(name = "year", unique = true, nullable = false)
    private Integer year;

    /**
     * 报告名称
     */
    @Column(name = "report_name", nullable = false)
    private String reportName;

    /**
     * 报告文件URL
     */
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    /**
     * 文件存储路径
     */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件格式
     */
    @Column(name = "file_format", length = 20)
    private String fileFormat;

    /**
     * 访问令牌，用于安全下载
     */
    @Column(name = "access_token", length = 100)
    private String accessToken;

    /**
     * 下载次数
     */
    @Column(name = "download_count")
    private Integer downloadCount;

    /**
     * 状态: 1-有效, 0-无效
     */
    @Column(name = "status")
    private Short status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (fileFormat == null) {
            fileFormat = "PDF";
        }
        if (downloadCount == null) {
            downloadCount = 0;
        }
        if (status == null) {
            status = 1;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 状态常量
     */
    public static class Status {
        public static final short ACTIVE = 1;
        public static final short INACTIVE = 0;
    }
}