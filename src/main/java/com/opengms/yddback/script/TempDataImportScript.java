package com.opengms.yddback.script;

import com.opengms.yddback.service.TempDataImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
//@SpringBootApplication
@ComponentScan(basePackages = "com.opengms.yddback")
public class TempDataImportScript {

//    public static void main(String[] args) {
//        SpringApplication.run(TempDataImportScript.class, args);
//    }

    @Bean
    public CommandLineRunner importData(TempDataImportService dataImportService) {
        return args -> {
            // 修改这里的路径
            String dataPath = "D:\\yangtze_delta_development\\yangtze_delta_development_back\\database\\data";
            log.info("开始执行数据导入脚本...");
            dataImportService.importPhase1Data(dataPath);
            log.info("数据导入完成");
        };
    }
}
