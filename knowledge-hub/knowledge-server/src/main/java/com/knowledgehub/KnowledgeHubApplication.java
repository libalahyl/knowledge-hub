package com.knowledgehub;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动入口
 */
@SpringBootApplication(scanBasePackages = "com.knowledgehub")
@MapperScan(basePackages = "com.knowledgehub", annotationClass = Mapper.class)
public class KnowledgeHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnowledgeHubApplication.class, args);
    }
}
