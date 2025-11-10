package com.opengms.yddback.repository;

import com.opengms.yddback.entity.IndicatorCalculationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 指标计算结果Repository
 */
@Repository
public interface IndicatorCalculationResultRepository extends JpaRepository<IndicatorCalculationResult, Long> {

    /**
     * 查询某年所有城市的指标结果
     */
    List<IndicatorCalculationResult> findByYear(Integer year);

    /**
     * 查询某城市某年的所有指标结果
     */
    List<IndicatorCalculationResult> findByCityIdAndYear(Long cityId, Integer year);

    /**
     * 查询某城市某年某维度的所有指标
     */
    List<IndicatorCalculationResult> findByCityIdAndYearAndDimensionId(Long cityId, Integer year, Long dimensionId);

    /**
     * 查询某城市某年某指标的结果
     */
    Optional<IndicatorCalculationResult> findByCityIdAndYearAndIndicatorId(Long cityId, Integer year, Long indicatorId);

    /**
     * 查询某年某指标的所有城市结果，按指标值降序
     */
    List<IndicatorCalculationResult> findByYearAndIndicatorIdOrderByIndicatorValueDesc(Integer year, Long indicatorId);

    /**
     * 查询某年某指标的所有城市结果，按指标值升序
     */
    List<IndicatorCalculationResult> findByYearAndIndicatorIdOrderByIndicatorValueAsc(Integer year, Long indicatorId);

    /**
     * 查询某年某维度的所有指标结果
     */
    List<IndicatorCalculationResult> findByYearAndDimensionId(Integer year, Long dimensionId);

    /**
     * 查询某城市多年某指标的趋势数据
     */
    @Query("SELECT i FROM IndicatorCalculationResult i WHERE i.cityId = :cityId AND i.indicatorId = :indicatorId AND i.year BETWEEN :startYear AND :endYear ORDER BY i.year")
    List<IndicatorCalculationResult> findTrendData(
            @Param("cityId") Long cityId,
            @Param("indicatorId") Long indicatorId,
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear
    );

    /**
     * 批量查询城市某年的指标结果
     */
    @Query("SELECT i FROM IndicatorCalculationResult i WHERE i.cityId IN :cityIds AND i.year = :year")
    List<IndicatorCalculationResult> findByCityIdsAndYear(@Param("cityIds") List<Long> cityIds, @Param("year") Integer year);

    /**
     * 查询某年某指标值最高的前N个城市
     */
    @Query("SELECT i FROM IndicatorCalculationResult i WHERE i.year = :year AND i.indicatorId = :indicatorId ORDER BY i.indicatorValue DESC")
    List<IndicatorCalculationResult> findTopCitiesByIndicator(@Param("year") Integer year, @Param("indicatorId") Long indicatorId);

    /**
     * 查询某年某城市的指标数量
     */
    @Query("SELECT COUNT(i) FROM IndicatorCalculationResult i WHERE i.cityId = :cityId AND i.year = :year")
    long countByCityIdAndYear(@Param("cityId") Long cityId, @Param("year") Integer year);

    /**
     * 检查某城市某年某指标是否存在
     */
    boolean existsByCityIdAndYearAndIndicatorId(Long cityId, Integer year, Long indicatorId);
}