package com.opengms.yddback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 城市地理数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityGeometryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("geometryType")
    private String geometryType;

    /**
     * 数据内容
     */
    @JsonProperty("geometryData")
    private Map<String, Object> geometryData;

}
