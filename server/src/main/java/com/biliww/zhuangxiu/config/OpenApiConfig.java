package com.biliww.zhuangxiu.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI 配置：自动生成接口文档（兼容 Native）。
 * 访问：/swagger-ui.html 与 /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("装修记录 API")
                .description("装修从 0 到完工 的周期 / 花销 / 资料记录后端接口")
                .version("v0.1"));
    }
}
