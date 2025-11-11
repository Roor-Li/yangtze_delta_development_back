package com.opengms.yddback.strategy;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 维度得分计算策略接口
 */
public interface DimensionScoreCalculationStrategy {

    /**
     * 获取维度代码（如 "D1"）
     */
    String getDimensionCode();

    /**
     * 计算维度得分
     *
     * @param indicatorScores 该维度下所有指标的归一化得分
     *                        key: 指标代码（如 "D1_I1"）
     *                        value: 归一化后的指标值（0-1）
     * @return 维度得分（0-20）
     */
    BigDecimal calculate(Map<String, BigDecimal> indicatorScores);
}