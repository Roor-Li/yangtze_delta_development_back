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
 * 维度指标数据DTO
 * 用于封装某个维度下的所有指标数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionIndicatorDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 城市名称列表
     */
    @JsonProperty("names")
    private List<String> names;

    /**
     * 各指标的值列表
     * key: 指标名称
     * value: 该指标的所有城市的值列表（与names列表顺序对应）
     */
    private Map<String, List<String>> indicatorValues;

    /**
     * 将indicatorValues展开到JSON顶层
     */
    @JsonAnyGetter
    public Map<String, List<String>> getIndicatorValues() {
        return indicatorValues == null ? new HashMap<>() : indicatorValues;
    }
}