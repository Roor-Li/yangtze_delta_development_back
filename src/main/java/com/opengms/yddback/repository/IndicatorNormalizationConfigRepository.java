package com.opengms.yddback.repository;

import com.opengms.yddback.entity.IndicatorNormalizationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 指标归一化配置Repository
 */
@Repository
public interface IndicatorNormalizationConfigRepository extends JpaRepository<IndicatorNormalizationConfig, Long> {

    /**
     * 根据指标ID查询配置
     */
    Optional<IndicatorNormalizationConfig> findByIndicatorId(Long indicatorId);

    /**
     * 根据指标代码查询配置
     */
    Optional<IndicatorNormalizationConfig> findByIndicatorCode(String indicatorCode);

    /**
     * 查询所有配置
     */
    List<IndicatorNormalizationConfig> findAll();
}