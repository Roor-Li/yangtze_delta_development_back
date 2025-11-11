package com.opengms.yddback.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 城市总分及各维度得分DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityScoreWithDimensionsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 城市名称
     */
    @JsonProperty("cityName")
    private String cityName;

    /**
     * 总分
     */
    @JsonProperty("score")
    private String score;

    /**
     * 各维度得分（动态字段）
     * 使用Map存储，key为维度名称，value为得分
     * 使用@JsonAnyGetter可以将Map中的键值对展开到JSON的顶层
     */
    private Map<String, String> dimensionScores;

    /**
     * 城市ID（可选，前端可能不需要，不会序列化到JSON）
     */
    private Long cityId;

    /**
     * 将dimensionScores展开到JSON顶层
     */
    @JsonAnyGetter
    public Map<String, String> getDimensionScores() {
        return dimensionScores == null ? new HashMap<>() : dimensionScores;
    }

    /**
     * 添加维度得分
     */
    public void addDimensionScore(String dimensionName, String score) {
        if (this.dimensionScores == null) {
            this.dimensionScores = new HashMap<>();
        }
        this.dimensionScores.put(dimensionName, score);
    }
}