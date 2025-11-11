package com.opengms.yddback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 城市基础数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("year")
    private Integer year;

    @JsonProperty("cityId")
    private String cityId;

    @JsonProperty("cityName")
    private String cityName;

    /**
     * 数据内容
     */
    @JsonProperty("dataContent")
    private Map<String, Object> dataContent;

}
