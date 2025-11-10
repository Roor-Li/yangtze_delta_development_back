package com.opengms.yddback.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opengms.yddback.entity.CityInfo;
import com.opengms.yddback.entity.CityGeometry;
import com.opengms.yddback.repository.CityInfoRepository;
import com.opengms.yddback.repository.CityGeometryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeoJsonImporter {

    private final CityInfoRepository cityInfoRepository;
    private final CityGeometryRepository cityGeometryRepository;
    private final ObjectMapper objectMapper;

    /**
     * 导入中心点GeoJSON数据
     */
    public void importCenterPoints(String jsonFilePath) throws Exception {
        JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
        JsonNode features = rootNode.get("features");

        for (JsonNode feature : features) {
            JsonNode properties = feature.get("properties");

            // 1. 保存或获取城市信息
            String cityCode = properties.get("cityCODE").asText();
            CityInfo city = cityInfoRepository.findByCityCode(cityCode)
                    .orElseGet(() -> {
                        CityInfo newCity = new CityInfo();
                        newCity.setCityCode(cityCode);
                        newCity.setCityName(properties.get("name").asText());
                        newCity.setProvinceCode(properties.get("proCODE").asText());
                        newCity.setProvinceName(properties.get("省份").asText());
                        newCity.setObjectId(properties.get("OBJECTID").asInt());
                        newCity.setOrigFid(properties.get("ORIG_FID").asInt());
                        return cityInfoRepository.save(newCity);
                    });

            // 2. 保存中心点几何数据
            CityGeometry geometry = new CityGeometry();
            geometry.setCityId(city.getId());
            geometry.setGeometryType("center_point");

            // 将整个feature转为Map存储
            Map<String, Object> geoJsonMap = objectMapper.convertValue(feature, Map.class);
            geometry.setGeojsonData(geoJsonMap);

            cityGeometryRepository.save(geometry);
        }

        System.out.println("中心点数据导入完成！");
    }

    /**
     * 导入边界GeoJSON数据
     */
    public void importBoundaries(String jsonFilePath) throws Exception {
        JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));
        JsonNode features = rootNode.get("features");

        for (JsonNode feature : features) {
            JsonNode properties = feature.get("properties");
            String cityCode = properties.get("cityCODE").asText();

            // 查找对应的城市
            CityInfo city = cityInfoRepository.findByCityCode(cityCode)
                    .orElseThrow(() -> new RuntimeException("城市不存在: " + cityCode));

            // 保存边界几何数据
            CityGeometry geometry = new CityGeometry();
            geometry.setCityId(city.getId());
            geometry.setGeometryType("boundary");

            Map<String, Object> geoJsonMap = objectMapper.convertValue(feature, Map.class);
            geometry.setGeojsonData(geoJsonMap);

            cityGeometryRepository.save(geometry);
        }

        System.out.println("边界数据导入完成！");
    }
}
