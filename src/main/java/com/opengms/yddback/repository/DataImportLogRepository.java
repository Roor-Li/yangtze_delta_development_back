package com.opengms.yddback.repository;

import com.opengms.yddback.entity.DataImportLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据导入日志Repository
 */
@Repository
public interface DataImportLogRepository extends JpaRepository<DataImportLog, Long> {

    /**
     * 查询某年的导入日志
     */
    List<DataImportLog> findByImportYearOrderByCreatedAtDesc(Integer importYear);

    /**
     * 查询某类型的导入日志
     */
    List<DataImportLog> findByImportTypeOrderByCreatedAtDesc(String importType);

    /**
     * 查询某状态的导入日志
     */
    List<DataImportLog> findByImportStatusOrderByCreatedAtDesc(String importStatus);

    /**
     * 查询某年某类型的导入日志
     */
    List<DataImportLog> findByImportYearAndImportTypeOrderByCreatedAtDesc(Integer importYear, String importType);

    /**
     * 分页查询所有日志，按创建时间降序
     */
    Page<DataImportLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 查询时间范围内的日志
     */
    List<DataImportLog> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询某操作人的导入记录
     */
    List<DataImportLog> findByOperatorOrderByCreatedAtDesc(String operator);

    /**
     * 查询最近N条导入记录
     */
    @Query("SELECT d FROM DataImportLog d ORDER BY d.createdAt DESC")
    List<DataImportLog> findRecentLogs(Pageable pageable);

    /**
     * 统计某年的导入次数
     */
    long countByImportYear(Integer importYear);

    /**
     * 统计某状态的导入次数
     */
    long countByImportStatus(String importStatus);

    /**
     * 查询某年最新的导入记录
     */
    @Query("SELECT d FROM DataImportLog d WHERE d.importYear = :year ORDER BY d.createdAt DESC")
    List<DataImportLog> findLatestByYear(@Param("year") Integer year, Pageable pageable);

    /**
     * 查询失败的导入记录
     */
    @Query("SELECT d FROM DataImportLog d WHERE d.importStatus IN ('failed', 'partial') ORDER BY d.createdAt DESC")
    List<DataImportLog> findFailedImports();
}
