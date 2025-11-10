package com.opengms.yddback.controller;

import com.opengms.yddback.common.Result;
import com.opengms.yddback.dto.CityGeometryDTO;
import com.opengms.yddback.service.CityGeometryDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/geometry")
@RequiredArgsConstructor
@Validated
@Tag(name = "城市地理数据", description = "城市地理数据查询相关接口")
public class CityGeometryController {
    private final CityGeometryDataService cityGeometryDataService;

    /**
     * 查询所有城市地理数据
     * @param geometryType 地理数据类型
     * @return 所有城市对应类型的地理数据
     */
    @GetMapping("{geometryType}")
    @Operation(summary = "查询地理数据", description = "查询所有城市中心点或边界数据")
    public Result<CityGeometryDTO> getAllCityCenterPoints(
            @PathVariable("geometryType")
            @Parameter(description = "地理数据类型", example = "center_point", required = true)
            String geometryType) {
        if (geometryType.equals("center_point")) {
            return Result.success(cityGeometryDataService.getAllCityCenterPoints());
        } else if (geometryType.equals("boundary")) {
            return Result.success(cityGeometryDataService.getAllCityBoundaries());
        } else {
//            log.error("输入地理类型错误");
            return Result.fail("输入类型错误");
        }
    }
}
