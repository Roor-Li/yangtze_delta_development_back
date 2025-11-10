package com.opengms.yddback.strategy.indicator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * TODO: 指标计算策略模板
 *
 */
@Component
public class D2I5CalculationStrategy extends AbstractIndicatorCalculationStrategy {

    @Override
    public String getIndicatorCode() {
        // TODO: 返回指标代码，如 "D1_I3"
        return "D2_I5";
    }

    /**
     * 标记该指标需要多年数据
     */
    @Override
    public boolean requiresMultiYearData() {
        return true;
    }

    /**
     * 需要当前年份(0)和上一年(-1)的数据
     */
    @Override
    public int[] getRequiredYearOffsets() {
        return new int[]{0, -1};  // 当前年和去年
    }

    /**
     * 使用多年数据计算
     */
    @Override
    public BigDecimal calculateWithMultiYear(
            Map<Integer, Map<String, Object>> multiYearData,
            String cityName,
            Integer targetYear) {

        // 1. 获取当前年份的人口
        BigDecimal currentPopulation = getValueFromYear(multiYearData, targetYear, "year_end_population");

        // 2. 获取上一年份的人口
        BigDecimal lastYearPopulation = getValueFromYear(multiYearData, targetYear - 1, "permanent_population");

        // 3. 数据验证
        if (currentPopulation == null || lastYearPopulation == null) {
            return BigDecimal.ZERO;
        }

        // 4. 计算增长率
        return calculateGrowthRate(currentPopulation, lastYearPopulation);
    }

    @Override
    public BigDecimal calculate(Map<String, Object> basicData, String cityName, Integer year) {
        // TODO: 实现计算逻辑
        // 步骤1: 从basicData中获取需要的字段
        // BigDecimal field1 = getValue(basicData, "字段名1");
        // BigDecimal field2 = getValue(basicData, "字段名2");

        // 步骤2: 数据验证
        // if (field1 == null || field2 == null) {
        //     return BigDecimal.ZERO;
        // }

        // 步骤3: 计算
        // BigDecimal result = ... 你的计算公式 ...

        // 步骤4: 返回结果
        // return result;

        return BigDecimal.ZERO;
    }

    @Override
    public String[] getRequiredFields() {
        // TODO: 列出该指标需要的基础数据字段
        return new String[]{
                "year_end_population",
                "permanent_population"
                // "字段名1",
                // "字段名2"
        };
    }
}