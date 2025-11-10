package com.opengms.yddback.service;

import com.opengms.yddback.dto.CityGeometryDTO;

/**
 * 城市的地理数据接口
 */
public interface CityGeometryDataService {
    /**
     * 查询所有城市中心点数据
     */
    CityGeometryDTO getAllCityCenterPoints();

    /**
     * 查询所有城市边界数据
     */
    CityGeometryDTO getAllCityBoundaries();
}
