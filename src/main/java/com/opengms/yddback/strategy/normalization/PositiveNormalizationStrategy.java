package com.opengms.yddback.strategy.normalization;

import com.opengms.yddback.strategy.NormalizationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 归一化策略（正向指标）
 * 公式:
 *
 * 适用于：数值越大越好的指标
 */
@Component("positiveNormalization")
public class PositiveNormalizationStrategy implements NormalizationStrategy {

    @Override
    public String getStrategyName() {
        return "positive";
    }

    @Override
    public BigDecimal normalize(BigDecimal value, BigDecimal normalizedParam,
                                BigDecimal targetMin, BigDecimal targetMax) {

        if (value == null || normalizedParam == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal res = value.divide(normalizedParam, 10, RoundingMode.HALF_UP);
        BigDecimal normalized;

        if (res.compareTo(targetMin) <= 0) {
            normalized = BigDecimal.ZERO;
        } else if (res.compareTo(targetMax) >= 0) {
            normalized = BigDecimal.ONE;
        } else {
            normalized = res;
        }

        return normalized.setScale(10, RoundingMode.HALF_UP);
    }
}