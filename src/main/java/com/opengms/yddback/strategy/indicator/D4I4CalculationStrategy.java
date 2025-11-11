package com.opengms.yddback.strategy.indicator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * TODO: 指标计算策略模板
 *
 */
@Component
public class D4I4CalculationStrategy extends AbstractIndicatorCalculationStrategy {

    @Override
    public String getIndicatorCode() {
        // TODO: 返回指标代码，如 "D1_I3"
        return "D4_I4";
    }

    @Override
    public BigDecimal calculate(Map<String, Object> basicData, String cityName, Integer year) {
        // TODO: 实现计算逻辑

        // 步骤1: 从basicData中获取需要的字段
         BigDecimal domesticTourism = getValue(basicData, "domestic_tourism");
         BigDecimal internationalTourism = getValue(basicData, "international_tourism");

        // 步骤2: 数据验证
         if (domesticTourism == null || internationalTourism == null) {
             return BigDecimal.ZERO;
         }

        // 步骤3: 计算
         BigDecimal result = percentage(internationalTourism, domesticTourism.add(internationalTourism));

        // 步骤4: 返回结果
         return result;

//        return BigDecimal.ZERO;
    }

    @Override
    public String[] getRequiredFields() {
        // TODO: 列出该指标需要的基础数据字段
        return new String[]{
                "domestic_tourism",
                "international_tourism"
                // "字段名1",
                // "字段名2"
        };
    }
}