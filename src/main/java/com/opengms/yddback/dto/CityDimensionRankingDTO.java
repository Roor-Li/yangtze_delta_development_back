package com.opengms.yddback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 城市维度排名（包含总分（综合维度））
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDimensionRankingDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 排名
     */
    @JsonProperty("rank")
    private Integer rank;

    /**
     * 城市名称
     */
    @JsonProperty("city")
    private String city;

    /**
     * 总分
     */
    @JsonProperty("score")
    private BigDecimal score;  // 使用String类型以保持精度显示

}
