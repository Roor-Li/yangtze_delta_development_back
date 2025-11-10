package com.opengms.yddback;

import com.opengms.yddback.repository.CityInfoRepository;
import com.opengms.yddback.repository.DimensionDefinitionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private CityInfoRepository cityInfoRepository;

    @Autowired
    private DimensionDefinitionRepository dimensionRepository;

    @Test
    public void testDatabaseConnection() {
        // 测试能否连接数据库
        assertNotNull(cityInfoRepository);

        // 测试能否查询初始数据
        long dimensionCount = dimensionRepository.count();
        assertEquals(5, dimensionCount, "应该有5个维度定义");

        System.out.println("数据库连接测试成功！");
    }
}
