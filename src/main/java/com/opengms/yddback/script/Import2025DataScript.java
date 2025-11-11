package com.opengms.yddback.script;

import com.opengms.yddback.service.Year2025DataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 25年数据导入脚本
 *
 * 使用方法：
 * 1. 修改下面的文件路径
 * 2. 直接运行该类的main方法
 * 3. 或使用命令: mvn spring-boot:run -Dspring-boot.run.main-class=com.example.cityreport.script.Import2025DataScript
 */
@Slf4j
//@SpringBootApplication
@ComponentScan(basePackages = "com.opengms.yddback")
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = "com.opengms.yddback.repository")
@EntityScan(basePackages = "com.opengms.yddback.entity")
public class Import2025DataScript implements CommandLineRunner {

    private final Year2025DataImportService importService;

    // ============ 配置区域：修改这里的路径 ============

    /**
     * Excel文件路径
     */
    private static final String EXCEL_FILE_PATH = "D:\\yangtze_delta_development\\yangtze_delta_development_back\\database\\data\\2025\\basic_data.xlsx";

    /**
     * 映射配置文件路径（已经人工审核修改后的）
     */
    private static final String MAPPING_FILE_PATH = "D:\\yangtze_delta_development\\yangtze_delta_development_back\\database\\data\\2025\\column_mapping.json";

    // ===============================================

//    public static void main(String[] args) {
//        SpringApplication.run(Import2025DataScript.class, args);
//    }

    @Override
    public void run(String... args) throws Exception {
        log.info("========== 开始导入25年基础数据 ==========");
        log.info("Excel文件: {}", EXCEL_FILE_PATH);
        log.info("映射文件: {}", MAPPING_FILE_PATH);

        try {
            // 执行导入
            importService.importDataWithMapping(EXCEL_FILE_PATH, MAPPING_FILE_PATH);

            log.info("========== 数据导入完成 ==========");

        } catch (Exception e) {
            log.error("数据导入失败", e);
            throw e;
        }
    }
}