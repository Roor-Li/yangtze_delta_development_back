package com.opengms.yddback.script;

import com.opengms.yddback.service.Year2025DataImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 生成列映射配置文件
 */
@Slf4j
//@SpringBootApplication
@ComponentScan(basePackages = "com.opengms.yddback")
@RequiredArgsConstructor
public class GenerateMappingScript implements CommandLineRunner {

    private final Year2025DataImportService importService;

    // ============ 修改这里 ============
    private static final String EXCEL_FILE = "D:\\yangtze_delta_development\\yangtze_delta_development_back\\database\\data\\2025\\basic_data.xlsx";
    private static final String OUTPUT_FILE = "D:\\yangtze_delta_development\\yangtze_delta_development_back\\database\\data\\2025\\column_mapping.json";
    // =================================

//    public static void main(String[] args) {
//        SpringApplication.run(GenerateMappingScript.class, args);
//    }

    @Override
    public void run(String... args) throws Exception {
        log.info("生成列映射配置文件...");
        importService.generateMappingFile(EXCEL_FILE, OUTPUT_FILE);
        log.info("完成！请检查文件: {}", OUTPUT_FILE);
    }
}