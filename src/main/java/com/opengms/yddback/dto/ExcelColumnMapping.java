package com.opengms.yddback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Excel列映射DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelColumnMapping {

    /**
     * 原始列名（从Excel读取）
     */
    private String originalColumnName;

    /**
     * 提取的年份（如果列名包含年份）
     */
    private Integer year;

    /**
     * 去除年份后的统计项名称
     */
    private String itemName;

    /**
     * 标准字段代码（建议值，可以修改）
     */
    private String suggestedItemCode;

    /**
     * 单位/备注（从Excel第2行读取）
     */
    private String remark;

    /**
     * 是否跳过该列
     */
    private boolean skip;

    /**
     * Excel列索引
     */
    private int columnIndex;
}