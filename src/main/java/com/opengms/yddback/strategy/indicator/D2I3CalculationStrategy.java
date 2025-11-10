package com.opengms.yddback.strategy.indicator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * TODO: 指标计算策略模板
 *
 */
@Component
public class D2I3CalculationStrategy extends AbstractIndicatorCalculationStrategy {

    @Override
    public String getIndicatorCode() {
        // TODO: 返回指标代码，如 "D1_I3"
        return "D2_I3";
    }

    @Override
    public BigDecimal calculate(Map<String, Object> basicData, String cityName, Integer year) {
        // TODO: 实现计算逻辑

        // 步骤1: 从basicData中获取需要的字段
         BigDecimal tertiaryIndustry = getValue(basicData, "tertiary_industry");
         BigDecimal gdp = getValue(basicData, "gdp");

        // 步骤2: 数据验证
         if (tertiaryIndustry == null || gdp == null) {
             return BigDecimal.ZERO;
         }

        // 步骤3: 计算
         BigDecimal result = percentage(tertiaryIndustry, gdp);

        // 步骤4: 返回结果
         return result;

//        return BigDecimal.ZERO;
    }

    @Override
    public String[] getRequiredFields() {
        // TODO: 列出该指标需要的基础数据字段
        return new String[]{
                "tertiary_industry",
                "gdp"
                // "字段名1",
                // "字段名2"
        };
    }
}