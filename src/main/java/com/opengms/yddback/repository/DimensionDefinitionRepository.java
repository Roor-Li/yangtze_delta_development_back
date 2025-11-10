package com.opengms.yddback.repository;

import com.opengms.yddback.entity.DimensionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 维度定义Repository
 */
@Repository
public interface DimensionDefinitionRepository extends JpaRepository<DimensionDefinition, Long> {

    /**
     * 根据维度编码查询
     */
    Optional<DimensionDefinition> findByDimensionCode(String dimensionCode);

    /**
     * 查询所有启用的维度，按显示顺序排序
     */
    List<DimensionDefinition> findByIsActiveTrueOrderByDisplayOrder();

    /**
     * 查询所有维度，按显示顺序排序
     */
    List<DimensionDefinition> findAllByOrderByDisplayOrder();

    /**
     * 根据维度名称查询
     */
    Optional<DimensionDefinition> findByDimensionName(String dimensionName);

    /**
     * 检查维度编码是否存在
     */
    boolean existsByDimensionCode(String dimensionCode);

    /**
     * 统计启用的维度数量
     */
    @Query("SELECT COUNT(d) FROM DimensionDefinition d WHERE d.isActive = true")
    long countActiveDimensions();
}