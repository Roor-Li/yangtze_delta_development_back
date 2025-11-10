package com.opengms.yddback.strategy.normalization;

import com.opengms.yddback.strategy.NormalizationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 标准化策略
 *
 *
 * 适用于：需要标准正态分布的场景
 */
@Component("bidirectionalNormalization")
public class BidirectionalNormalizationStrategy implements NormalizationStrategy {

    @Override
    public String getStrategyName() {
        return "bidirectional";
    }

    @Override
    public BigDecimal normalize(BigDecimal value, BigDecimal normalizedParam,
                                BigDecimal targetMin, BigDecimal targetMax) {

        BigDecimal normalized;

        if (value.compareTo(targetMin) < 0) {
            normalized = targetMin;
        } else if (value.compareTo(targetMax) <= 0) {
            normalized = value.divide(normalizedParam, 10, RoundingMode.HALF_UP);
        } else {
            BigDecimal two = new BigDecimal(2);
            BigDecimal tmp = two.subtract(value);
            normalized = tmp.divide(normalizedParam, 10, RoundingMode.HALF_UP);
        }

        return normalized.setScale(10, RoundingMode.HALF_UP);
    }
}