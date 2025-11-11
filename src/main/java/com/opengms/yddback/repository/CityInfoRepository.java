package com.opengms.yddback.repository;

import com.opengms.yddback.entity.CityInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 城市基础信息Repository
 */
@Repository
public interface CityInfoRepository extends JpaRepository<CityInfo, Long> {

    /**
     * 根据城市编码查询
     */
    Optional<CityInfo> findByCityCode(String cityCode);

    /**
     * 根据城市名称查询
     */
    Optional<CityInfo> findByCityName(String cityName);

    /**
     * 根据省份名称查询所有城市
     */
    List<CityInfo> findByProvinceName(String provinceName);

    /**
     * 根据省份编码查询所有城市
     */
    List<CityInfo> findByProvinceCode(String provinceCode);

    /**
     * 检查城市编码是否存在
     */
    boolean existsByCityCode(String cityCode);

    /**
     * 查询所有省份列表（去重）
     */
    @Query("SELECT DISTINCT c.provinceName FROM CityInfo c ORDER BY c.provinceName")
    List<String> findAllProvinceNames();

    /**
     * 统计城市总数
     */
    @Query("SELECT COUNT(c) FROM CityInfo c")
    long countTotalCities();

    /**
     * 模糊查询城市名称
     */
    List<CityInfo> findByCityNameContaining(String keyword);
}