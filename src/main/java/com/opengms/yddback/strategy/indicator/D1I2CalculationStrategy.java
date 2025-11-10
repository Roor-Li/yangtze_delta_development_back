package com.opengms.yddback.strategy.indicator;


import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * D1_I2: R&D经费占GDP比重
 * 计算公式: R&D经费 / GDP * 100%
 */
@Component
public class D1I2CalculationStrategy extends AbstractIndicatorCalculationStrategy {

    @Override
    public String getIndicatorCode() {
        return "D1_I2";
    }

    @Override
    public BigDecimal calculate(Map<String, Object> basicData, String cityName, Integer year) {

        BigDecimal rdExpenditure = getValue(basicData, "rd_expenditure");
        BigDecimal gdp = getValue(basicData, "gdp");

        if (rdExpenditure == null || gdp == null) {
            return BigDecimal.ZERO;
        }

        return percentage(rdExpenditure, gdp);
    }

    @Override
    public String[] getRequiredFields() {
        return new String[]{"rd_expenditure", "gdp"};
    }
}