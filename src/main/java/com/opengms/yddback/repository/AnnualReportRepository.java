package com.opengms.yddback.repository;

import com.opengms.yddback.entity.AnnualReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 年度报告Repository
 */
@Repository
public interface AnnualReportRepository extends JpaRepository<AnnualReport, Long> {

    /**
     * 根据年份查询报告
     */
    Optional<AnnualReport> findByYear(Integer year);

    /**
     * 根据访问令牌查询报告
     */
    Optional<AnnualReport> findByAccessToken(String accessToken);

    /**
     * 查询所有有效的报告，按年份降序
     */
    List<AnnualReport> findByStatusOrderByYearDesc(Short status);

    /**
     * 查询所有报告，按年份降序
     */
    List<AnnualReport> findAllByOrderByYearDesc();

    /**
     * 查询年份范围内的报告
     */
    List<AnnualReport> findByYearBetweenOrderByYearDesc(Integer startYear, Integer endYear);

    /**
     * 检查某年是否有报告
     */
    boolean existsByYear(Integer year);

    /**
     * 增加下载次数
     */
    @Modifying
    @Query("UPDATE AnnualReport ar SET ar.downloadCount = ar.downloadCount + 1 WHERE ar.id = :id")
    void incrementDownloadCount(@Param("id") Long id);

    /**
     * 根据年份增加下载次数
     */
    @Modifying
    @Query("UPDATE AnnualReport ar SET ar.downloadCount = ar.downloadCount + 1 WHERE ar.year = :year")
    void incrementDownloadCountByYear(@Param("year") Integer year);

    /**
     * 查询所有可用年份（有报告的年份）
     */
    @Query("SELECT ar.year FROM AnnualReport ar WHERE ar.status = 1 ORDER BY ar.year DESC")
    List<Integer> findAllAvailableYears();

    /**
     * 统计报告总数
     */
    @Query("SELECT COUNT(ar) FROM AnnualReport ar WHERE ar.status = 1")
    long countActiveReports();
}
