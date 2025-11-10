package com.opengms.yddback.service;


import com.opengms.yddback.dto.CityDataDTO;

import java.util.List;

/**
 * 城市基础数据接口
 */
public interface CityBasicDataService {
    /**
     * 查询某年某个城市的所有基础数据
     * @param cityName 城市名称
     * @param year 年份
     * @return 对应城市对应年份所有基础数据
     */
    CityDataDTO getCityBasicDataByYear(String cityName, Integer year);

    /**
     * 查询某个城市连续3年的所有基础数据。这里根据前端的需求进行了适配
     * @param cityName 城市名称
     * @param endYear 结束年份
     * @return 对应城市连续3年的基础数据
     */
    List<CityDataDTO> getCityBasicDataForThreeConsecutiveYears(String cityName, Integer endYear);
}
