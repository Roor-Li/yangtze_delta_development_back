package com.opengms.yddback.strategy.indicator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * TODO: 指标计算策略模板
 *
 */
@Component
public class D5I6CalculationStrategy extends AbstractIndicatorCalculationStrategy {

    @Override
    public String getIndicatorCode() {
        // TODO: 返回指标代码，如 "D1_I3"
        return "D5_I6";
    }

    @Override
    public BigDecimal calculate(Map<String, Object> basicData, String cityName, Integer year) {
        // TODO: 实现计算逻辑

        // 步骤1: 从basicData中获取需要的字段
         BigDecimal cityArea = getValue(basicData, "city_area");
         BigDecimal highwayLength = getValue(basicData, "highway_length");

        // 步骤2: 数据验证
         if (cityArea == null || highwayLength == null) {
             return BigDecimal.ZERO;
         }

        // 步骤3: 计算
         BigDecimal result = safeDivide(highwayLength, cityArea, 10);

        // 步骤4: 返回结果
         return result;

//        return BigDecimal.ZERO;
    }

    @Override
    public String[] getRequiredFields() {
        // TODO: 列出该指标需要的基础数据字段
        return new String[]{
                "city_area",
                "highway_length"
                // "字段名1",
                // "字段名2"
        };
    }
}