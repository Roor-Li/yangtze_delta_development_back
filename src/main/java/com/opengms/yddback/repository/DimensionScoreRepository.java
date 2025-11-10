package com.opengms.yddback.repository;

import com.opengms.yddback.entity.DimensionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 维度得分Repository
 */
@Repository
public interface DimensionScoreRepository extends JpaRepository<DimensionScore, Long> {

    /**
     * 查询某年所有城市的所有维度得分
     */
    List<DimensionScore> findByYear(Integer year);

    /**
     * 查询某城市某年的所有维度得分
     */
    List<DimensionScore> findByCityIdAndYear(Long cityId, Integer year);

    /**
     * 查询某城市某年某维度的得分
     */
    Optional<DimensionScore> findByCityIdAndYearAndDimensionId(Long cityId, Integer year, Long dimensionId);

    /**
     * 查询某年某维度的所有城市得分，按得分降序
     */
    List<DimensionScore> findByYearAndDimensionIdOrderByDimensionScoreDesc(Integer year, Long dimensionId);

    /**
     * 查询某年某维度的所有城市得分，按得分升序
     */
    List<DimensionScore> findByYearAndDimensionIdOrderByDimensionScoreAsc(Integer year, Long dimensionId);

    /**
     * 查询某年某维度的所有城市得分，按排名排序
     */
    List<DimensionScore> findByYearAndDimensionIdOrderByRanking(Integer year, Long dimensionId);

    /**
     * 查询某城市多年某维度的得分趋势
     */
    @Query("SELECT ds FROM DimensionScore ds WHERE ds.cityId = :cityId AND ds.dimensionId = :dimensionId AND ds.year BETWEEN :startYear AND :endYear ORDER BY ds.year")
    List<DimensionScore> findTrendData(
            @Param("cityId") Long cityId,
            @Param("dimensionId") Long dimensionId,
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear
    );

    /**
     * 查询某城市所有年份某维度的得分，按年份降序
     */
    List<DimensionScore> findByCityIdAndDimensionIdOrderByYearDesc(Long cityId, Long dimensionId);

    /**
     * 查询某年某维度得分前N名的城市
     */
    @Query("SELECT ds FROM DimensionScore ds WHERE ds.year = :year AND ds.dimensionId = :dimensionId ORDER BY ds.dimensionScore DESC")
    List<DimensionScore> findTopCitiesByYearAndDimension(@Param("year") Integer year, @Param("dimensionId") Long dimensionId);

    /**
     * 批量查询城市某年的维度得分
     */
    @Query("SELECT ds FROM DimensionScore ds WHERE ds.cityId IN :cityIds AND ds.year = :year")
    List<DimensionScore> findByCityIdsAndYear(@Param("cityIds") List<Long> cityIds, @Param("year") Integer year);

    /**
     * 查询某年某维度的平均分
     */
    @Query("SELECT AVG(ds.dimensionScore) FROM DimensionScore ds WHERE ds.year = :year AND ds.dimensionId = :dimensionId")
    Optional<BigDecimal> findAverageScoreByYearAndDimension(@Param("year") Integer year, @Param("dimensionId") Long dimensionId);

    /**
     * 查询某年某维度的最高分
     */
    @Query("SELECT MAX(ds.dimensionScore) FROM DimensionScore ds WHERE ds.year = :year AND ds.dimensionId = :dimensionId")
    Optional<BigDecimal> findMaxScoreByYearAndDimension(@Param("year") Integer year, @Param("dimensionId") Long dimensionId);

    /**
     * 查询某年某维度的最低分
     */
    @Query("SELECT MIN(ds.dimensionScore) FROM DimensionScore ds WHERE ds.year = :year AND ds.dimensionId = :dimensionId")
    Optional<BigDecimal> findMinScoreByYearAndDimension(@Param("year") Integer year, @Param("dimensionId") Long dimensionId);

    /**
     * 查询某年某城市的维度数量
     */
    @Query("SELECT COUNT(ds) FROM DimensionScore ds WHERE ds.cityId = :cityId AND ds.year = :year")
    long countByCityIdAndYear(@Param("cityId") Long cityId, @Param("year") Integer year);

    /**
     * 检查某城市某年某维度是否存在
     */
    boolean existsByCityIdAndYearAndDimensionId(Long cityId, Integer year, Long dimensionId);

    /**
     * 查询某城市某年综合得分（所有维度平均分）
     */
    @Query("SELECT AVG(ds.dimensionScore) FROM DimensionScore ds WHERE ds.cityId = :cityId AND ds.year = :year")
    Optional<BigDecimal> findOverallScoreByCityAndYear(@Param("cityId") Long cityId, @Param("year") Integer year);
}