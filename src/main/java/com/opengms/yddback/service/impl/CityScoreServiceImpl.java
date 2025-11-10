package com.opengms.yddback.service.impl;

import com.opengms.yddback.dto.*;
import com.opengms.yddback.entity.*;
import com.opengms.yddback.repository.*;
import com.opengms.yddback.service.CityScoreService;
import com.opengms.yddback.service.ScoreCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 城市得分服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CityScoreServiceImpl implements CityScoreService {

    private final DimensionScoreRepository dimensionScoreRepository;
    private final CityInfoRepository cityInfoRepository;
    private final DimensionDefinitionRepository dimensionDefinitionRepository;
    private final IndicatorDefinitionRepository indicatorDefinitionRepository;
    private final IndicatorCalculationResultRepository indicatorResultRepository;

    private final ScoreCalculationService scoreCalculationService;

    /**
     * 查询某年所有城市的总分及排名
     *
     * @param year 年份
     * @return 城市总分排名列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<CityTotalScoreDTO> getCityTotalScoreRanking(Integer year) {
        log.info("查询{}年所有城市的总分排名", year);

        // 1. 查询该年份所有城市的维度得分
        List<DimensionScore> dimensionScores = dimensionScoreRepository.findByYear(year);

        if (dimensionScores.isEmpty()) {
            log.warn("未找到{}年的维度得分数据", year);
            return Collections.emptyList();
        }

        // 如果没有数据，尝试计算
//        if (dimensionScores.isEmpty()) {
//            log.info("未找到{}年的维度得分数据，尝试自动计算", year);
//
//            try {
//                // 先计算指标，再计算维度
//                scoreCalculationService.calculateAndSaveAllScores(year);
//
//                // 重新查询
//                dimensionScores = dimensionScoreRepository.findByYear(year);
//
//                if (dimensionScores.isEmpty()) {
//                    log.warn("自动计算后仍未找到数据");
//                    return Collections.emptyList();
//                }
//            } catch (Exception e) {
//                log.error("自动计算失败", e);
//                throw new RuntimeException("数据计算失败: " + e.getMessage(), e);
//            }
//        }

        // 2. 按城市ID分组，计算每个城市的总分
        Map<Long, BigDecimal> cityTotalScores = dimensionScores.stream()
                .collect(Collectors.groupingBy(
                        DimensionScore::getCityId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                score -> score.getDimensionScore() != null ? score.getDimensionScore() : BigDecimal.ZERO,
                                BigDecimal::add
                        )
                ));

        log.info("计算出{}个城市的总分", cityTotalScores.size());

        // 3. 批量查询城市信息
        List<Long> cityIds = new ArrayList<>(cityTotalScores.keySet());
        List<CityInfo> cities = cityInfoRepository.findAllById(cityIds);

        // 创建城市ID到城市信息的映射
        Map<Long, CityInfo> cityMap = cities.stream()
                .collect(Collectors.toMap(CityInfo::getId, city -> city));

        // 4. 构建结果列表
        List<CityTotalScoreDTO> results = cityTotalScores.entrySet().stream()
                .map(entry -> {
                    Long cityId = entry.getKey();
                    BigDecimal totalScore = entry.getValue();
                    CityInfo city = cityMap.get(cityId);

                    if (city == null) {
                        log.warn("未找到城市ID为{}的城市信息", cityId);
                        return null;
                    }

                    return CityTotalScoreDTO.builder()
                            .cityId(cityId)
                            .cityName(city.getCityName())
                            .score(formatScore(totalScore))  // 格式化分数
                            .build();
                })
                .filter(Objects::nonNull)  // 过滤掉null值
                .collect(Collectors.toList());

        // 5. 按总分降序排序
        results.sort((a, b) -> {
            BigDecimal scoreA = new BigDecimal(a.getScore());
            BigDecimal scoreB = new BigDecimal(b.getScore());
            return scoreB.compareTo(scoreA);  // 降序
        });

        // 6. 设置排名
        for (int i = 0; i < results.size(); i++) {
            results.get(i).setRanking(i + 1);
        }

        log.info("查询完成，返回{}个城市的排名数据", results.size());

        return results;
    }

    /**
     * 查询某年所有城市的总分及排名
     *
     * @param year 年份
     * @return 城市得分列表
     */
    public List<CityTotalScoreDTO> getCityTotalScoreRankingAndAutoCalculation(Integer year) {
        log.info("查询{}年所有城市的总分及排名（自动计算）", year);

        List<DimensionScore> dimensionScores = dimensionScoreRepository.findByYear(year);

        if (dimensionScores.isEmpty()) {
            log.info("未找到{}年维度得分数据，开始自动计算", year);

            try {
                scoreCalculationService.calculateAndSaveAllScores(year);
                log.info("自动计算完成");
            } catch (Exception e) {
                log.error("自动计算失败", e);
                throw new RuntimeException("数据自动计算失败" + e.getMessage(), e);
            }
        }
// 更新数据库
//        try {
//            scoreCalculationService.calculateAndSaveAllScores(year);
//            log.info("自动计算完成");
//        } catch (Exception e) {
//            log.error("自动计算失败", e);
//            throw new RuntimeException("数据自动计算失败" + e.getMessage(), e);
//        }

        return getCityTotalScoreRanking(year);
    }


    /**
     * 查询某年所有城市的总分及各维度得分
     *
     * @param year 年份
     * @return 城市得分列表（包含总分和各维度得分）
     */
    @Override
    @Transactional(readOnly = true)
    public List<CityScoreWithDimensionsDTO> getCityScoresWithDimensions(Integer year) {
        log.info("查询{}年所有城市的总分及各维度得分", year);

        // 1. 查询该年份所有城市的维度得分
        List<DimensionScore> dimensionScores = dimensionScoreRepository.findByYear(year);

        if (dimensionScores.isEmpty()) {
            log.warn("未找到{}年的维度得分数据", year);
            return Collections.emptyList();
        }

        // 如果没有数据，尝试计算
//        if (dimensionScores.isEmpty()) {
//            log.info("未找到{}年的维度得分数据，尝试自动计算", year);
//
//            try {
//                // 先计算指标，再计算维度
//                scoreCalculationService.calculateAndSaveAllScores(year);
//
//                // 重新查询
//                dimensionScores = dimensionScoreRepository.findByYear(year);
//
//                if (dimensionScores.isEmpty()) {
//                    log.warn("自动计算后仍未找到数据");
//                    return Collections.emptyList();
//                }
//            } catch (Exception e) {
//                log.error("自动计算失败", e);
//                throw new RuntimeException("数据计算失败: " + e.getMessage(), e);
//            }
//        }

        // 2. 查询所有维度定义（按显示顺序排序）
        List<DimensionDefinition> dimensions = dimensionDefinitionRepository.findAllByOrderByDisplayOrder();

        // 创建维度ID到维度名称的映射
        Map<Long, String> dimensionNameMap = dimensions.stream()
                .collect(Collectors.toMap(
                        DimensionDefinition::getId,
                        DimensionDefinition::getDimensionName
                ));

        log.info("查询到{}个维度定义", dimensions.size());

        // 3. 按城市ID分组
        Map<Long, List<DimensionScore>> cityScoresMap = dimensionScores.stream()
                .collect(Collectors.groupingBy(DimensionScore::getCityId));

        // 4. 批量查询城市信息
        List<Long> cityIds = new ArrayList<>(cityScoresMap.keySet());
        List<CityInfo> cities = cityInfoRepository.findAllById(cityIds);

        // 创建城市ID到城市信息的映射
        Map<Long, CityInfo> cityMap = cities.stream()
                .collect(Collectors.toMap(CityInfo::getId, city -> city));

        // 5. 构建结果列表
        List<CityScoreWithDimensionsDTO> results = cityScoresMap.entrySet().stream()
                .map(entry -> {
                    Long cityId = entry.getKey();
                    List<DimensionScore> cityDimensionScores = entry.getValue();
                    CityInfo city = cityMap.get(cityId);

                    if (city == null) {
                        log.warn("未找到城市ID为{}的城市信息", cityId);
                        return null;
                    }

                    // 计算总分
                    BigDecimal totalScore = cityDimensionScores.stream()
                            .map(ds -> ds.getDimensionScore() != null ? ds.getDimensionScore() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // 创建DTO
                    CityScoreWithDimensionsDTO dto = CityScoreWithDimensionsDTO.builder()
                            .cityId(cityId)
                            .cityName(city.getCityName())
                            .score(formatScore(totalScore))
                            .dimensionScores(new HashMap<>())
                            .build();

                    // 添加各维度得分
                    for (DimensionScore dimensionScore : cityDimensionScores) {
                        String dimensionName = dimensionNameMap.get(dimensionScore.getDimensionId());
                        if (dimensionName != null) {
                            String score = formatScore(dimensionScore.getDimensionScore());
                            dto.addDimensionScore(dimensionName, score);
                        }
                    }

                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 6. 按总分降序排序（可选，如果需要排序的话）
        results.sort((a, b) -> {
            BigDecimal scoreA = new BigDecimal(a.getScore());
            BigDecimal scoreB = new BigDecimal(b.getScore());
            return scoreB.compareTo(scoreA);
        });

        log.info("查询完成，返回{}个城市的得分数据", results.size());

        return results;
    }

    /**
     * 查询某年所有城市的总分及各维度得分（支持自动计算）
     */
    public List<CityScoreWithDimensionsDTO> getCityScoresWithDimensionsAndAutoCalculation(Integer year) {
        log.info("查询{}年所有城市的总分及各维度得分（自动计算模式）", year);

        List<DimensionScore> dimensionScores = dimensionScoreRepository.findByYear(year);

        if (dimensionScores.isEmpty()) {
            log.info("未找到{}年的维度得分数据，开始自动计算", year);

            try {
                scoreCalculationService.calculateAndSaveAllScores(year);
                log.info("自动计算完成");
            } catch (Exception e) {
                log.error("自动计算失败", e);
                throw new RuntimeException("数据计算失败: " + e.getMessage(), e);
            }
        }

        return getCityScoresWithDimensions(year);
    }

    /**
     * 查询某年所有城市的二级指标归一化得分
     * 如果没有数据，自动触发计算
     *
     * @param year 年份
     * @return 所有城市的指标归一化得分
     */
    @Override
    @Transactional(readOnly = true)
    public CityIndicatorScoresResponseDTO getCityIndicatorScores(Integer year) {
        log.info("查询{}年所有城市的二级指标归一化得分", year);

        // 1. 查询所有维度定义（按显示顺序）
        List<DimensionDefinition> dimensions = dimensionDefinitionRepository.findAllByOrderByDisplayOrder();

        if (dimensions.isEmpty()) {
            log.warn("未找到维度定义");
            return CityIndicatorScoresResponseDTO.builder()
                    .dimensionData(new HashMap<>())
                    .keyName(new HashMap<>())
                    .build();
        }

        // 2. 查询所有指标定义（按维度和显示顺序）
        List<IndicatorDefinition> indicators = indicatorDefinitionRepository.findAllOrderedByDimensionAndDisplay();

        // 按维度ID分组指标
        Map<Long, List<IndicatorDefinition>> dimensionIndicatorsMap = indicators.stream()
                .collect(Collectors.groupingBy(IndicatorDefinition::getDimensionId));

        log.info("查询到{}个维度，{}个指标", dimensions.size(), indicators.size());

        // 3. 查询该年份所有城市的指标计算结果
        List<IndicatorCalculationResult> indicatorResults = indicatorResultRepository.findByYear(year);

        if (indicatorResults.isEmpty()) {
            log.warn("未找到{}年的指标计算结果", year);

            return buildEmptyResponse(dimensions, dimensionIndicatorsMap);
        }

        //如果没有数据，尝试计算
//        if (indicatorResults.isEmpty()) {
//            log.info("未找到{}年的指标计算结果，尝试自动计算", year);
//
//            try {
//                // 触发计算
//                int calculatedCount = scoreCalculationService.calculateAndSaveIndicatorScores(year);
//
//                if (calculatedCount > 0) {
//                    log.info("自动计算成功，重新查询数据");
//                    // 重新查询
//                    indicatorResults = indicatorResultRepository.findByYear(year);
//                } else {
//                    log.warn("自动计算未生成任何数据");
//                    return buildEmptyResponse(dimensions, dimensionIndicatorsMap);
//                }
//            } catch (Exception e) {
//                log.error("自动计算失败", e);
//                throw new RuntimeException("数据计算失败: " + e.getMessage(), e);
//            }
//        }

        // 4. 获取所有涉及的城市ID，并按某种顺序排序（如按城市名称或ID）
        Set<Long> cityIdSet = indicatorResults.stream()
                .map(IndicatorCalculationResult::getCityId)
                .collect(Collectors.toSet());

        List<CityInfo> cities = cityInfoRepository.findAllById(cityIdSet);

        // 按城市名称排序（或按ID排序，保持一致性）
        cities.sort(Comparator.comparing(CityInfo::getCityName));

        // 创建城市ID到城市信息的映射
        Map<Long, CityInfo> cityMap = cities.stream()
                .collect(Collectors.toMap(CityInfo::getId, city -> city));

        // 5. 按城市ID和指标ID组织数据
        // Map<城市ID, Map<指标ID, 指标值>>
        Map<Long, Map<Long, BigDecimal>> cityIndicatorValuesMap = indicatorResults.stream()
                .collect(Collectors.groupingBy(
                        IndicatorCalculationResult::getCityId,
                        Collectors.toMap(
                                IndicatorCalculationResult::getIndicatorId,
                                result -> result.getIndicatorValue() != null ? result.getIndicatorValue() : BigDecimal.ZERO
                        )
                ));

        // 6. 构建响应数据
        Map<String, DimensionIndicatorDataDTO> dimensionDataMap = new HashMap<>();
        Map<String, List<String>> keyNameMap = new HashMap<>();

        for (DimensionDefinition dimension : dimensions) {
            String dimensionName = dimension.getDimensionName();
            Long dimensionId = dimension.getId();

            // 获取该维度下的所有指标（按显示顺序）
            List<IndicatorDefinition> dimensionIndicators = dimensionIndicatorsMap.getOrDefault(dimensionId, new ArrayList<>());

            if (dimensionIndicators.isEmpty()) {
                continue;
            }

            // 构建城市名称列表
            List<String> cityNames = cities.stream()
                    .map(CityInfo::getCityName)
                    .collect(Collectors.toList());

            // 构建指标值映射
            Map<String, List<String>> indicatorValuesMap = new LinkedHashMap<>();

            // 构建指标名称列表（用于keyName）
            List<String> indicatorNamesList = new ArrayList<>();

            for (IndicatorDefinition indicator : dimensionIndicators) {
                String indicatorName = indicator.getIndicatorName();
                Long indicatorId = indicator.getId();

                indicatorNamesList.add(indicatorName);

                // 为每个城市获取该指标的值（按cities列表的顺序）
                List<String> indicatorValues = cities.stream()
                        .map(city -> {
                            Map<Long, BigDecimal> cityIndicators = cityIndicatorValuesMap.get(city.getId());
                            if (cityIndicators != null && cityIndicators.containsKey(indicatorId)) {
                                BigDecimal value = cityIndicators.get(indicatorId);
                                return formatIndicatorValue(value);
                            } else {
                                // 如果没有数据，返回空字符串或"0"
                                return "0";
                            }
                        })
                        .collect(Collectors.toList());

                indicatorValuesMap.put(indicatorName, indicatorValues);
            }

            // 构建该维度的数据DTO
            DimensionIndicatorDataDTO dimensionData = DimensionIndicatorDataDTO.builder()
                    .names(cityNames)
                    .indicatorValues(indicatorValuesMap)
                    .build();

            dimensionDataMap.put(dimensionName, dimensionData);
            keyNameMap.put(dimensionName, indicatorNamesList);
        }

        // 7. 构建最终响应
        CityIndicatorScoresResponseDTO response = CityIndicatorScoresResponseDTO.builder()
                .dimensionData(dimensionDataMap)
                .keyName(keyNameMap)
                .build();

        log.info("查询完成，返回{}个维度的指标数据", dimensionDataMap.size());

        return response;
    }

    /**
     * 查询某年所有城市的二级指标归一化得分（支持自动计算）
     * 这个方法没有事务注解，避免只读事务问题
     */
    public CityIndicatorScoresResponseDTO getCityIndicatorScoresWithAutoCalculation(Integer year) {
        log.info("查询{}年所有城市的二级指标归一化得分（自动计算模式）", year);

        // 1. 先检查是否有数据
        List<IndicatorCalculationResult> indicatorResults = indicatorResultRepository.findByYear(year);

        // 2. 如果没有数据，触发计算
        if (indicatorResults.isEmpty()) {
            log.info("未找到{}年的指标计算结果，开始自动计算", year);

            try {
                // 调用计算服务（使用新事务）
                scoreCalculationService.calculateAndSaveAllScores(year);
                log.info("自动计算完成");
            } catch (Exception e) {
                log.error("自动计算失败", e);
                throw new RuntimeException("数据计算失败: " + e.getMessage(), e);
            }
        }

        // 3. 调用查询方法获取数据
        return getCityIndicatorScores(year);
    }

    /**
     * 查询某年所有城市某维度的排名和得分
     * @param year 年份
     * @param dimension 维度名称
     * @return 所有城市维度排名和得分
     */
    @Override
    @Transactional(readOnly = true)
    public List<CityDimensionRankingDTO> getCityDimensionRanking(Integer year, String dimension) {
        List<CityDimensionRankingDTO> cityDimensionRankingDTOList;
        if (dimension == null || dimension.isEmpty()) {
            log.warn("查询维度错误，请检查");
            return Collections.emptyList();
        } else if (dimension.equals("综合发展")) {
            List<CityTotalScoreDTO> cityTotalScoreDTOList = getCityTotalScoreRanking(year);
            if (cityTotalScoreDTOList.isEmpty()) {
                log.warn("未查询到{}年，{}维度数据", year, dimension);
                return Collections.emptyList();
            }
            cityDimensionRankingDTOList = cityTotalScoreDTOList.stream()
                    .map(cityTotalScoreDTO ->  {
                            CityDimensionRankingDTO cityDimensionRankingDTO = new CityDimensionRankingDTO();
                            cityDimensionRankingDTO.setRank(cityTotalScoreDTO.getRanking());
                            cityDimensionRankingDTO.setCity(cityTotalScoreDTO.getCityName());
                            cityDimensionRankingDTO.setScore((new BigDecimal(cityTotalScoreDTO.getScore())));
                            return cityDimensionRankingDTO;
                    })
                    .toList();
        } else {
            Long dimensionId;
            Optional<DimensionDefinition> dimensionDefinitionOpt = dimensionDefinitionRepository.findByDimensionName(dimension);
            if (dimensionDefinitionOpt.isPresent()) {
                dimensionId = dimensionDefinitionOpt.get().getId();
            } else {
                log.warn("查询维度错误，请检查");
                return Collections.emptyList();
            }
            List<DimensionScore> dimensionScoreList = dimensionScoreRepository.findByYearAndDimensionIdOrderByDimensionScoreDesc(year, dimensionId);
            cityDimensionRankingDTOList = IntStream.range(0, dimensionScoreList.size())
                    .mapToObj(i -> {
                        DimensionScore dimensionScore = dimensionScoreList.get(i);
                        CityDimensionRankingDTO cityDimensionRankingDTO = new CityDimensionRankingDTO();
                        cityDimensionRankingDTO.setRank(i + 1);
                        cityDimensionRankingDTO.setCity(dimensionScore.getCity().getCityName());
                        cityDimensionRankingDTO.setScore(dimensionScore.getDimensionScore());
                        return cityDimensionRankingDTO;
                    })
                    .toList();
        }
        return cityDimensionRankingDTOList;
    }

    /**
     * 构建空响应（当没有数据时）
     */
    private CityIndicatorScoresResponseDTO buildEmptyResponse(
            List<DimensionDefinition> dimensions,
            Map<Long, List<IndicatorDefinition>> dimensionIndicatorsMap) {

        Map<String, List<String>> keyNameMap = new HashMap<>();

        for (DimensionDefinition dimension : dimensions) {
            List<IndicatorDefinition> dimensionIndicators = dimensionIndicatorsMap.getOrDefault(
                    dimension.getId(), new ArrayList<>());

            List<String> indicatorNames = dimensionIndicators.stream()
                    .map(IndicatorDefinition::getIndicatorName)
                    .collect(Collectors.toList());

            keyNameMap.put(dimension.getDimensionName(), indicatorNames);
        }

        return CityIndicatorScoresResponseDTO.builder()
                .dimensionData(new HashMap<>())
                .keyName(keyNameMap)
                .build();
    }

    /**
     * 格式化指标值（保留2位小数）
     *
     * @param value 原始值
     * @return 格式化后的字符串
     */
    private String formatIndicatorValue(BigDecimal value) {
        if (value == null) {
            return "0";
        }
        // 保留2位小数，四舍五入
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 格式化分数（保留10位小数，与你的示例一致）
     *
     * @param score 原始分数
     * @return 格式化后的分数字符串
     */
    private String formatScore(BigDecimal score) {
        if (score == null) {
            return "0";
        }
        // 保留10位小数，四舍五入
        return score.setScale(10, RoundingMode.HALF_UP).toPlainString();
    }
}