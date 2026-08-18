# GraalVM Native Image 简介与适配

> 适用项目：装修记录网站后端（`server/`）
> 编译目标：GraalVM `graalvm64-25`（native-image 25，macOS arm64）
> 排查专文：`开发环境中遇到的问题/GraalVM Native 编译踩坑记录.md`

本文介绍 Native Image 是什么、相比 JVM 的异同，以及本项目为适配它所做的配置。

---

## 一、Native Image 是什么

**GraalVM Native Image** 是一种 AOT（Ahead-Of-Time，运行前编译）技术：它把 Java 应用连同运行时（Substrate VM）在**构建期**编译成一个**独立的原生可执行文件**（如本项目的 `target/zhuangxiu-server`）。

对比 JVM 模式：

| 维度 | JVM 模式 | Native Image |
| --- | --- | --- |
| 产物 | `.jar` + JRE | 单个原生可执行文件 |
| 启动速度 | 较慢（需 JVM 预热） | 极快（毫秒~秒级） |
| 内存占用 | 较高 | 显著更低 |
| 构建时间 | 快（仅打包） | 慢（需 native-image 全量编译，分钟级） |
| 反射/动态代理 | 天然支持 | 需**显式登记**元数据 |
| 部署 | 需装 JRE | 可直接拷贝运行，适合容器/Serverless |

一句话：**牺牲构建速度与动态性，换取启动快、体积小、内存低**，特别适合容器和云原生部署。

---

## 二、本项目为什么关心它

- 后端用 Spring Boot 4，官方一等公民支持 Native；
- 容器化部署（见 `docker-compose.yml`）时，原生镜像启动快、镜像小；
- 属于"可选优化"：JVM 模式直接 `mvn spring-boot:run` 就能跑，原生镜像是进阶玩法。

---

## 三、构建命令

```bash
# 必须：JAVA_HOME 指向 GraalVM（项目 .java-version 已固定为 graalvm64-25）
export JAVA_HOME="$(jenv prefix graalvm64-25)"
cd server

# 编译原生可执行
mvn -Pnative -DskipTests native:compile

# 运行（用 9097 避开 JVM 后端的 9096）
./target/zhuangxiu-server --server.port=9097
```

产物位置：`server/target/zhuangxiu-server`（约 200MB 级）。

> 关键：改了任何 Native 相关配置（如新增 `reachability-metadata.json`、反射配置）后，**必须重跑 `native:compile`**，旧二进制不会自动更新。

---

## 四、AOT 与"反射元数据"为何是核心难点

JVM 下，反射、动态代理、序列化在运行时按需加载类，一切正常。但 Native Image 在**构建期**就决定了哪些类、方法、字段被打进镜像——凡是"运行时才发现要用"的东西（反射读字段、CGLIB 代理、JSON 序列化 DTO），**默认不会被包含**，于是运行期报 `MissingReflectionRegistrationError` 或 `NoSuchMethodException`。

因此 Native 适配的本质工作 = **把运行期需要的反射/代理/资源，在 AOT 阶段显式登记**。Spring Boot 能自动处理大部分（实体、配置类），但第三方库（如 Flyway、springdoc）的"隐藏"用法需要手动补：

| 问题点 | 登记方式 | 详见 |
| --- | --- | --- |
| Flyway 内部异常类反射 | `RuntimeHints`（`FlywayNativeConfig`） | 踩坑记录 问题 4 |
| TLS 安全提供者缺失 | `-H:+EnableAllSecurityServices` | 踩坑记录 问题 5 |
| springdoc CGLIB 代理 | `reachability-metadata.json` + `SpringDocNativeConfig` | 踩坑记录 问题 6 |
| DTO JSON 序列化 | `@RegisterReflectionForBinding` | 项目既有做法 |

---

## 五、Spring 7 的一个变化

Spring 7 已从公共 API 移除 `ProxyHints.registerCglibProxy(...)`，CGLIB 代理改由框架在 AOT 阶段**自动生成**。这意味着：

- 大多数情况下你不用手动登记 CGLIB 代理；
- 但**第三方库通过 lambda 定义的 Bean**（如 springdoc 的 `swaggerWelcome`），其惰性依赖未被 AOT 捕获，运行期生成的 `$$SpringCGLIB$$N` 代理子类仍需你用 **GraalVM 的 `reachability-metadata.json`** 手动登记（见上表 springdoc 行）。

---

## 六、调试建议

- 想看 Native 专属报错，启动后关注 `org.graalvm.nativeimage.MissingReflectionRegistrationError`；
- GraalVM 报错信息通常会**直接给出**该往 `reachability-metadata.json` 加什么，复制即可；
- 暴露真实根因：Native 下异常可能被上层掩盖（如 Flyway 反射错误掩盖 TLS 证书错误），看完整堆栈。

---

## 七、小结

- Native Image = 构建期编译成原生可执行，启动快、体积小、内存低，但需显式登记反射/代理。
- 本项目用 `graalvm64-25` + `-Pnative native:compile` 构建；JVM 模式仍是日常开发首选。
- 适配重点在"反射元数据"：Flyway、springdoc、TLS 三处需手动补（详见《GraalVM Native 编译踩坑记录》）。
