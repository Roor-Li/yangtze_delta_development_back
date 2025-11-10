package com.opengms.yddback.repository;

import com.opengms.yddback.entity.IndicatorDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 指标定义Repository
 */
@Repository
public interface IndicatorDefinitionRepository extends JpaRepository<IndicatorDefinition, Long> {

    /**
     * 根据指标编码查询
     */
    Optional<IndicatorDefinition> findByIndicatorCode(String indicatorCode);

    /**
     * 根据维度ID查询所有指标，按显示顺序排序
     */
    List<IndicatorDefinition> findByDimensionIdOrderByDisplayOrder(Long dimensionId);

    /**
     * 根据维度ID查询所有启用的指标
     */
    List<IndicatorDefinition> findByDimensionIdAndIsActiveTrueOrderByDisplayOrder(Long dimensionId);

    /**
     * 查询所有启用的指标
     */
    List<IndicatorDefinition> findByIsActiveTrueOrderByDimensionIdAscDisplayOrderAsc();

    /**
     * 批量查询维度的指标
     */
    @Query("SELECT i FROM IndicatorDefinition i WHERE i.dimensionId IN :dimensionIds ORDER BY i.dimensionId, i.displayOrder")
    List<IndicatorDefinition> findByDimensionIds(@Param("dimensionIds") List<Long> dimensionIds);

    /**
     * 根据维度ID统计指标数量
     */
    long countByDimensionId(Long dimensionId);

    /**
     * 检查指标编码是否存在
     */
    boolean existsByIndicatorCode(String indicatorCode);

    /**
     * 查询所有指标，按维度和顺序排序（用于前端展示）
     */
    @Query("SELECT i FROM IndicatorDefinition i ORDER BY i.dimensionId, i.displayOrder")
    List<IndicatorDefinition> findAllOrderedByDimensionAndDisplay();
}