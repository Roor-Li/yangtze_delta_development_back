package com.opengms.yddback.strategy;

import com.opengms.yddback.entity.GlobalParams;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 指标计算策略接口
 * 每个指标实现一个计算策略
 */
public interface IndicatorCalculationStrategy {

    /**
     * 获取指标代码（如 "D1_I1"）
     */
    String getIndicatorCode();

    /**
     * 计算指标值
     *
     * @param basicData 基础统计数据（从basic_statistics_data表的data_content字段读取）
     * @param cityName 城市名称（可能需要用于某些特殊计算）
     * @param year 年份（可能需要多年数据进行计算）
     * @return 计算后的指标原始值
     */
    BigDecimal calculate(Map<String, Object> basicData, String cityName, Integer year);

    /**
     * 计算指标值（支持多年数据）
     *
     * @param multiYearData 多年基础统计数据
     *                      格式：Map<年份, Map<字段名, 值>>
     *                      例如：{2024: {人口: 1000, GDP: 5000}, 2023: {人口: 950, GDP: 4800}}
     * @param cityName 城市名称
     * @param targetYear 目标年份（要计算哪一年的指标）
     * @return 计算后的指标原始值
     */
    default BigDecimal calculateWithMultiYear(
            Map<Integer, Map<String, Object>> multiYearData,
            String cityName,
            Integer targetYear) {

        // 默认实现：只使用目标年份的数据，调用单年计算方法
        Map<String, Object> targetYearData = multiYearData.get(targetYear);
        if (targetYearData == null) {
            return BigDecimal.ZERO;
        }
        return calculate(targetYearData, cityName, targetYear);
    }

    /**
     * 使用多年数据和全局参数计算（用于需要全局统计信息的指标）
     *
     * @param multiYearData 多年基础统计数据
     * @param cityName 城市名称
     * @param targetYear 目标年份
     * @param globalParams 全局参数
     *                     例如：{"totalGdpGrowthRate": 5.2, "avgPopulation": 1000000}
     * @return 计算后的指标值
     */
    default BigDecimal calculateWithGlobalParams(
            Map<Integer, Map<String, Object>> multiYearData,
            String cityName,
            Integer targetYear,
            GlobalParams globalParams) {

        // 默认实现：忽略全局参数，调用普通多年计算
        return calculateWithMultiYear(multiYearData, cityName, targetYear);
    }

    /**
     * 该指标是否需要多年数据
     */
    default boolean requiresMultiYearData() {
        return false;
    }

    /**
     * 需要哪些年份的数据（相对于目标年份）
     * 例如：返回 [-1, 0] 表示需要去年和今年的数据
     */
    default int[] getRequiredYearOffsets() {
        return new int[]{0};  // 默认只需要当前年份
    }

    /**
     * 该指标是否需要全局参数（如总增长率等）
     */
    default boolean requiresGlobalParams() {
        return false;
    }

    /**
     * 需要哪些全局参数
     * 返回需要的全局参数key列表
     */
    default String[] getRequiredGlobalParams() {
        return new String[0];
    }

    /**
     * 获取该指标需要的基础数据字段列表
     * 用于验证数据完整性
     *
     * @return 基础数据字段名列表
     */
    default String[] getRequiredFields() {
        return new String[0];
    }
}