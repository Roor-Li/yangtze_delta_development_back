package com.opengms.yddback.strategy.indicator;

import com.opengms.yddback.entity.GlobalParams;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * TODO: 指标计算策略模板
 *
 */
@Component
public class D2I6CalculationStrategy extends AbstractIndicatorCalculationStrategy {

    @Override
    public String getIndicatorCode() {
        // TODO: 返回指标代码，如 "D1_I3"
        return "D2_I6";
    }

    /**
     * 需要多年数据
     */
    @Override
    public boolean requiresMultiYearData() {
        return true;
    }

    /**
     * 需要全局参数
     */
    @Override
    public boolean requiresGlobalParams() {
        return true;
    }

    /**
     * 需要的全局参数：区域总GDP增长率
     */
    @Override
    public String[] getRequiredGlobalParams() {
        return new String[]{"totalGdpGrowthRate"};
    }

    @Override
    public int[] getRequiredYearOffsets() {
        return new int[]{0, -1};  // 当前年和去年
    }

    /**
     * 使用全局参数计算
     */
    @Override
    public BigDecimal calculateWithGlobalParams(
            Map<Integer, Map<String, Object>> multiYearData,
            String cityName,
            Integer targetYear,
            GlobalParams globalParams) {

        // 1. 获取当前年份和上一年份的GDP
        BigDecimal currentGdp = getValueFromYear(multiYearData, targetYear, "gdp");
        BigDecimal lastYearGdp = getValueFromYear(multiYearData, targetYear - 1, "gdp");

        if (currentGdp == null || lastYearGdp == null) {
            return BigDecimal.ZERO;
        }

        // 2. 计算城市GDP增长率
        BigDecimal cityGrowthRate = calculateGrowthRate(currentGdp, lastYearGdp);

        // 3. 获取区域总GDP增长率
        BigDecimal totalGrowthRate = globalParams.getTotalGdpGrowthRate();

        if (totalGrowthRate == null || totalGrowthRate.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        // 4. 计算协同指数：城市增长率 / 总增长率
        return safeDivide(cityGrowthRate, totalGrowthRate, 10);
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
                "gdp"
                // "字段名1",
                // "字段名2"
        };
    }
}