package com.opengms.yddback.strategy.indicator;

import com.opengms.yddback.strategy.IndicatorCalculationStrategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 指标计算策略抽象基类
 * 提供一些通用的辅助方法
 */
public abstract class AbstractIndicatorCalculationStrategy implements IndicatorCalculationStrategy {

    /**
     * 从数据中获取BigDecimal值
     */
    protected BigDecimal getValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        } else if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * 从多年数据中获取指定年份的值
     */
    protected BigDecimal getValueFromYear(
            Map<Integer, Map<String, Object>> multiYearData,
            Integer year,
            String key) {

        Map<String, Object> yearData = multiYearData.get(year);
        if (yearData == null) {
            return null;
        }
        return getValue(yearData, key);
    }

    /**
     * 安全除法（避免除以0）
     */
    protected BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator, int scale) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, scale, RoundingMode.HALF_UP);
    }

    /**
     * 百分比计算
     */
    protected BigDecimal percentage(BigDecimal part, BigDecimal total) {
        return safeDivide(part, total, 10).multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算增长率
     * 公式：(当前值 - 上期值) / 上期值 * 100%
     */
    protected BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return current.subtract(previous)
                .divide(previous, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}