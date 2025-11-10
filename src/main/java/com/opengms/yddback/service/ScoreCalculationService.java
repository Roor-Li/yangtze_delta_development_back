package com.opengms.yddback.service;

/**
 * 指标和维度得分计算服务接口
 */
public interface ScoreCalculationService {

    /**
     * 计算并保存某年的所有指标得分
     *
     * @param year 年份
     * @return 成功计算的城市数量
     */
    int calculateAndSaveIndicatorScores(Integer year);

    /**
     * 计算并保存某年的所有维度得分
     *
     * @param year 年份
     * @return 成功计算的城市数量
     */
    int calculateAndSaveDimensionScores(Integer year);

    /**
     * 计算并保存某年的完整得分（指标+维度）
     *
     * @param year 年份
     */
    void calculateAndSaveAllScores(Integer year);
}