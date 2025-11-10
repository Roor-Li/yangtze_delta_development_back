package com.opengms.yddback.strategy.normalization;

import com.opengms.yddback.strategy.NormalizationStrategy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 逆向归一化策略（负向指标）
 * 公式:
 *
 * 适用于：数值越小越好的指标（如污染、能耗等）
 */
@Component("negativeNormalization")
public class NegativeNormalizationStrategy implements NormalizationStrategy {

    @Override
    public String getStrategyName() {
        return "negative";
    }

    @Override
    public BigDecimal normalize(BigDecimal value, BigDecimal normalizedParam,
                                BigDecimal targetMin, BigDecimal targetMax) {

        int compareWithP = value.compareTo(normalizedParam);
        int compareWith3P = value.compareTo(normalizedParam.multiply(new BigDecimal(3)));

        BigDecimal normalized;

        if (compareWithP <= 0) {
            normalized = targetMax;
        } else if (compareWith3P >= 0) {
            normalized = targetMin;
        } else {
            BigDecimal onePointFive = new BigDecimal("1.5");
            BigDecimal tmp = value.divide(
                    normalizedParam.multiply(new BigDecimal(2)),
                    10,
                    RoundingMode.HALF_UP
            );
            normalized = onePointFive.subtract(tmp);
        }

        return normalized.setScale(10, RoundingMode.HALF_UP);
    }
}