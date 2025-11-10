package com.opengms.yddback.service.impl;

import com.opengms.yddback.dto.CityGeometryDTO;
import com.opengms.yddback.entity.CityGeometry;
import com.opengms.yddback.repository.CityGeometryRepository;
import com.opengms.yddback.service.CityGeometryDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityGeometryDataServiceImpl implements CityGeometryDataService {

    private final CityGeometryRepository cityGeometryRepository;

    @Override
    @Transactional(readOnly = true)
    public CityGeometryDTO getAllCityCenterPoints() {
        List<CityGeometry> cityGeometryList = cityGeometryRepository.findAllCenterPoints();
        if (cityGeometryList.isEmpty()) {
            log.warn("未找到城市中心点数据");
            return null;
        }

        return buildResponse(cityGeometryList, "center_point");
    }

    @Override
    @Transactional(readOnly = true)
    public CityGeometryDTO getAllCityBoundaries() {
        List<CityGeometry> cityGeometryList = cityGeometryRepository.findAllBoundaries();
        if (cityGeometryList.isEmpty()) {
            log.warn("未找到城市边界数据");
            return null;
        }

        return buildResponse(cityGeometryList, "boundary");
    }

    private CityGeometryDTO buildResponse(List<CityGeometry> cityGeometryList, String geometryType) {
        Map<String, Object> geometryData = new HashMap<>();
        geometryData.put("type", "FeatureCollection");
//        geometryData.put("features", cityGeometryList);
        List<Map<String, Object>> features = new ArrayList<>();
        for (CityGeometry cityGeometry : cityGeometryList) {
            Map<String, Object> feature = cityGeometry.getGeojsonData();
            features.add(feature);
        }
        geometryData.put("features", features);

        return new CityGeometryDTO(geometryType, geometryData);
    }
}
