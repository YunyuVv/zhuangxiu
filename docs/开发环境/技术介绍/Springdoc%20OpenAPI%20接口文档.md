# Springdoc OpenAPI 接口文档

> 适用项目：装修记录网站后端（`server/`）
> 依赖：`springdoc-openapi-starter-webmvc-ui:3.0.0`（适配 Spring Boot 4 / Spring 7）
> 访问地址：`/swagger-ui.html`

本文介绍 Springdoc 的作用、在本项目中的配置，以及 Native 编译下的注意事项。

---

## 一、Springdoc 是什么

**Springdoc OpenAPI** 是基于 Swagger/OpenAPI 规范的 Spring Boot 集成库。它能：

- 在**运行时根据 Controller 自动生成 OpenAPI 文档**，无需手写 YAML；
- 提供可视化的 **Swagger UI** 页面，方便前后端联调、测试接口；
- 暴露机器可读的 `api-docs` JSON（标准 OpenAPI 描述），可被前端代码生成器消费。

一句话：**写 Controller 即自动产出可交互的接口文档。**

---

## 二、在本项目中的配置

`server/src/main/resources/application.yml`：

```yaml
springdoc:
  api-docs:
    enabled: true
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

依赖（`server/pom.xml`）：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.0</version>
</dependency>
```

> 注意：**Spring Boot 4 必须搭配 springdoc `3.x`**。`2.x` 系列在 AOT 阶段会引用已被 Spring 7 移除的 `TypeInformation` 类而报错（详见《GraalVM Native 编译踩坑记录》问题 1）。

---

## 三、怎么用

1. **启动后端**（JVM 或 Native 均可）。
2. 浏览器打开 `http://localhost:9096/swagger-ui.html`（JVM 后端默认端口 9096；Native 用 9097 时对应改成 9097）。
3. 页面列出所有 Controller 的接口，可展开查看参数、直接 "Try it out" 发请求（需先通过 JWT 鉴权，见下文）。
4. 机器描述：`http://localhost:9096/v3/api-docs`。

### 与代码的关系

- Controller 方法上的 Spring 注解（`@GetMapping`、`@RequestParam`、`@RequestBody` 等）会被自动读取生成文档；
- DTO 上的 JSR-303 校验注解（`@NotBlank`、`@Size` 等）、OpenAPI 注解（`@Schema`、`@Operation`）会体现在文档里；
- **改了 Controller / DTO，文档自动更新**，无需手动维护。

---

## 四、鉴权联调

本项目接口受 JWT 保护。在 Swagger UI 中调试受保护接口前：

1. 先调用登录接口拿到 token；
2. 点页面右上角 **Authorize**，输入 `Bearer <token>`；
3. 之后所有请求会自动带上该 token。

（JWT 机制见同目录《JWT 认证与安全》。）

---

## 五、Native 编译下的注意事项

Springdoc 在 **GraalVM Native Image** 下曾导致启动失败，原因是它对内部 `SpringWebProvider` 创建了 **CGLIB 惰性代理**，而运行时生成的代理子类（`SpringWebProvider$$SpringCGLIB$$0`）缺少反射元数据，报 `MissingReflectionRegistrationError`。

修复（详见《GraalVM Native 编译踩坑记录》问题 6）：

- 在 `SpringDocNativeConfig.java` 中用 `@ImportRuntimeHints` + `RuntimeHintsRegistrar`，按类名（`TypeReference.of("...SpringWebProvider$$SpringCGLIB$$0")`）登记生成的 CGLIB 代理子类的反射（补全 `DECLARED_FIELDS` 等），在 AOT 阶段直接合并进元数据。

> ⚠️ 注意：直接写 `META-INF/native-image/.../reachability-metadata.json` 来登记该代理子类**不可靠**——Spring 7 AOT 已自动登记它（仅构造器权限），聚合时不会与 JSON 里的字段权限合并，导致被"降配"丢失。必须用代码注册。
> 经验：Native 下 Swagger UI 打不开 / 应用启动报 CGLIB 反射错误，优先检查 `SpringDocNativeConfig` 的 `PROXY_CLASSES` 是否覆盖报错类名，并重跑 `mvn -Pnative clean package`。

---

## 六、小结

- Springdoc 让接口文档随代码自动生成，降低前后端沟通成本。
- 本项目开启 `swagger-ui`（`/swagger-ui.html`）与 `api-docs`。
- 版本必须 `3.x`（配 Spring Boot 4）；Native 下需额外处理 CGLIB 代理反射。
