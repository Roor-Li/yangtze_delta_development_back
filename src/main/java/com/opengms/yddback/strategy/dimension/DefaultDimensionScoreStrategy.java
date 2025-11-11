package com.opengms.yddback.strategy.dimension;

import com.opengms.yddback.strategy.DimensionScoreCalculationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 默认维度得分计算策略
 * 加权平均法：每个指标权重相同，均为 10/3
 */
@Component
public class DefaultDimensionScoreStrategy implements DimensionScoreCalculationStrategy {

    // 每个指标的权重 = 10/3 (维度满分20，有6个指标)
    private static final BigDecimal INDICATOR_WEIGHT = new BigDecimal("10").divide(
            new BigDecimal("3"), 10, RoundingMode.HALF_UP);

    @Override
    public String getDimensionCode() {
        // 空字符串表示这是默认策略，适用于所有维度
        return "";
    }

    @Override
    public BigDecimal calculate(Map<String, BigDecimal> indicatorScores) {
        if (indicatorScores == null || indicatorScores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算加权得分
        // 公式: Σ(归一化指标值 * 权重)
        BigDecimal totalScore = indicatorScores.values().stream()
                .map(score -> score.multiply(INDICATOR_WEIGHT))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 保留10位小数
        return totalScore.setScale(10, RoundingMode.HALF_UP);
    }
}