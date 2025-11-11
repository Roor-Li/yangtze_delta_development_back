package com.opengms.yddback.service.impl;

import com.opengms.yddback.entity.*;
import com.opengms.yddback.manager.CalculationStrategyManager;
import com.opengms.yddback.repository.*;
import com.opengms.yddback.service.NormalizationConfigCacheService;
import com.opengms.yddback.service.ScoreCalculationService;
import com.opengms.yddback.strategy.DimensionScoreCalculationStrategy;
import com.opengms.yddback.strategy.IndicatorCalculationStrategy;
import com.opengms.yddback.strategy.NormalizationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 指标和维度得分计算服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreCalculationServiceImpl implements ScoreCalculationService {

    private final BasicStatisticsDataRepository statisticsRepository;
    private final CityInfoRepository cityInfoRepository;
    private final DimensionDefinitionRepository dimensionRepository;
    private final IndicatorDefinitionRepository indicatorRepository;
    private final IndicatorCalculationResultRepository indicatorResultRepository;
    private final DimensionScoreRepository dimensionScoreRepository;
    private final CalculationStrategyManager strategyManager;
    private final NormalizationConfigCacheService normalizationConfigCacheService;
    private final GlobalParamsRepository globalParamsRepository;

    /**
     * 计算并保存某年的所有指标得分
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int calculateAndSaveIndicatorScores(Integer year) {
        log.info("开始计算{}年的指标得分", year);

        // 1. 查询该年份的基础统计数据
        List<BasicStatisticsData> statisticsDataList = statisticsRepository.findByYear(year);

        if (statisticsDataList.isEmpty()) {
            log.warn("未找到{}年的基础统计数据，无法计算指标得分", year);
            return 0;
        }

        log.info("找到{}个城市的基础数据", statisticsDataList.size());

        // 2. 查询所有指标定义
        List<IndicatorDefinition> indicators = indicatorRepository.findAllOrderedByDimensionAndDisplay();

        if (indicators.isEmpty()) {
            log.warn("未找到指标定义");
            return 0;
        }

        log.info("共有{}个指标需要计算", indicators.size());

        // 3. 按维度分组指标
        Map<Long, List<IndicatorDefinition>> dimensionIndicatorsMap = indicators.stream()
                .collect(Collectors.groupingBy(IndicatorDefinition::getDimensionId));

        // 4. 创建城市ID到城市信息的映射
        List<Long> cityIds = statisticsDataList.stream()
                .map(BasicStatisticsData::getCityId)
                .collect(Collectors.toList());

        List<CityInfo> cities = cityInfoRepository.findAllById(cityIds);
        Map<Long, CityInfo> cityMap = cities.stream()
                .collect(Collectors.toMap(CityInfo::getId, city -> city));

        // 5. 创建城市ID到基础数据的映射
        Map<Long, BasicStatisticsData> cityDataMap = statisticsDataList.stream()
                .collect(Collectors.toMap(BasicStatisticsData::getCityId, data -> data));

        // 6. 预查询多年数据（用于需要历史数据的指标）
        Map<Long, Map<Integer, Map<String, Object>>> cityMultiYearDataMap =
                loadMultiYearData(cityIds, year);

        // ========== 计算全局参数 ==========
        Optional<GlobalParams> globalParamsOpt = globalParamsRepository.findByYear(year);
        GlobalParams globalParams;
        if (globalParamsOpt.isPresent()) {
            log.info("{}年全局参数已存在，可以直接使用", year);
            globalParams = globalParamsOpt.get();
        } else {
//            log.info("未查询到全局参数，开始计算");
            globalParams = calculateGlobalParams(
                    statisticsDataList,
                    cityMultiYearDataMap,
                    year
            );
            log.info("全局参数计算完成: {}", globalParams);
            globalParamsRepository.save(globalParams);
        }

        // =====================================

        int successCount = 0;
        // 7. 遍历每个维度，计算指标
        for (Map.Entry<Long, List<IndicatorDefinition>> entry : dimensionIndicatorsMap.entrySet()) {
            Long dimensionId = entry.getKey();
            List<IndicatorDefinition> dimensionIndicators = entry.getValue();

            for (IndicatorDefinition indicator : dimensionIndicators) {
                String indicatorCode = indicator.getIndicatorCode();
                Long indicatorId = indicator.getId();

                IndicatorCalculationStrategy calculationStrategy =
                        strategyManager.getIndicatorStrategy(indicatorCode);

                if (calculationStrategy == null) {
                    log.warn("指标{}未配置计算策略", indicatorCode);
                    continue;
                }

                // 8. 计算所有城市的该指标值
                Map<Long, BigDecimal> cityIndicatorValues = new HashMap<>();

                for (BasicStatisticsData data : statisticsDataList) {
                    Long cityId = data.getCityId();
                    CityInfo city = cityMap.get(cityId);

                    if (city == null) {
                        continue;
                    }

                    try {
                        BigDecimal value;

                        // 判断计算方式
                        if (calculationStrategy.requiresGlobalParams()) {
                            // ========== 使用全局参数计算 ==========
                            Map<Integer, Map<String, Object>> multiYearData =
                                    cityMultiYearDataMap.get(cityId);

                            if (multiYearData == null || multiYearData.isEmpty()) {
                                log.warn("城市{}缺少多年数据", city.getCityName());
                                continue;
                            }

                            value = calculationStrategy.calculateWithGlobalParams(
                                    multiYearData,
                                    city.getCityName(),
                                    year,
                                    globalParams  // 传入全局参数
                            );
                            // ======================================

                        } else if (calculationStrategy.requiresMultiYearData()) {
                            // 使用多年数据计算
                            Map<Integer, Map<String, Object>> multiYearData =
                                    cityMultiYearDataMap.get(cityId);

                            if (multiYearData == null || multiYearData.isEmpty()) {
                                log.warn("城市{}缺少多年数据", city.getCityName());
                                continue;
                            }

                            value = calculationStrategy.calculateWithMultiYear(
                                    multiYearData,
                                    city.getCityName(),
                                    year
                            );
                        } else {
                            // 使用单年数据计算
                            value = calculationStrategy.calculate(
                                    data.getDataContent(),
                                    city.getCityName(),
                                    year
                            );
                        }

                        cityIndicatorValues.put(cityId, value);

                    } catch (Exception e) {
                        log.error("计算指标失败: 城市={}, 指标={}", city.getCityName(), indicatorCode, e);
                    }
                }

                // 9. 归一化
                Map<Long, BigDecimal> normalizedValues = normalizeIndicatorValues(
                        cityIndicatorValues,
                        indicatorCode
                );

                // 10. 保存
                for (Map.Entry<Long, BigDecimal> valueEntry : normalizedValues.entrySet()) {
                    Long cityId = valueEntry.getKey();
                    BigDecimal normalizedValue = valueEntry.getValue();

                    boolean exists = indicatorResultRepository.existsByCityIdAndYearAndIndicatorId(
                            cityId, year, indicatorId);

                    if (exists) {
                        continue;
                    }

                    IndicatorCalculationResult result = IndicatorCalculationResult.builder()
                            .cityId(cityId)
                            .year(year)
                            .dimensionId(dimensionId)
                            .indicatorId(indicatorId)
                            .indicatorValue(normalizedValue)
                            .build();

                    indicatorResultRepository.save(result);

//                    更新数据库值
                    // 查找是否已存在
//                    Optional<IndicatorCalculationResult> existingOpt =
//                            indicatorResultRepository.findByCityIdAndYearAndIndicatorId(
//                                    cityId, year, indicatorId);
//
//                    if (existingOpt.isPresent()) {
//                        // 存在则更新
//                        IndicatorCalculationResult existing = existingOpt.get();
//                        existing.setIndicatorValue(normalizedValue);
//                        existing.setUpdatedAt(LocalDateTime.now());
//
//                        indicatorResultRepository.save(existing);
//
//                        log.debug("指标结果已更新（覆盖）: 城市ID={}, 年份={}, 指标={}, 新值={}",
//                                cityId, year, indicatorCode, normalizedValue);
//                    } else {
//                        // 不存在则新增
//                        IndicatorCalculationResult result = IndicatorCalculationResult.builder()
//                                .cityId(cityId)
//                                .year(year)
//                                .dimensionId(dimensionId)
//                                .indicatorId(indicatorId)
//                                .indicatorValue(normalizedValue)
//                                .build();
//
//                        indicatorResultRepository.save(result);
//
//                        log.debug("指标结果已新增: 城市ID={}, 年份={}, 指标={}",
//                                cityId, year, indicatorCode);
//                    }

                    successCount++;
                }

                log.info("指标{}计算完成", indicatorCode);
            }
        }

        log.info("{}年指标得分计算完成，共保存{}条记录", year, successCount);

        return successCount;
    }

    /**
     * 计算全局参数
     *
     * @param statisticsDataList 所有城市的当前年份数据
     * @param cityMultiYearDataMap 所有城市的多年数据
     * @param targetYear 目标年份
     * @return 全局参数Map
     */
    private GlobalParams calculateGlobalParams(
            List<BasicStatisticsData> statisticsDataList,
            Map<Long, Map<Integer, Map<String, Object>>> cityMultiYearDataMap,
            Integer targetYear) {

        log.info("开始计算全局参数，目标年份: {}", targetYear);

//        Map<String, BigDecimal> globalParams = new HashMap<>();
        GlobalParams globalParams = new GlobalParams();

        // 1. 计算区域总GDP增长率
        BigDecimal totalGdpGrowthRate = calculateTotalGdpGrowthRate(
                statisticsDataList,
                cityMultiYearDataMap,
                targetYear
        );

        globalParams.setYear(targetYear);
        globalParams.setTotalGdpGrowthRate(totalGdpGrowthRate);

//        globalParams.put("totalGdpGrowthRate", totalGdpGrowthRate);

        // 2. 可以添加其他全局参数
        // 例如：区域平均人口、平均收入等

        return globalParams;
    }

    /**
     * 计算区域总GDP增长率
     *
     * @param statisticsDataList 所有城市的当前年份数据
     * @param cityMultiYearDataMap 所有城市的多年数据
     * @param targetYear 目标年份
     * @return 区域总GDP增长率（百分比）
     */
    public BigDecimal calculateTotalGdpGrowthRate(
            List<BasicStatisticsData> statisticsDataList,
            Map<Long, Map<Integer, Map<String, Object>>> cityMultiYearDataMap,
            Integer targetYear) {

        // 1. 计算当前年份所有城市的总GDP
        BigDecimal currentYearTotalGdp = BigDecimal.ZERO;

        for (BasicStatisticsData data : statisticsDataList) {
            Object gdpObj = data.getDataContent().get("gdp");
            if (gdpObj != null) {
                BigDecimal gdp = convertToBigDecimal(gdpObj);
                if (gdp != null) {
                    currentYearTotalGdp = currentYearTotalGdp.add(gdp);
                }
            }
        }

        log.info("{}年区域总GDP: {}", targetYear, currentYearTotalGdp);

        // 2. 计算上一年份所有城市的总GDP
        BigDecimal lastYearTotalGdp = BigDecimal.ZERO;

        for (Map.Entry<Long, Map<Integer, Map<String, Object>>> entry : cityMultiYearDataMap.entrySet()) {
            Map<Integer, Map<String, Object>> multiYearData = entry.getValue();
            Map<String, Object> lastYearData = multiYearData.get(targetYear - 1);

            if (lastYearData != null) {
                Object gdpObj = lastYearData.get("gdp");
                if (gdpObj != null) {
                    BigDecimal gdp = convertToBigDecimal(gdpObj);
                    if (gdp != null) {
                        lastYearTotalGdp = lastYearTotalGdp.add(gdp);
                    }
                }
            }
        }

        log.info("{}年区域总GDP: {}", targetYear - 1, lastYearTotalGdp);

        // 3. 计算增长率：(今年总GDP - 去年总GDP) / 去年总GDP * 100%
        if (lastYearTotalGdp.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("上一年总GDP为0，无法计算增长率");
            return BigDecimal.ZERO;
        }

        BigDecimal growthRate = currentYearTotalGdp.subtract(lastYearTotalGdp)
                .divide(lastYearTotalGdp, 10, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        log.info("区域总GDP增长率: {}%", growthRate);

        return growthRate;
    }

    /**
     * 将对象转换为BigDecimal
     */
    private BigDecimal convertToBigDecimal(Object value) {
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
     * 预加载多年数据
     *
     * @param cityIds 城市ID列表
     * @param targetYear 目标年份
     * @return Map<城市ID, Map<年份, Map<字段名, 值>>>
     */
    private Map<Long, Map<Integer, Map<String, Object>>> loadMultiYearData(
            List<Long> cityIds,
            Integer targetYear) {

        log.info("预加载多年数据，目标年份: {}", targetYear);

        // 确定需要加载的年份范围
        // 假设最多需要前3年的数据（可以根据实际情况调整）
        Set<Integer> yearsToLoad = new HashSet<>();
        yearsToLoad.add(targetYear);
        yearsToLoad.add(targetYear - 1);
        yearsToLoad.add(targetYear - 2);
        yearsToLoad.add(targetYear - 3);

        Map<Long, Map<Integer, Map<String, Object>>> result = new HashMap<>();

        for (Integer year : yearsToLoad) {
            List<BasicStatisticsData> yearDataList = statisticsRepository.findByYear(year);

            for (BasicStatisticsData data : yearDataList) {
                Long cityId = data.getCityId();

                result.computeIfAbsent(cityId, k -> new HashMap<>())
                        .put(year, data.getDataContent());
            }
        }

        log.info("多年数据加载完成，共{}个城市，{}个年份", result.size(), yearsToLoad.size());

        return result;
    }

    /**
     * 归一化指标值
     *
     * @param cityIndicatorValues 城市ID -> 原始指标值
     * @param indicatorCode       指标代码
     * @return 城市ID -> 归一化后的值（0-1）
     */
    private Map<Long, BigDecimal> normalizeIndicatorValues(
            Map<Long, BigDecimal> cityIndicatorValues,
            String indicatorCode) {

        if (indicatorCode.equals("D3_I5")) {
            log.info("问题");
        }

        // 1. 从配置缓存获取该指标的归一化配置
        Optional<IndicatorNormalizationConfig> configOpt = normalizationConfigCacheService.getConfig(indicatorCode);

        if (configOpt.isEmpty()) {
            log.warn("指标{}未配置归一化参数，跳过归一化", indicatorCode);
            // 如果没有配置，返回原始值（或者抛出异常）
            return cityIndicatorValues;
        }

        IndicatorNormalizationConfig config = configOpt.get();

        // 2. 获取归一化策略
        String normalizationMethod = config.getNormalizationMethod();
        NormalizationStrategy strategy = strategyManager.getNormalizationStrategy(normalizationMethod);

        if (strategy == null) {
            log.warn("归一化策略{}不存在，跳过归一化", normalizationMethod);
            return cityIndicatorValues;
        }

        // 3. 获取归一化参数
        BigDecimal normalizationParam = config.getNormalizationParam();
        BigDecimal targetMin = config.getTargetMin();
        BigDecimal targetMax = config.getTargetMax();

        log.debug("指标{}使用归一化方法: {}, 参数: {}", indicatorCode, normalizationMethod, normalizationParam);

        // 4. 对每个城市的值进行归一化
        Map<Long, BigDecimal> normalizedValues = new HashMap<>();

        for (Map.Entry<Long, BigDecimal> entry : cityIndicatorValues.entrySet()) {
            Long cityId = entry.getKey();
            BigDecimal value = entry.getValue();

            try {
                // 调用归一化策略（注意这里传入的是单个参数，而不是列表）
                BigDecimal normalizedValue = strategy.normalize(value, normalizationParam, targetMin, targetMax);
                normalizedValues.put(cityId, normalizedValue);
            } catch (Exception e) {
                log.error("归一化失败: 城市ID={}, 指标={}, 值={}", cityId, indicatorCode, value, e);
                // 归一化失败时，可以使用原始值或0
                normalizedValues.put(cityId, BigDecimal.ZERO);
            }
        }

        return normalizedValues;
    }

    /**
     * 计算并保存某年的所有维度得分
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int calculateAndSaveDimensionScores(Integer year) {
        log.info("开始计算{}年的维度得分", year);

        // 1. 查询该年份所有指标计算结果
        List<IndicatorCalculationResult> indicatorResults = indicatorResultRepository.findByYear(year);

        if (indicatorResults.isEmpty()) {
            log.warn("未找到{}年的指标计算结果，无法计算维度得分", year);
            return 0;
        }

        log.info("找到{}条指标计算结果", indicatorResults.size());

        // 2. 查询所有维度定义
        List<DimensionDefinition> dimensions = dimensionRepository.findAllByOrderByDisplayOrder();

        // 3. 按城市ID和维度ID组织指标结果
        // Map<城市ID, Map<维度ID, Map<指标代码, 指标值>>>
        Map<Long, Map<Long, Map<String, BigDecimal>>> cityDimensionIndicatorMap = new HashMap<>();

        // 创建指标ID到指标代码的映射
        List<IndicatorDefinition> allIndicators = indicatorRepository.findAll();
        Map<Long, String> indicatorIdToCodeMap = allIndicators.stream()
                .collect(Collectors.toMap(IndicatorDefinition::getId, IndicatorDefinition::getIndicatorCode));

        for (IndicatorCalculationResult result : indicatorResults) {
            Long cityId = result.getCityId();
            Long dimensionId = result.getDimensionId();
            Long indicatorId = result.getIndicatorId();
            BigDecimal value = result.getIndicatorValue();

            String indicatorCode = indicatorIdToCodeMap.get(indicatorId);
            if (indicatorCode == null) {
                continue;
            }

            cityDimensionIndicatorMap
                    .computeIfAbsent(cityId, k -> new HashMap<>())
                    .computeIfAbsent(dimensionId, k -> new HashMap<>())
                    .put(indicatorCode, value);
        }

        int successCount = 0;

        // 4. 遍历每个城市和维度，计算维度得分
        for (Map.Entry<Long, Map<Long, Map<String, BigDecimal>>> cityEntry : cityDimensionIndicatorMap.entrySet()) {
            Long cityId = cityEntry.getKey();
            Map<Long, Map<String, BigDecimal>> dimensionIndicatorMap = cityEntry.getValue();

            for (DimensionDefinition dimension : dimensions) {
                Long dimensionId = dimension.getId();
                String dimensionCode = dimension.getDimensionCode();

                Map<String, BigDecimal> indicatorScores = dimensionIndicatorMap.get(dimensionId);

                if (indicatorScores == null || indicatorScores.isEmpty()) {
                    log.warn("城市ID={}在维度{}下没有指标数据", cityId, dimensionCode);
                    continue;
                }

                // 获取维度得分计算策略
                DimensionScoreCalculationStrategy strategy = strategyManager.getDimensionStrategy(dimensionCode);

                if (strategy == null) {
                    log.warn("维度{}未配置计算策略", dimensionCode);
                    continue;
                }

                try {
                    // 计算维度得分
                    BigDecimal dimensionScore = strategy.calculate(indicatorScores);

                    if (dimensionScore == null) {
                        log.warn("维度得分计算返回null，使用默认策略");
                        // 使用默认策略
                        DimensionScoreCalculationStrategy defaultStrategy = strategyManager.getDimensionStrategy("");
                        dimensionScore = defaultStrategy.calculate(indicatorScores);
                    }

                    // 检查是否已存在
                    boolean exists = dimensionScoreRepository.existsByCityIdAndYearAndDimensionId(
                            cityId, year, dimensionId);

                    if (exists) {
                        log.debug("维度得分已存在，跳过: 城市ID={}, 年份={}, 维度={}",
                                cityId, year, dimensionCode);
                        continue;
                    }

                    // 保存维度得分
                    DimensionScore score = DimensionScore.builder()
                            .cityId(cityId)
                            .year(year)
                            .dimensionId(dimensionId)
                            .dimensionScore(dimensionScore)
                            .build();

                    dimensionScoreRepository.save(score);

//                    更新数据库值
                    // 查找是否已存在
//                    Optional<DimensionScore> existingOpt =
//                            dimensionScoreRepository.findByCityIdAndYearAndDimensionId(
//                                    cityId, year, dimensionId);
//
//                    if (existingOpt.isPresent()) {
//                        // 存在则更新
//                        DimensionScore existing = existingOpt.get();
//                        existing.setDimensionScore(dimensionScore);
//                        existing.setUpdatedAt(LocalDateTime.now());
//
//                        dimensionScoreRepository.save(existing);
//
//                        log.debug("维度得分已更新（覆盖）: 城市ID={}, 年份={}, 维度={}, 新值={}",
//                                cityId, year, dimensionCode, dimensionScore);
//                    } else {
//                        // 不存在则新增
//                        DimensionScore score = DimensionScore.builder()
//                                .cityId(cityId)
//                                .year(year)
//                                .dimensionId(dimensionId)
//                                .dimensionScore(dimensionScore)
//                                .build();
//
//                        dimensionScoreRepository.save(score);
//
//                        log.debug("维度得分已新增: 城市ID={}, 年份={}, 维度={}",
//                                cityId, year, dimensionCode);
//                    }

                    successCount++;

                } catch (Exception e) {
                    log.error("计算维度得分失败: 城市ID={}, 维度={}", cityId, dimensionCode, e);
                }
            }
        }

        log.info("{}年维度得分计算完成，共保存{}条记录", year, successCount);

        return successCount;
    }

    /**
     * 计算并保存某年的完整得分（指标+维度）
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void calculateAndSaveAllScores(Integer year) {
        log.info("========== 开始计算{}年的完整得分 ==========", year);

        // 1. 计算指标得分
        int indicatorCount = calculateAndSaveIndicatorScores(year);
        log.info("指标得分计算完成: {}条", indicatorCount);

        // 2. 计算维度得分
        int dimensionCount = calculateAndSaveDimensionScores(year);
        log.info("维度得分计算完成: {}条", dimensionCount);

        log.info("========== {}年完整得分计算完成 ==========", year);
    }
}