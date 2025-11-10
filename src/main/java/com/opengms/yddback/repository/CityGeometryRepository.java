package com.opengms.yddback.repository;

import com.opengms.yddback.entity.CityGeometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 城市地理数据Repository
 */
@Repository
public interface CityGeometryRepository extends JpaRepository<CityGeometry, Long> {

    /**
     * 根据城市ID和几何类型查询
     */
    Optional<CityGeometry> findByCityIdAndGeometryType(Long cityId, String geometryType);

    /**
     * 根据城市ID查询所有几何数据
     */
    List<CityGeometry> findByCityId(Long cityId);

    /**
     * 查询所有边界数据
     */
    List<CityGeometry> findByGeometryType(String geometryType);

    /**
     * 批量查询城市的边界数据
     */
    @Query("SELECT cg FROM CityGeometry cg WHERE cg.cityId IN :cityIds AND cg.geometryType = :geometryType")
    List<CityGeometry> findByCityIdsAndGeometryType(
            @Param("cityIds") List<Long> cityIds,
            @Param("geometryType") String geometryType
    );

    /**
     * 查询所有中心点（用于地图初始化）
     */
    @Query("SELECT cg FROM CityGeometry cg WHERE cg.geometryType = 'center_point'")
    List<CityGeometry> findAllCenterPoints();

    /**
     * 查询所有边界（用于地图初始化）
     */
    @Query("SELECT cg FROM CityGeometry cg WHERE cg.geometryType = 'boundary'")
    List<CityGeometry> findAllBoundaries();

    /**
     * 检查城市是否有几何数据
     */
    boolean existsByCityId(Long cityId);
}
