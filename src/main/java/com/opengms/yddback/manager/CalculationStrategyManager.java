package com.opengms.yddback.manager;

import com.opengms.yddback.strategy.DimensionScoreCalculationStrategy;
import com.opengms.yddback.strategy.IndicatorCalculationStrategy;
import com.opengms.yddback.strategy.NormalizationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计算策略管理器
 * 负责管理所有的计算策略
 */
@Slf4j
@Component
public class CalculationStrategyManager {

    private final Map<String, IndicatorCalculationStrategy> indicatorStrategies;
    private final Map<String, NormalizationStrategy> normalizationStrategies;
    private final Map<String, DimensionScoreCalculationStrategy> dimensionStrategies;
    private final DimensionScoreCalculationStrategy defaultDimensionStrategy;

    /**
     * 构造函数：Spring自动注入所有策略实现
     */
    public CalculationStrategyManager(
            List<IndicatorCalculationStrategy> indicatorStrategyList,
            List<NormalizationStrategy> normalizationStrategyList,
            List<DimensionScoreCalculationStrategy> dimensionStrategyList) {

        // 初始化指标计算策略映射
        this.indicatorStrategies = new HashMap<>();
        for (IndicatorCalculationStrategy strategy : indicatorStrategyList) {
            String code = strategy.getIndicatorCode();
            if (!code.equals("DX_IX")) { // 跳过模板
                indicatorStrategies.put(code, strategy);
                log.info("注册指标计算策略: {}", code);
            }
        }

        // 初始化归一化策略映射
        this.normalizationStrategies = new HashMap<>();
        for (NormalizationStrategy strategy : normalizationStrategyList) {
            normalizationStrategies.put(strategy.getStrategyName(), strategy);
            log.info("注册归一化策略: {}", strategy.getStrategyName());
        }

        // 初始化维度得分计算策略映射
        this.dimensionStrategies = new HashMap<>();
        DimensionScoreCalculationStrategy tempDefaultStrategy = null;
        for (DimensionScoreCalculationStrategy strategy : dimensionStrategyList) {
            String code = strategy.getDimensionCode();
            if (code.isEmpty()) {
                tempDefaultStrategy = strategy;
                log.info("注册默认维度得分计算策略");
            } else {
                dimensionStrategies.put(code, strategy);
                log.info("注册维度得分计算策略: {}", code);
            }
        }
        this.defaultDimensionStrategy = tempDefaultStrategy;

        log.info("策略管理器初始化完成: 指标策略{}个, 归一化策略{}个, 维度策略{}个",
                indicatorStrategies.size(), normalizationStrategies.size(), dimensionStrategies.size());
    }

    /**
     * 获取指标计算策略
     */
    public IndicatorCalculationStrategy getIndicatorStrategy(String indicatorCode) {
        return indicatorStrategies.get(indicatorCode);
    }

    /**
     * 获取归一化策略
     */
    public NormalizationStrategy getNormalizationStrategy(String strategyName) {
        return normalizationStrategies.getOrDefault(strategyName, normalizationStrategies.get("positive"));
    }

    /**
     * 获取维度得分计算策略
     */
    public DimensionScoreCalculationStrategy getDimensionStrategy(String dimensionCode) {
        DimensionScoreCalculationStrategy strategy = dimensionStrategies.get(dimensionCode);
        return strategy != null ? strategy : defaultDimensionStrategy;
    }

    /**
     * 检查是否所有指标都有计算策略
     */
    public boolean hasStrategyForIndicator(String indicatorCode) {
        return indicatorStrategies.containsKey(indicatorCode);
    }

    /**
     * 获取所有已注册的指标代码
     */
    public List<String> getAllIndicatorCodes() {
        return List.copyOf(indicatorStrategies.keySet());
    }
}
