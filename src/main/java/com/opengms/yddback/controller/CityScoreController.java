package com.opengms.yddback.controller;

import com.opengms.yddback.common.Result;
import com.opengms.yddback.common.ResultCode;
import com.opengms.yddback.dto.CityDimensionRankingDTO;
import com.opengms.yddback.dto.CityIndicatorScoresResponseDTO;
import com.opengms.yddback.dto.CityScoreWithDimensionsDTO;
import com.opengms.yddback.dto.CityTotalScoreDTO;
import com.opengms.yddback.service.CityScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * 城市得分相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
@Validated
@Tag(name = "城市得分", description = "城市得分查询相关接口")
public class CityScoreController {

    private final CityScoreService cityScoreService;

    /**
     * 查询某年所有城市的总分及排名
     *
     * @param year 年份
     * @return 城市总分排名列表
     */
    @GetMapping("/ranking/{year}")
    @Operation(summary = "查询城市总分排名", description = "查询指定年份所有城市的总分及排名")
    public Result<List<CityTotalScoreDTO>> getCityTotalScoreRanking(
            @PathVariable("year")
            @Parameter(description = "年份", example = "2025", required = true)
            @Min(value = 2020, message = "年份不能小于2020")
            @Max(value = 2030, message = "年份不能大于2030")
            Integer year) {

        log.info("接收到查询请求: 年份={}", year);

        try {
            List<CityTotalScoreDTO> ranking = cityScoreService.getCityTotalScoreRankingAndAutoCalculation(year);

            if (ranking.isEmpty()) {
                return Result.success("未找到数据", ranking);
            }

            return Result.success(ranking);

        } catch (Exception e) {
            log.error("查询城市总分排名失败: year={}", year, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询某年所有城市的总分及各维度得分
     *
     * @param year 年份
     * @return 城市得分列表（包含总分和各维度得分）
     */
    @GetMapping("/dimensions/{year}")
    @Operation(summary = "查询城市总分及各维度得分", description = "查询指定年份所有城市的总分及各维度得分")
    public Result<List<CityScoreWithDimensionsDTO>> getCityScoresWithDimensions(
            @PathVariable("year")
            @Parameter(description = "年份", example = "2025", required = true)
            @Min(value = 2020, message = "年份不能小于2020")
            @Max(value = 2030, message = "年份不能大于2030")
            Integer year) {

        log.info("接收到查询请求: 年份={}, 查询类型=总分及各维度得分", year);

        try {
            List<CityScoreWithDimensionsDTO> scores = cityScoreService.getCityScoresWithDimensionsAndAutoCalculation(year);

            if (scores.isEmpty()) {
                return Result.success("未找到数据", scores);
            }

            return Result.success(scores);

        } catch (Exception e) {
            log.error("查询城市总分及各维度得分失败: year={}", year, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询某年所有城市的二级指标归一化得分
     *
     * @param year 年份
     * @return 所有城市的指标归一化得分
     */
    @GetMapping("/indicators/{year}")
    @Operation(summary = "查询城市二级指标归一化得分", description = "查询指定年份所有城市的二级指标归一化得分")
    public Result<CityIndicatorScoresResponseDTO> getCityIndicatorScores(
            @PathVariable("year")
            @Parameter(description = "年份", example = "2025", required = true)
            @Min(value = 2020, message = "年份不能小于2020")
            @Max(value = 2030, message = "年份不能大于2030")
            Integer year) {

        log.info("接收到查询请求: 年份={}, 查询类型=二级指标归一化得分", year);

        try {
            CityIndicatorScoresResponseDTO scores = cityScoreService.getCityIndicatorScoresWithAutoCalculation(year);

            if (scores.getDimensionData().isEmpty()) {
                return Result.success("未找到数据", scores);
            }

            return Result.success(scores);

        } catch (Exception e) {
            log.error("查询城市二级指标归一化得分失败: year={}", year, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    /**
     * 查询某年所有城市某个维度的得分和排名
     * @param year 年份
     * @param dimension 维度，这里包括综合（总分）
     * @return 所有城市维度得分和排名
     */
    @GetMapping("/dimensions/ranking/{year}/{dimension}")
    @Operation(summary = "", description = "")
    public Result<List<CityDimensionRankingDTO>> getCityScoresWithDimensionsRanking(
            @PathVariable("year")
            @Parameter(description = "", example = "2025", required = true)
            @Min(value = 2020, message = "年份不能小于2020")
            @Max(value = 2030, message = "年份不能大于2030")
            Integer year,
            @PathVariable("dimension")
            @Parameter(description = "", example = "综合发展", required = true)
            String dimension){
        log.info("接受到查询请求：年份={}，查询内容={}维度排名", year, dimension);
        try {
            List<CityDimensionRankingDTO> cityDimensionRankingDTOList = cityScoreService.getCityDimensionRanking(year, dimension);
            if (cityDimensionRankingDTOList.isEmpty()) {
                return Result.success("未找到数据", cityDimensionRankingDTOList);
            }
            return Result.success(cityDimensionRankingDTOList);
        } catch (Exception e) {
            log.error("查询城市二级指标归一化得分失败: year={}", year, e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }
}