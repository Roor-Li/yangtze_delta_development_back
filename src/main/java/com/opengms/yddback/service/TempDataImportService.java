package com.opengms.yddback.service;

import com.opengms.yddback.entity.*;
import com.opengms.yddback.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据导入服务
 */
@Slf4j
@Service
@RequiredArgsConstructor

public class TempDataImportService {

    private final CityInfoRepository cityInfoRepository;
    private final CityGeometryRepository cityGeometryRepository;
    private final DimensionDefinitionRepository dimensionRepository;
    private final IndicatorDefinitionRepository indicatorRepository;
    private final IndicatorCalculationResultRepository indicatorResultRepository;
    private final DimensionScoreRepository dimensionScoreRepository;
    private final DataImportLogRepository importLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * 执行完整的导入流程（23、24年数据 + GeoJSON）
     */
    @Transactional
    public void importPhase1Data(String dataRootPath) {
        log.info("========== 开始第一阶段数据导入 ==========");

        try {
            // 步骤1: 导入城市信息和地理数据
            importGeoData(dataRootPath + "/geo");

            // 步骤2: 导入23年数据
            importYearData(2023, dataRootPath + "/2023");

            // 步骤3: 导入24年数据
            importYearData(2024, dataRootPath + "/2024");

            log.info("========== 第一阶段数据导入完成 ==========");

        } catch (Exception e) {
            log.error("数据导入失败", e);
            throw new RuntimeException("数据导入失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导入地理数据（城市信息、中心点、边界）
     */
    @Transactional
    public void importGeoData(String geoDataPath) throws IOException {
        log.info("开始导入地理数据...");

        DataImportLog importLog = createImportLog(null, "geo_data");

        try {
            // 1. 导入城市中心点（同时创建城市基础信息）
            String centerPath = geoDataPath + "/city_centers.geojson";
            int cityCount = importCityCenters(centerPath);
            log.info("导入城市中心点完成，共{}个城市", cityCount);

            // 2. 导入城市边界
            String boundaryPath = geoDataPath + "/city_boundaries.geojson";
            int boundaryCount = importCityBoundaries(boundaryPath);
            log.info("导入城市边界完成，共{}个边界", boundaryCount);

            // 更新导入日志
            importLog.setSuccessCount(cityCount);
            importLog.setRecordCount(cityCount);
            importLog.setCityCount(cityCount);
            importLog.setImportStatus("success");
            importLog.setEndTime(LocalDateTime.now());
            importLogRepository.save(importLog);

            log.info("地理数据导入成功");

        } catch (Exception e) {
            importLog.setImportStatus("failed");
            importLog.setErrorDetail(e.getMessage());
            importLog.setEndTime(LocalDateTime.now());
            importLogRepository.save(importLog);
            throw e;
        }
    }

    /**
     * 导入城市中心点（GeoJSON格式）
     */
    private int importCityCenters(String filePath) throws IOException {
        log.info("读取城市中心点文件: {}", filePath);

        JsonNode root = objectMapper.readTree(new File(filePath));
        JsonNode features = root.get("features");

        int count = 0;
        for (JsonNode feature : features) {
            JsonNode properties = feature.get("properties");

            // 提取城市信息
            String cityCode = properties.get("cityCODE").asText();
            String cityName = properties.get("name").asText();
            String provinceCode = properties.get("proCODE").asText();
            String provinceName = properties.get("省份").asText();
            Integer objectId = properties.get("OBJECTID").asInt();
            Integer origFid = properties.has("ORIG_FID") ? properties.get("ORIG_FID").asInt() : null;

            // 检查城市是否已存在
            Optional<CityInfo> existingCity = cityInfoRepository.findByCityCode(cityCode);
            CityInfo city;

            if (existingCity.isPresent()) {
                city = existingCity.get();
                log.debug("城市已存在，跳过: {}", cityName);
            } else {
                // 创建城市基础信息
                city = CityInfo.builder()
                        .cityCode(cityCode)
                        .cityName(cityName)
                        .provinceCode(provinceCode)
                        .provinceName(provinceName)
                        .objectId(objectId)
                        .origFid(origFid)
                        .build();
                city = cityInfoRepository.save(city);
                log.debug("创建城市: {}", cityName);
            }

            // 保存中心点几何数据
            Map<String, Object> geoJsonMap = objectMapper.convertValue(feature, Map.class);

            CityGeometry geometry = CityGeometry.builder()
                    .cityId(city.getId())
                    .geometryType(CityGeometry.GeometryType.CENTER_POINT)
                    .geojsonData(geoJsonMap)
                    .build();

            cityGeometryRepository.save(geometry);
            count++;
        }

        return count;
    }

    /**
     * 导入城市边界（GeoJSON格式）
     */
    private int importCityBoundaries(String filePath) throws IOException {
        log.info("读取城市边界文件: {}", filePath);

        JsonNode root = objectMapper.readTree(new File(filePath));
        JsonNode features = root.get("features");

        int count = 0;
        int notFoundCount = 0;

        for (JsonNode feature : features) {
            JsonNode properties = feature.get("properties");

            String cityCode = properties.get("cityCODE").asText();
            String cityName = properties.get("name").asText();

            // 查找对应的城市
            Optional<CityInfo> cityOpt = cityInfoRepository.findByCityCode(cityCode);

            if (cityOpt.isEmpty()) {
                log.warn("未找到城市: {} ({}), 跳过边界导入", cityName, cityCode);
                notFoundCount++;
                continue;
            }

            CityInfo city = cityOpt.get();

            // 检查是否已有边界数据
            Optional<CityGeometry> existingBoundary = cityGeometryRepository
                    .findByCityIdAndGeometryType(city.getId(), CityGeometry.GeometryType.BOUNDARY);

            if (existingBoundary.isPresent()) {
                log.debug("城市边界已存在，跳过: {}", cityName);
                continue;
            }

            // 保存边界几何数据
            Map<String, Object> geoJsonMap = objectMapper.convertValue(feature, Map.class);

            CityGeometry geometry = CityGeometry.builder()
                    .cityId(city.getId())
                    .geometryType(CityGeometry.GeometryType.BOUNDARY)
                    .geojsonData(geoJsonMap)
                    .build();

            cityGeometryRepository.save(geometry);
            count++;
        }

        if (notFoundCount > 0) {
            log.warn("有{}个边界未找到对应城市", notFoundCount);
        }

        return count;
    }

    /**
     * 导入某一年的数据（指标结果 + 维度得分）
     */
    @Transactional
    public void importYearData(int year, String yearDataPath) throws IOException {
        log.info("========== 开始导入{}年数据 ==========", year);

        // 1. 导入指标计算结果
        String indicatorPath = yearDataPath + "/indicator_results.json";
        int indicatorCount = importIndicatorResults(year, indicatorPath);
        log.info("{}年指标数据导入完成，共{}条", year, indicatorCount);

        // 2. 导入维度得分
        String dimensionPath = yearDataPath + "/dimension_scores.json";
        int dimensionCount = importDimensionScores(year, dimensionPath);
        log.info("{}年维度得分导入完成，共{}条", year, dimensionCount);

        log.info("========== {}年数据导入完成 ==========", year);
    }

    /**
     * 导入指标计算结果
     *
     * JSON格式：
     * {
     *   "创新发展": {
     *     "names": ["上海市", "南京市", ...],
     *     "科技拨款占财政拨款的比重": ["0.51", "0.76", ...],
     *     ...
     *   },
     *   ...
     * }
     */
    private int importIndicatorResults(int year, String filePath) throws IOException {
        log.info("读取指标结果文件: {}", filePath);

        DataImportLog importLog = createImportLog(year, "indicator_result");

        try {
            JsonNode root = objectMapper.readTree(new File(filePath));

            // 获取所有维度定义（用于名称映射）
            Map<String, DimensionDefinition> dimensionMap = getDimensionNameMap();

            // 获取所有指标定义（用于名称映射）
            Map<String, IndicatorDefinition> indicatorMap = getIndicatorNameMap();

            int totalCount = 0;
            int successCount = 0;
            int failedCount = 0;
            List<String> errors = new ArrayList<>();

            // 遍历每个维度
            Iterator<String> dimensionNames = root.fieldNames();
            while (dimensionNames.hasNext()) {
                String dimensionName = dimensionNames.next();
                JsonNode dimensionData = root.get(dimensionName);

                // 获取维度定义
                DimensionDefinition dimension = dimensionMap.get(dimensionName);
                if (dimension == null) {
                    String error = "未找到维度定义: " + dimensionName;
                    log.warn(error);
                    errors.add(error);
                    continue;
                }

                // 获取城市名称列表
                JsonNode namesNode = dimensionData.get("names");
                if (namesNode == null || !namesNode.isArray()) {
                    String error = "维度 " + dimensionName + " 缺少 names 字段";
                    log.warn(error);
                    errors.add(error);
                    continue;
                }

                List<String> cityNames = new ArrayList<>();
                for (JsonNode nameNode : namesNode) {
                    cityNames.add(nameNode.asText().trim());
                }

                // 遍历该维度下的所有指标
                Iterator<String> indicatorNames = dimensionData.fieldNames();
                while (indicatorNames.hasNext()) {
                    String indicatorName = indicatorNames.next();

                    // 跳过 names 字段
                    if ("names".equals(indicatorName)) {
                        continue;
                    }

                    // 获取指标定义
                    IndicatorDefinition indicator = indicatorMap.get(indicatorName);
                    if (indicator == null) {
                        String error = "未找到指标定义: " + indicatorName;
                        log.warn(error);
                        errors.add(error);
                        continue;
                    }

                    // 验证：指标是否属于当前维度
                    if (!indicator.getDimensionId().equals(dimension.getId())) {
                        String error = String.format("指标 %s 不属于维度 %s", indicatorName, dimensionName);
                        log.warn(error);
                        errors.add(error);
                        continue;
                    }

                    // 获取指标值列表
                    JsonNode valuesNode = dimensionData.get(indicatorName);
                    if (valuesNode == null || !valuesNode.isArray()) {
                        String error = "指标 " + indicatorName + " 缺少值数组";
                        log.warn(error);
                        errors.add(error);
                        continue;
                    }

                    // 验证：城市数量和值数量是否匹配
                    if (cityNames.size() != valuesNode.size()) {
                        String error = String.format("指标 %s 的城市数量(%d)和值数量(%d)不匹配",
                                indicatorName, cityNames.size(), valuesNode.size());
                        log.warn(error);
                        errors.add(error);
                        continue;
                    }

                    // 逐个城市导入指标值
                    for (int i = 0; i < cityNames.size(); i++) {
                        String cityName = cityNames.get(i);
                        String valueStr = valuesNode.get(i).asText().trim();

                        totalCount++;

                        try {
                            // 查找城市
                            Optional<CityInfo> cityOpt = cityInfoRepository.findByCityName(cityName);
                            if (cityOpt.isEmpty()) {
                                String error = "未找到城市: " + cityName;
                                log.warn(error);
                                errors.add(error);
                                failedCount++;
                                continue;
                            }

                            CityInfo city = cityOpt.get();

                            // 解析指标值（去除空格，处理空字符串）
                            BigDecimal indicatorValue = null;
                            if (!valueStr.isEmpty() && !valueStr.equals("-")) {
                                try {
                                    indicatorValue = new BigDecimal(valueStr);
                                } catch (NumberFormatException e) {
                                    String error = String.format("无法解析指标值: %s, 城市: %s, 指标: %s",
                                            valueStr, cityName, indicatorName);
                                    log.warn(error);
                                    errors.add(error);
                                    // 继续导入，将值设为null
                                }
                            }

                            // 检查是否已存在
                            boolean exists = indicatorResultRepository.existsByCityIdAndYearAndIndicatorId(
                                    city.getId(), year, indicator.getId());

                            if (exists) {
                                log.debug("指标结果已存在，跳过: 城市={}, 年份={}, 指标={}",
                                        cityName, year, indicatorName);
                                successCount++;
                                continue;
                            }

                            // 创建指标计算结果记录
                            IndicatorCalculationResult result = IndicatorCalculationResult.builder()
                                    .cityId(city.getId())
                                    .year(year)
                                    .dimensionId(dimension.getId())
                                    .indicatorId(indicator.getId())
                                    .indicatorValue(indicatorValue)
                                    .build();

                            indicatorResultRepository.save(result);
                            successCount++;

                        } catch (Exception e) {
                            String error = String.format("导入失败: 城市=%s, 指标=%s, 错误=%s",
                                    cityName, indicatorName, e.getMessage());
                            log.error(error, e);
                            errors.add(error);
                            failedCount++;
                        }
                    }
                }
            }

            // 更新导入日志
            importLog.setRecordCount(totalCount);
            importLog.setSuccessCount(successCount);
            importLog.setFailedCount(failedCount);
            importLog.setImportStatus(failedCount == 0 ? "success" : "partial");
            if (!errors.isEmpty()) {
                importLog.setErrorDetail(String.join("\n", errors.subList(0, Math.min(errors.size(), 100))));
            }
            importLog.setEndTime(LocalDateTime.now());
            importLogRepository.save(importLog);

            log.info("指标结果导入完成: 总数={}, 成功={}, 失败={}", totalCount, successCount, failedCount);

            return successCount;

        } catch (Exception e) {
            importLog.setImportStatus("failed");
            importLog.setErrorDetail(e.getMessage());
            importLog.setEndTime(LocalDateTime.now());
            importLogRepository.save(importLog);
            throw e;
        }
    }

    /**
     * 导入维度得分
     *
     * JSON格式：
     * [
     *   {
     *     "cityName": "上海市",
     *     "score": "87.58",
     *     "创新发展": "17.30",
     *     "协调发展": "13.50",
     *     ...
     *   },
     *   ...
     * ]
     */
    private int importDimensionScores(int year, String filePath) throws IOException {
        log.info("读取维度得分文件: {}", filePath);

        DataImportLog importLog = createImportLog(year, "dimension_score");

        try {
            JsonNode root = objectMapper.readTree(new File(filePath));

            // 获取所有维度定义（用于名称映射）
            Map<String, DimensionDefinition> dimensionMap = getDimensionNameMap();

            int totalCount = 0;
            int successCount = 0;
            int failedCount = 0;
            List<String> errors = new ArrayList<>();

            // 遍历每个城市
            for (JsonNode cityNode : root) {
                String cityName = cityNode.get("cityName").asText().trim();
                String totalScoreStr = cityNode.has("score") ? cityNode.get("score").asText().trim() : null;

                try {
                    // 查找城市
                    Optional<CityInfo> cityOpt = cityInfoRepository.findByCityName(cityName);
                    if (cityOpt.isEmpty()) {
                        String error = "未找到城市: " + cityName;
                        log.warn(error);
                        errors.add(error);
                        failedCount++;
                        continue;
                    }

                    CityInfo city = cityOpt.get();

                    // 遍历每个维度得分
                    for (Map.Entry<String, DimensionDefinition> entry : dimensionMap.entrySet()) {
                        String dimensionName = entry.getKey();
                        DimensionDefinition dimension = entry.getValue();

                        if (!cityNode.has(dimensionName)) {
                            String error = String.format("城市 %s 缺少维度 %s 的得分", cityName, dimensionName);
                            log.warn(error);
                            errors.add(error);
                            failedCount++;
                            totalCount++;
                            continue;
                        }

                        String scoreStr = cityNode.get(dimensionName).asText().trim();
                        totalCount++;

                        try {
                            // 解析得分
                            BigDecimal dimensionScore = null;
                            if (!scoreStr.isEmpty() && !scoreStr.equals("-")) {
                                try {
                                    dimensionScore = new BigDecimal(scoreStr);

                                    // 验证：得分范围应在0-20之间（每个维度满分20）
                                    if (dimensionScore.compareTo(BigDecimal.ZERO) < 0 ||
                                            dimensionScore.compareTo(new BigDecimal("20")) > 0) {
                                        String error = String.format("维度得分超出范围[0-20]: 城市=%s, 维度=%s, 得分=%s",
                                                cityName, dimensionName, scoreStr);
                                        log.warn(error);
                                        errors.add(error);
                                    }
                                } catch (NumberFormatException e) {
                                    String error = String.format("无法解析维度得分: %s, 城市: %s, 维度: %s",
                                            scoreStr, cityName, dimensionName);
                                    log.warn(error);
                                    errors.add(error);
                                    // 继续导入，将得分设为null
                                }
                            }

                            // 检查是否已存在
                            boolean exists = dimensionScoreRepository.existsByCityIdAndYearAndDimensionId(
                                    city.getId(), year, dimension.getId());

                            if (exists) {
                                log.debug("维度得分已存在，跳过: 城市={}, 年份={}, 维度={}",
                                        cityName, year, dimensionName);
                                successCount++;
                                continue;
                            }

                            // 创建维度得分记录
                            DimensionScore score = DimensionScore.builder()
                                    .cityId(city.getId())
                                    .year(year)
                                    .dimensionId(dimension.getId())
                                    .dimensionScore(dimensionScore)
                                    .build();

                            dimensionScoreRepository.save(score);
                            successCount++;

                        } catch (Exception e) {
                            String error = String.format("导入维度得分失败: 城市=%s, 维度=%s, 错误=%s",
                                    cityName, dimensionName, e.getMessage());
                            log.error(error, e);
                            errors.add(error);
                            failedCount++;
                        }
                    }

                } catch (Exception e) {
                    String error = String.format("处理城市失败: %s, 错误=%s", cityName, e.getMessage());
                    log.error(error, e);
                    errors.add(error);
                    failedCount += 5; // 该城市的5个维度都失败
                    totalCount += 5;
                }
            }

            // 更新导入日志
            importLog.setRecordCount(totalCount);
            importLog.setSuccessCount(successCount);
            importLog.setFailedCount(failedCount);
            importLog.setImportStatus(failedCount == 0 ? "success" : "partial");
            if (!errors.isEmpty()) {
                importLog.setErrorDetail(String.join("\n", errors.subList(0, Math.min(errors.size(), 100))));
            }
            importLog.setEndTime(LocalDateTime.now());
            importLogRepository.save(importLog);

            log.info("维度得分导入完成: 总数={}, 成功={}, 失败={}", totalCount, successCount, failedCount);

            return successCount;

        } catch (Exception e) {
            importLog.setImportStatus("failed");
            importLog.setErrorDetail(e.getMessage());
            importLog.setEndTime(LocalDateTime.now());
            importLogRepository.save(importLog);
            throw e;
        }
    }

    /**
     * 获取维度名称到维度定义的映射
     */
    private Map<String, DimensionDefinition> getDimensionNameMap() {
        List<DimensionDefinition> dimensions = dimensionRepository.findAll();
        Map<String, DimensionDefinition> map = new HashMap<>();
        for (DimensionDefinition dimension : dimensions) {
            map.put(dimension.getDimensionName(), dimension);
        }
        return map;
    }

    /**
     * 获取指标名称到指标定义的映射
     */
    private Map<String, IndicatorDefinition> getIndicatorNameMap() {
        List<IndicatorDefinition> indicators = indicatorRepository.findAll();
        Map<String, IndicatorDefinition> map = new HashMap<>();
        for (IndicatorDefinition indicator : indicators) {
            map.put(indicator.getIndicatorName(), indicator);
        }
        return map;
    }

    /**
     * 创建导入日志记录
     */
    private DataImportLog createImportLog(Integer year, String importType) {
        DataImportLog log = DataImportLog.builder()
                .importYear(year)
                .importType(importType)
                .importStatus("processing")
                .startTime(LocalDateTime.now())
                .operator("system")
                .build();
        return importLogRepository.save(log);
    }
}
