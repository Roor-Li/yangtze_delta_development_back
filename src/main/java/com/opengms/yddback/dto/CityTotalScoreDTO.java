package com.opengms.yddback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 城市总分排名DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityTotalScoreDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 排名
     */
    @JsonProperty("ranking")
    private Integer ranking;

    /**
     * 城市名称
     */
    @JsonProperty("cityName")
    private String cityName;

    /**
     * 总分
     */
    @JsonProperty("score")
    private String score;  // 使用String类型以保持精度显示

    /**
     * 城市ID（可选，前端可能不需要）
     */
    @JsonProperty("cityId")
    private Long cityId;
}
