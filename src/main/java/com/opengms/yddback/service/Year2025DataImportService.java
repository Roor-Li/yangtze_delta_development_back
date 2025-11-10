package com.opengms.yddback.service;

import com.opengms.yddback.dto.ExcelColumnMapping;
import com.opengms.yddback.entity.*;
import com.opengms.yddback.repository.*;
import com.opengms.yddback.util.ExcelReaderUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 25年基础数据导入服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Year2025DataImportService {

    private final ExcelReaderUtil excelReaderUtil;
    private final CityInfoRepository cityInfoRepository;
    private final BasicStatisticsDataRepository statisticsRepository;
    private final StatisticsItemMetadataRepository metadataRepository;
    private final DataImportLogRepository importLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * 第一步：生成列映射配置文件
     *
     * @param excelFilePath Excel文件路径
     * @param outputMappingFile 输出的映射配置文件路径
     */
    public void generateMappingFile(String excelFilePath, String outputMappingFile) throws IOException {
        log.info("========== 开始生成列映射配置文件 ==========");
        log.info("Excel文件: {}", excelFilePath);
        log.info("输出文件: {}", outputMappingFile);

        // 读取Excel并生成映射
        List<ExcelColumnMapping> mappings = excelReaderUtil.generateColumnMappings(excelFilePath);

        // 将映射写入JSON文件
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(outputMappingFile), mappings);

        log.info("列映射配置文件已生成: {}", outputMappingFile);
        log.info("请检查并修改该文件，确保映射正确，然后执行导入操作");
        log.info("========== 生成完成 ==========");

        // 打印摘要
        printMappingSummary(mappings);
    }

    /**
     * 第二步：根据修改后的映射配置导入数据
     *
     * @param excelFilePath Excel文件路径
     * @param mappingFile 映射配置文件路径（已人工审核修改）
     */
    @Transactional
    public void importDataWithMapping(String excelFilePath, String mappingFile) throws IOException {
        log.info("========== 开始导入25年基础数据 ==========");
        log.info("Excel文件: {}", excelFilePath);
        log.info("映射文件: {}", mappingFile);

        DataImportLog importLog = createImportLog(2025, "basic_data");

        try {
            // 1. 读取映射配置
            String mappingJson = new String(Files.readAllBytes(Paths.get(mappingFile)));
            List<ExcelColumnMapping> mappings = Arrays.asList(
                    objectMapper.readValue(mappingJson, ExcelColumnMapping[].class));

            log.info("读取到{}个列映射配置", mappings.size());

            // 2. 保存统计项元数据
            saveStatisticsMetadata(mappings);

            // 3. 读取Excel数据
            Map<String, Map<Integer, Map<String, Object>>> excelData =
                    excelReaderUtil.readExcelData(excelFilePath, mappings);

            log.info("读取到{}个城市的数据", excelData.size());

            // 4. 导入到数据库
            int totalRecords = 0;
            int successCount = 0;
            int failedCount = 0;
            List<String> errors = new ArrayList<>();

            for (Map.Entry<String, Map<Integer, Map<String, Object>>> cityEntry : excelData.entrySet()) {
                String cityName = cityEntry.getKey();
                Map<Integer, Map<String, Object>> yearDataMap = cityEntry.getValue();

                // 查找城市
                Optional<CityInfo> cityOpt = cityInfoRepository.findByCityName(cityName);
                if (cityOpt.isEmpty()) {
                    String error = "未找到城市: " + cityName;
                    log.warn(error);
                    errors.add(error);
                    failedCount += yearDataMap.size();
                    totalRecords += yearDataMap.size();
                    continue;
                }

                CityInfo city = cityOpt.get();

                // 按年份导入数据
                for (Map.Entry<Integer, Map<String, Object>> yearEntry : yearDataMap.entrySet()) {
                    Integer year = yearEntry.getKey();
                    Map<String, Object> dataContent = yearEntry.getValue();

                    totalRecords++;

                    try {
                        // 检查是否已存在
                        Optional<BasicStatisticsData> existing =
                                statisticsRepository.findLatestByCityIdAndYear(city.getId(), year);

                        if (existing.isPresent()) {
                            log.debug("数据已存在，跳过: 城市={}, 年份={}", cityName, year);
                            successCount++;
                            continue;
                        }

                        // 创建记录
                        BasicStatisticsData statistics = BasicStatisticsData.builder()
                                .cityId(city.getId())
                                .year(year)
                                .dataContent(dataContent)
                                .dataVersion(1)
                                .build();

                        statisticsRepository.save(statistics);
                        successCount++;

                        log.debug("导入成功: 城市={}, 年份={}, 字段数={}",
                                cityName, year, dataContent.size());

                    } catch (Exception e) {
                        String error = String.format("导入失败: 城市=%s, 年份=%d, 错误=%s",
                                cityName, year, e.getMessage());
                        log.error(error, e);
                        errors.add(error);
                        failedCount++;
                    }
                }
            }

            // 更新导入日志
            importLog.setCityCount(excelData.size());
            importLog.setRecordCount(totalRecords);
            importLog.setSuccessCount(successCount);
            importLog.setFailedCount(failedCount);
            importLog.setImportStatus(failedCount == 0 ? "success" : "partial");
            if (!errors.isEmpty()) {
                importLog.setErrorDetail(String.join("\n", errors.subList(0, Math.min(errors.size(), 100))));
            }
            importLog.setEndTime(LocalDateTime.now());
            importLogRepository.save(importLog);

            log.info("========== 导入完成 ==========");
            log.info("总记录数: {}, 成功: {}, 失败: {}", totalRecords, successCount, failedCount);

        } catch (Exception e) {
            importLog.setImportStatus("failed");
            importLog.setErrorDetail(e.getMessage());
            importLog.setEndTime(LocalDateTime.now());
            importLogRepository.save(importLog);
            throw e;
        }
    }

    /**
     * 保存统计项元数据
     */
    private void saveStatisticsMetadata(List<ExcelColumnMapping> mappings) {
        log.info("保存统计项元数据...");

        for (ExcelColumnMapping mapping : mappings) {
            if (mapping.isSkip()) {
                continue;
            }

            String itemCode = mapping.getSuggestedItemCode();

            // 检查是否已存在
            if (metadataRepository.existsByItemCode(itemCode)) {
                log.debug("统计项元数据已存在: {}", itemCode);
                continue;
            }

            // 创建元数据记录
            StatisticsItemMetadata metadata = StatisticsItemMetadata.builder()
                    .itemCode(itemCode)
                    .itemName(mapping.getItemName())
                    .unit(mapping.getRemark())  // 将备注作为单位
                    .remark(mapping.getRemark())
                    .dataType("numeric")
                    .isActive(true)
                    .build();

            metadataRepository.save(metadata);
            log.debug("保存统计项元数据: {} -> {}", itemCode, mapping.getItemName());
        }

        log.info("统计项元数据保存完成");
    }

    /**
     * 打印映射摘要
     */
    private void printMappingSummary(List<ExcelColumnMapping> mappings) {
        System.out.println("\n========== 列映射摘要 ==========");
        System.out.println("总列数: " + mappings.size());

        // 按年份分组统计
        Map<Integer, Long> yearCounts = mappings.stream()
                .collect(Collectors.groupingBy(ExcelColumnMapping::getYear, Collectors.counting()));

        System.out.println("\n按年份统计:");
        yearCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> System.out.println("  " + entry.getKey() + "年: " + entry.getValue() + "列"));

        System.out.println("\n前10个映射示例:");
        mappings.stream()
                .limit(10)
                .forEach(m -> System.out.printf("  %s -> %s (年份: %d, 备注: %s)%n",
                        m.getOriginalColumnName(), m.getSuggestedItemCode(), m.getYear(), m.getRemark()));

        System.out.println("\n请检查生成的映射文件，特别注意:");
        System.out.println("  1. 相同统计项是否映射到同一个字段代码");
        System.out.println("  2. 不同统计项是否映射到不同字段代码");
        System.out.println("  3. 年份提取是否正确");
        System.out.println("  4. 是否有需要跳过的列（将skip设为true）");
        System.out.println("==============================\n");
    }

    /**
     * 创建导入日志
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