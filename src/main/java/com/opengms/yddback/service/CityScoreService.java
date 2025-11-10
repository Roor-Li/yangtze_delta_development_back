package com.opengms.yddback.service;

import com.opengms.yddback.dto.CityDimensionRankingDTO;
import com.opengms.yddback.dto.CityIndicatorScoresResponseDTO;
import com.opengms.yddback.dto.CityScoreWithDimensionsDTO;
import com.opengms.yddback.dto.CityTotalScoreDTO;

import java.util.List;

/**
 * 城市得分服务接口
 */
public interface CityScoreService {

    /**
     * 查询某年所有城市的总分及排名
     *
     * @param year 年份
     * @return 城市总分排名列表
     */
    List<CityTotalScoreDTO> getCityTotalScoreRanking(Integer year);

    /**
     * 查询某年所有城市的总分及各维度得分
     *
     * @param year 年份
     * @return 城市得分列表（包含总分和各维度得分）
     */
    List<CityScoreWithDimensionsDTO> getCityScoresWithDimensions(Integer year);

    /**
     * 查询某年所有城市的二级指标归一化得分
     *
     * @param year 年份
     * @return 所有城市的指标归一化得分
     */
    CityIndicatorScoresResponseDTO getCityIndicatorScores(Integer year);

    // 新增：支持自动计算的方法
    List<CityTotalScoreDTO> getCityTotalScoreRankingAndAutoCalculation(Integer year);

    List<CityScoreWithDimensionsDTO> getCityScoresWithDimensionsAndAutoCalculation(Integer year);

    CityIndicatorScoresResponseDTO getCityIndicatorScoresWithAutoCalculation(Integer year);

    /**
     * 查询某年所有城市对应维度排名和得分
     * @param year 年份
     * @param dimension 维度名称
     * @return 所有城市维度排名和得分
     */
    List<CityDimensionRankingDTO> getCityDimensionRanking(Integer year, String dimension);
}