package com.opengms.yddback.service.impl;

import com.opengms.yddback.dto.CityDataDTO;
import com.opengms.yddback.entity.BasicStatisticsData;
import com.opengms.yddback.entity.CityInfo;
import com.opengms.yddback.repository.BasicStatisticsDataRepository;
import com.opengms.yddback.repository.CityInfoRepository;
import com.opengms.yddback.service.CityBasicDataService;
import com.opengms.yddback.service.ScoreCalculationService;
import jakarta.persistence.Basic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


/**
 * 查询城市基础数据
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CityBasicDataServiceImpl implements CityBasicDataService {

    private final CityInfoRepository cityInfoRepository;
    private final BasicStatisticsDataRepository basicStatisticsDataRepository;

    @Override
    @Transactional(readOnly = true)
    public CityDataDTO getCityBasicDataByYear(String cityName, Integer year) {
        log.info("查询{}在{}年的所有基础数据",cityName,year);

        // 查找城市
        Optional<CityInfo> cityOpt = cityInfoRepository.findByCityName(cityName);
        if (cityOpt.isEmpty()) {
            log.warn("未找到城市{}", cityName);
            return null;
        }
        CityInfo cityInfo = cityOpt.get();

        // 根据城市id和年份查询基础数据
        Optional<BasicStatisticsData> basicStatisticsDataOpt = basicStatisticsDataRepository.findLatestByCityIdAndYear(cityInfo.getId(), year);
        if (basicStatisticsDataOpt.isEmpty()) {
            log.warn("未找到{}的{}年基础数据", cityName, year);
            return null;
        }
        BasicStatisticsData basicStatisticsData = basicStatisticsDataOpt.get();

        return CityDataDTO.builder()
                .year(year)
                .cityName(cityName)
                .cityId(cityInfo.getId().toString())
                .dataContent(basicStatisticsData.getDataContent())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityDataDTO> getCityBasicDataForThreeConsecutiveYears(String cityName, Integer endYear) {
        log.info("查询{}在{}年-{}年的所有基础数据",cityName, endYear-2, endYear);

//        查找城市
        Optional<CityInfo> cityOpt = cityInfoRepository.findByCityName(cityName);
        if (cityOpt.isEmpty()) {
            log.warn("未找到城市{}", cityName);
            return null;
        }
        CityInfo cityInfo = cityOpt.get();

//        根据城市id和年份查询数据
        List<CityDataDTO> basicDataDTOList = new ArrayList<>();

        for (int i = endYear; i >= endYear - 2; i--) {
            CityDataDTO CityDataDTO = getCityBasicDataByYear(cityName, i);
            if (CityDataDTO != null) {
                basicDataDTOList.add(CityDataDTO);
            }
        }

        if (basicDataDTOList.size() != 3) {
            log.warn("{}的基础数据缺失", cityName);
            return null;
        } else {
            return basicDataDTOList;
        }
    }

}
