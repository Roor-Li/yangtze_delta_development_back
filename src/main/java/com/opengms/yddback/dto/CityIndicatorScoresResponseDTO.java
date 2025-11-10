package com.opengms.yddback.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 所有城市指标归一化得分响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityIndicatorScoresResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 各维度的指标数据
     * key: 维度名称（如"创新发展"）
     * value: 该维度下的所有指标数据
     */
    private Map<String, DimensionIndicatorDataDTO> dimensionData;

    /**
     * 指标名称列表（附加信息）
     * key: 维度名称
     * value: 该维度下的所有指标名称列表
     */
    @JsonProperty("keyName")
    private Map<String, List<String>> keyName;

    /**
     * 将dimensionData展开到JSON顶层
     */
    @JsonAnyGetter
    public Map<String, DimensionIndicatorDataDTO> getDimensionData() {
        return dimensionData == null ? new HashMap<>() : dimensionData;
    }
}