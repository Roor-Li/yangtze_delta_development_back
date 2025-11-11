package com.opengms.yddback.strategy.indicator;

//import lombok.extern.slf4j.Slf4j; // 暂时使用log
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * D1_I1: 科技拨款占财政拨款的比重
 * 计算公式: 科学技术支出 / 一般公共预算支出 * 100%
 */
//@Slf4j // 暂时使用log
@Component
public class D1I1CalculationStrategy extends AbstractIndicatorCalculationStrategy {

    @Override
    public String getIndicatorCode() {
        return "D1_I1";
    }

    @Override
    public BigDecimal calculate(Map<String, Object> basicData, String cityName, Integer year) {
        // TODO: 填充计算逻辑

//        log.info(basicData.toString());

        // 1. 获取基础数据
        BigDecimal scienceTechExpenditure = getValue(basicData, "tech_funding");
        BigDecimal publicBudgetExpenditure = getValue(basicData, "fiscal_appropriation");

        // 2. 数据验证
        if (scienceTechExpenditure == null || publicBudgetExpenditure == null) {
            return BigDecimal.ZERO;
        }

        // 3. 计算指标值
        BigDecimal result = percentage(scienceTechExpenditure, publicBudgetExpenditure);

        return result;
    }

    @Override
    public String[] getRequiredFields() {
        return new String[]{"tech_funding", "fiscal_appropriation"};
    }
}