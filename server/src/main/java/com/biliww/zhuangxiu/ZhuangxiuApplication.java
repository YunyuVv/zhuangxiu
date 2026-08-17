package com.biliww.zhuangxiu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 装修记录后端启动类。
 * 全量兼容 GraalVM Native Image：所有 DTO 已加 @RegisterReflectionForBinding，
 * JPA 实体与 JSON 序列化在 Native 下由 Spring Boot 自动注册反射（ddl-auto=none + Flyway）。
 */
@SpringBootApplication
@EnableJpaAuditing
public class ZhuangxiuApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhuangxiuApplication.class, args);
    }
}
