package com.opengms.yddback.service.impl;

import com.opengms.yddback.dto.CityDataDTO;
import com.opengms.yddback.entity.GlobalParams;
import com.opengms.yddback.repository.GlobalParamsRepository;
import com.opengms.yddback.service.CityBasicDataService;
import com.opengms.yddback.service.CityDecisionDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityDecisionDataServiceImpl implements CityDecisionDataService {

    private final CityBasicDataService cityBasicDataService;
    private final GlobalParamsRepository globalParamsRepository;

    @Override
    public CityDataDTO getCityDecisionDataByYear(String cityName, Integer year) {
//        查询全局变量
        GlobalParams globalParams = new GlobalParams();
        Optional<GlobalParams> globalParamsOpt = globalParamsRepository.findByYear(year);
        if (globalParamsOpt.isPresent()) {
            globalParams = globalParamsOpt.get();
        } else {
            log.warn("未查询到{}年的全局变量", year);
            return null; // TODO: 可以考虑尝试计算
        }
//        查询连续3年的基础数据
        List<CityDataDTO> cityDataDTOList = cityBasicDataService.getCityBasicDataForThreeConsecutiveYears(cityName, year);
        if (cityDataDTOList == null || cityDataDTOList.size() < 3) {
            log.warn("数据不完整");
            return null;
        }

        CityDataDTO cityDataDTO = cityDataDTOList.get(0);

//        构建响应数据
        Map<String, Object> dataContent = cityDataDTO.getDataContent();

//        本科以上人口数
        BigDecimal bachelorAbove = (new BigDecimal(dataContent.get("bachelor_above").toString())).divide(new BigDecimal("10000"), 10, RoundingMode.HALF_UP);
        dataContent.replace("bachelor_above", bachelorAbove);

//        总人口
        BigDecimal population = (new BigDecimal(dataContent.get("population").toString())).divide(new BigDecimal("10000"), 10, RoundingMode.HALF_UP);
        dataContent.replace("population", population);

//        gdp增长率
        BigDecimal gdp = (new BigDecimal(dataContent.get("gdp").toString()));
        BigDecimal lastYearGdp = (new BigDecimal(cityDataDTOList.get(1).getDataContent().get("gdp").toString()));
        BigDecimal gdpGrowthRate = gdp.subtract(lastYearGdp).divide(lastYearGdp, 10, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        dataContent.put("gdp_growth_rate", gdpGrowthRate);

//        总体gdp增长率
        dataContent.put("total_gdp_growth_rate", globalParams.getTotalGdpGrowthRate());

//        BigDecimal highTechEnterprises = BigDecimal.ZERO;
//        for (CityDataDTO cityDataDTO1 : cityDataDTOList) {
//            highTechEnterprises.add(cityDataDTO1.getDataContent().get("high_tech_enterprises"));
//        }

//        上一年常驻人口
        BigDecimal lastYearPermanentPopulation = new BigDecimal(cityDataDTOList.get(1).getDataContent().get("permanent_population").toString());
        dataContent.put("last_year_permanent_population", lastYearPermanentPopulation);

//        连续3年高新技术产业
        BigDecimal highTechEnterprises = cityDataDTOList.stream()
                .map(dto -> dto.getDataContent().get("high_tech_enterprises"))
                .filter(Objects::nonNull)
                .map(value -> new BigDecimal(value.toString()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dataContent.replace("high_tech_enterprises", highTechEnterprises);

//        旅游总收入
        BigDecimal totalTourism = (new BigDecimal(dataContent.get("domestic_tourism").toString())).add(new BigDecimal(dataContent.get("international_tourism").toString()));
        dataContent.put("total_tourism", totalTourism);
        dataContent.remove("domestic_tourism");

//        中小学教师
        BigDecimal primarySecondaryTeachers = (new BigDecimal(dataContent.get("primary_teachers").toString()).add((new BigDecimal(dataContent.get("secondary_teachers").toString()))));
        dataContent.put("primary_secondary_teachers", primarySecondaryTeachers);
        dataContent.remove("primary_teachers");
        dataContent.remove("secondary_teachers");

//        中小学学生
        BigDecimal primarySecondaryStudents = (new BigDecimal(dataContent.get("primary_students").toString()).add((new BigDecimal(dataContent.get("secondary_students").toString()))));
        dataContent.put("primary_secondary_students", primarySecondaryStudents);
        dataContent.remove("primary_students");
        dataContent.remove("secondary_students");

        cityDataDTO.setDataContent(dataContent);

        return cityDataDTO;
    }
}
