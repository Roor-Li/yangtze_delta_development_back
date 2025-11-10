package com.opengms.yddback.service;

import com.opengms.yddback.dto.CityDataDTO;

/**
 * 前端决策页面需要的数据服务
 */
public interface CityDecisionDataService {
    /**
     * 查询前端做某年决策模拟时所需要的所有数据
     * @param cityName 城市名称
     * @param year 年份
     * @return 返回对应年份对应城市所需要的决策数据
     */
    CityDataDTO getCityDecisionDataByYear(String cityName, Integer year);
}
