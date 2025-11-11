package com.opengms.yddback.strategy;


import java.math.BigDecimal;
import java.util.List;

/**
 * 归一化策略接口
 * 每个指标可以有不同的归一化方法
 */
public interface NormalizationStrategy {

    /**
     * 获取策略名称
     */
    String getStrategyName();

    /**
     * 归一化单个值
     *
     * @param value 原始值
     * @param normalizedParam 所有城市的该指标目标值（用于归一化计算）
     * @param targetMin 目标最小值（通常为0）
     * @param targetMax 目标最大值（通常为1）
     * @return 归一化后的值（0-1之间）
     */
    BigDecimal normalize(BigDecimal value, BigDecimal normalizedParam,
                         BigDecimal targetMin, BigDecimal targetMax);
}