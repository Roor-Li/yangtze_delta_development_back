package com.opengms.yddback.repository;

import com.opengms.yddback.entity.BasicStatisticsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 基础统计数据Repository
 */
@Repository
public interface BasicStatisticsDataRepository extends JpaRepository<BasicStatisticsData, Long> {

    /**
     * 查询某年所有城市的数据
     */
    List<BasicStatisticsData> findByYear(Integer year);

    /**
     * 查询某城市某年的数据（最新版本）
     */
    @Query("SELECT b FROM BasicStatisticsData b WHERE b.cityId = :cityId AND b.year = :year ORDER BY b.dataVersion DESC")
    Optional<BasicStatisticsData> findLatestByCityIdAndYear(@Param("cityId") Long cityId, @Param("year") Integer year);

    /**
     * 查询某城市某年的数据（指定版本）
     */
    Optional<BasicStatisticsData> findByCityIdAndYearAndDataVersion(Long cityId, Integer year, Integer dataVersion);

    /**
     * 查询某城市多年的数据
     */
    List<BasicStatisticsData> findByCityIdAndYearBetween(Long cityId, Integer startYear, Integer endYear);

    /**
     * 查询某城市所有年份的数据，按年份降序
     */
    List<BasicStatisticsData> findByCityIdOrderByYearDesc(Long cityId);

    /**
     * 查询某城市所有年份的数据，按年份升序
     */
    List<BasicStatisticsData> findByCityIdOrderByYearAsc(Long cityId);

    /**
     * 查询所有可用年份（去重排序）
     */
    @Query("SELECT DISTINCT b.year FROM BasicStatisticsData b ORDER BY b.year DESC")
    List<Integer> findAllYears();

    /**
     * 查询某年的城市数量
     */
    @Query("SELECT COUNT(DISTINCT b.cityId) FROM BasicStatisticsData b WHERE b.year = :year")
    Long countCitiesByYear(@Param("year") Integer year);

    /**
     * 查询某城市有数据的年份列表
     */
    @Query("SELECT DISTINCT b.year FROM BasicStatisticsData b WHERE b.cityId = :cityId ORDER BY b.year DESC")
    List<Integer> findYearsByCityId(@Param("cityId") Long cityId);

    /**
     * 批量查询城市某年的数据
     */
    @Query("SELECT b FROM BasicStatisticsData b WHERE b.cityId IN :cityIds AND b.year = :year")
    List<BasicStatisticsData> findByCityIdsAndYear(@Param("cityIds") List<Long> cityIds, @Param("year") Integer year);

    /**
     * 检查某城市某年是否有数据
     */
    boolean existsByCityIdAndYear(Long cityId, Integer year);

    /**
     * 查询最新年份
     */
    @Query("SELECT MAX(b.year) FROM BasicStatisticsData b")
    Optional<Integer> findLatestYear();

    /**
     * 查询最早年份
     */
    @Query("SELECT MIN(b.year) FROM BasicStatisticsData b")
    Optional<Integer> findEarliestYear();
}
