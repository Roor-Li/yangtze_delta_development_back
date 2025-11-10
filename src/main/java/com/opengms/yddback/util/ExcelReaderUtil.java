package com.opengms.yddback.util;

import com.opengms.yddback.dto.ExcelColumnMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel数据读取工具
 */
@Slf4j
@Component
public class ExcelReaderUtil {

    /**
     * 年份提取正则表达式
     * 匹配：2023、2023年、(2023)、【2023】等格式
     */
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");

    /**
     * 读取Excel文件并生成列映射
     *
     * @param filePath Excel文件路径
     * @return 列映射列表
     */
    public List<ExcelColumnMapping> generateColumnMappings(String filePath) throws IOException {
        log.info("读取Excel文件: {}", filePath);

        List<ExcelColumnMapping> mappings = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 读取第1行（列名）
            Row headerRow = sheet.getRow(0);
            // 读取第2行（备注/单位）
            Row remarkRow = sheet.getRow(1);

            if (headerRow == null) {
                throw new IllegalArgumentException("Excel文件第1行（列名行）为空");
            }

            int columnCount = headerRow.getLastCellNum();
            log.info("Excel共有{}列", columnCount);

            // 遍历每一列
            for (int i = 0; i < columnCount; i++) {
                Cell headerCell = headerRow.getCell(i);
                if (headerCell == null) {
                    continue;
                }

                String columnName = getCellValueAsString(headerCell).trim();

                // 跳过空列名
                if (columnName.isEmpty()) {
                    continue;
                }

                // 跳过"城市名称"或"备注"列
                if (columnName.equals("城市名称") || columnName.equals("备注")) {
                    continue;
                }

                // 读取备注（第2行）
                String remark = "";
                if (remarkRow != null) {
                    Cell remarkCell = remarkRow.getCell(i);
                    if (remarkCell != null) {
                        remark = getCellValueAsString(remarkCell).trim();
                    }
                }

                // 解析列名（提取年份和统计项名称）
                ParsedColumnName parsed = parseColumnName(columnName);

                // 生成建议的字段代码
                String suggestedCode = generateItemCode(parsed.getItemName());

                ExcelColumnMapping mapping = ExcelColumnMapping.builder()
                        .originalColumnName(columnName)
                        .year(parsed.getYear())
                        .itemName(parsed.getItemName())
                        .suggestedItemCode(suggestedCode)
                        .remark(remark)
                        .skip(false)
                        .columnIndex(i)
                        .build();

                mappings.add(mapping);

                log.debug("列映射: {} -> {} (年份: {}, 备注: {})",
                        columnName, suggestedCode, parsed.getYear(), remark);
            }
        }

        log.info("生成{}个列映射", mappings.size());
        return mappings;
    }

    /**
     * 解析列名，提取年份和统计项名称
     */
    private ParsedColumnName parseColumnName(String columnName) {
        Integer year = null;
        String itemName = columnName;

        // 尝试提取年份
        Matcher matcher = YEAR_PATTERN.matcher(columnName);
        if (matcher.find()) {
            String yearStr = matcher.group(1);
            int extractedYear = Integer.parseInt(yearStr);

            // 验证年份合理性（2020-2030之间）
            if (extractedYear >= 2020 && extractedYear <= 2030) {
                year = extractedYear;
                // 从列名中移除年份部分
                itemName = columnName.replaceAll(yearStr + "年?", "").trim();
                // 清理括号、中括号等
                itemName = itemName.replaceAll("[\\(\\)\\[\\]【】]", "").trim();
            }
        }

        // 如果没有提取到年份，默认为2025年
        if (year == null) {
            year = 2025;
        }

        return new ParsedColumnName(year, itemName);
    }

    /**
     * 生成标准字段代码（拼音首字母缩写）
     */
    private String generateItemCode(String itemName) {
        // 简单处理：移除特殊字符，转小写，用下划线连接
        // 实际项目中可以使用拼音库（如pinyin4j）生成更标准的代码
        String code = itemName
                .replaceAll("[\\s　]+", "_")  // 空格替换为下划线
                .replaceAll("[^a-zA-Z0-9_\\u4e00-\\u9fa5]", "")  // 移除特殊字符
                .toLowerCase();

        // 如果是中文，使用拼音首字母（这里简化处理，实际可用pinyin4j）
        if (code.matches(".*[\\u4e00-\\u9fa5]+.*")) {
            // 简化：直接使用中文作为代码
            code = itemName.replaceAll("[\\s　]+", "_");
        }

        return code;
    }

    /**
     * 读取Excel数据行
     *
     * @param filePath Excel文件路径
     * @param mappings 列映射（已确认的）
     * @return 城市名称 -> 年份 -> 字段代码 -> 值
     */
    public Map<String, Map<Integer, Map<String, Object>>> readExcelData(
            String filePath, List<ExcelColumnMapping> mappings) throws IOException {

        log.info("开始读取Excel数据...");

        Map<String, Map<Integer, Map<String, Object>>> result = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getLastRowNum();

            log.info("Excel共有{}行数据（不含表头）", rowCount - 1);

            // 从第3行开始读取（第1行列名，第2行备注，第3行开始是数据）
            for (int rowIndex = 2; rowIndex <= rowCount; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                // 读取城市名称（第1列，索引0）
                Cell cityNameCell = row.getCell(0);
                if (cityNameCell == null) {
                    continue;
                }

                String cityName = getCellValueAsString(cityNameCell).trim();
                if (cityName.isEmpty() || cityName.equals("备注")) {
                    continue;
                }

                // 初始化该城市的数据结构
                result.putIfAbsent(cityName, new HashMap<>());

                // 遍历每个映射的列
                for (ExcelColumnMapping mapping : mappings) {
                    if (mapping.isSkip()) {
                        continue;
                    }

                    Cell dataCell = row.getCell(mapping.getColumnIndex());
                    if (dataCell == null) {
                        continue;
                    }

                    Object value = getCellValue(dataCell);

                    // 如果值为空或无效，跳过
                    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                        continue;
                    }

                    // 按年份组织数据
                    Integer year = mapping.getYear();
                    result.get(cityName).putIfAbsent(year, new HashMap<>());
                    result.get(cityName).get(year).put(mapping.getSuggestedItemCode(), value);
                }
            }
        }

        log.info("读取完成，共{}个城市的数据", result.size());
        return result;
    }

    /**
     * 获取单元格值（字符串形式）
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }

    /**
     * 获取单元格值（原始类型）
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                String strValue = cell.getStringCellValue().trim();
                return strValue.isEmpty() ? null : strValue;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return null;
        }
    }

    /**
     * 解析后的列名
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ParsedColumnName {
        private Integer year;
        private String itemName;
    }
}