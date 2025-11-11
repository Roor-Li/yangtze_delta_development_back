package com.opengms.yddback.controller;

import com.opengms.yddback.common.Result;
import com.opengms.yddback.dto.CityDataDTO;
import com.opengms.yddback.service.CityBasicDataService;
import com.opengms.yddback.service.CityDecisionDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@Validated
@Tag(name = "城市基础数据", description = "城市基础数据查询相关接口")
public class CityDataController {

    private final CityBasicDataService cityBasicDataService;
    private final CityDecisionDataService cityDecisionDataService;
    /**
     * 查询某城市某年的全部基础数据
     * @param cityName 城市名称
     * @param year 年份
     * @return 指定城市指定年份全部基础数据
     */
    @GetMapping("/{cityName}/{year}")
    @Operation(summary = "查询城市基础数据", description = "查询指定年份指定城市的全部基础数据")
    public Result<CityDataDTO> getCityBasicData(
            @PathVariable("cityName")
            @Parameter(description = "城市名称", example = "上海市", required = true)
            String cityName,
            @PathVariable("year")
            @Parameter(description = "年份", example = "2025", required = true)
            @Min(value = 2020, message = "年份不能小于2020")
            @Max(value = 2030, message = "年份不能大于2030")
            Integer year) {

        log.info("开始查询{}的{}年基础数据", cityName, year);

        try {
            CityDataDTO basicData = cityBasicDataService.getCityBasicDataByYear(cityName, year);
            if (basicData == null) {
                return Result.success("未找到数据", null);
            }
            return Result.success(basicData);

        } catch (Exception e) {
            log.error("查询{}的{}年基础数据失败", cityName, year, e);
            return Result.fail("查询失败" + e.getMessage());
        }
    }

    /**
     * 查询某城市某年决策辅助所需要的所有数据
     * @param cityName 城市名称
     * @param year 年份
     * @return 指定城市指定年份决策辅助需要的数据
     */
    @GetMapping("/decision/{cityName}/{year}")
    @Operation(summary = "查询城市决策辅助初始", description = "查询")
    public Result<CityDataDTO> getCityDecisionData(
            @PathVariable("cityName")
            @Parameter(description = "城市名称", example = "上海市", required = true)
            String cityName,
            @PathVariable("year")
            @Parameter(description = "年份", example = "2025", required = true)
            @Min(value = 2020, message = "年份不能小于2020")
            @Max(value = 2030, message = "年份不能大于2030")
            Integer year
    ) {
        log.info("开始查询{}的{}年决策初始数据", cityName, year);
        try {
            CityDataDTO decisionData = cityDecisionDataService.getCityDecisionDataByYear(cityName, year);
            if (decisionData == null) {
                return Result.success("未找到数据",null);
            }
            return Result.success(decisionData);
        } catch (Exception e) {
            log.error("查询{}的{}年基础数据失败", cityName, year, e);
            return Result.fail("查询失败" + e.getMessage());
        }
    }


}
