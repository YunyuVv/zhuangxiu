# GraalVM Native 编译踩坑记录

> 适用项目：装修记录网站后端（`server/`，Spring Boot 4.0.0 / Spring 7 / Spring Data 4.0）
> 编译目标：GraalVM `graalvm64-25`（native-image 25，macOS arm64）
> 记录日期：2026-08-18

本文汇总本地对 Spring Boot 4 后端做 **GraalVM Native Image** 编译时遇到的全部问题、根因与解决办法，以及必须注意的事项。

---

## 一、背景与编译命令

Native 编译依赖 `spring-boot-starter-parent` 提供的 `native` profile 与 `native-maven-plugin`（由 pluginManagement 引入，无需手动声明坐标即可用）。

**标准编译启动命令（在 `server/` 目录下）：**

```bash
# 进入 server 目录
cd server

# 关键：确保 JAVA_HOME 指向 GraalVM（见问题三）
export JAVA_HOME="$(jenv prefix graalvm64-25)"

# 编译原生可执行文件（跳过测试）
mvn -Pnative -DskipTests native:compile
```

编译产物位于 `server/target/zhuangxiu-server`（约 200MB 级）。

**启动原生可执行：**

```bash
# 用不同端口避开 JVM 后端（JVM 端默认 9096）
./target/zhuangxiu-server --server.port=9097
```

---

## 二、遇到的问题与解决方案

### 问题 1：AOT 预处理阶段 `ClassNotFoundException: TypeInformation`

**现象**
执行 `mvn -Pnative` 进入 `process-aot` 阶段时报错，提示找不到 `org.springframework.data.util.TypeInformation`。

**根因**
Spring Boot 4 / Spring 7 / Spring Data 4.0 已**移除** `TypeInformation` 类。项目原用的 `springdoc-openapi-starter-webmvc-ui` 为 `2.8.0`，其内部代码仍在引用该已被删除的类，与 Spring 7 不兼容。

**解决方案**
将 springdoc 升级到适配 Spring Boot 4 的 `3.0.0`：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.0</version>
</dependency>
```

> 注意：Spring Boot 4 必须搭配 springdoc `3.x`，`2.x` 系列不可用。

---

### 问题 2：`mvn -Pnative package` 只产出 jar，未生成原生可执行文件

**现象**
执行 `mvn -Pnative package` 后 `target/` 下只有 `*.jar`，没有原生可执行文件 `zhuangxiu-server`。

**根因**
`native` profile 默认只把 `add-reachability-metadata` 绑定到 `package` 阶段，`native:compile`（即生成原生镜像）目标并未绑定到任何生命周期阶段。仅靠 `package` 不会触发 `compile` 目标。

**解决方案**
在 `pom.xml` 中显式声明 `native-maven-plugin` 并把 `compile` 目标绑定到 `package` 阶段：

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>build-native</id>
            <goals>
                <goal>compile</goal>
            </goals>
            <phase>package</phase>
        </execution>
    </executions>
    <buildArgs>
        <buildArg>-H:+EnableAllSecurityServices</buildArg>
    </buildArgs>
</plugin>
```

配置后即可用：

```bash
mvn -Pnative -DskipTests package   # 先 package 出 jar（非 native）
# 或直接调用目标
mvn -Pnative -DskipTests native:compile
```

---

### 问题 3：`native-image is not installed ... JDK at ... is not a GraalVM`

**现象**
执行 `mvn -Pnative native:compile` 报错：`native-image is not installed` 或 `JDK at .../1.8.x is not a GraalVM`。

**根因**
`JAVA_HOME` 指向的是系统默认的旧 JDK（如 `jdk1.8`），而非 GraalVM。native-image 工具链只存在于 GraalVM 中。

**解决方案**
1. 编译/启动前手动切换 `JAVA_HOME` 到 GraalVM：

   ```bash
   export JAVA_HOME="$(jenv prefix graalvm64-25)"
   ```

2. 把 GraalVM 固定为项目级 Java 版本，避免每次手动 export：
   - 在 `server/.java-version` 中写入 `graalvm64-25`（`jenv local graalvm64-25` 写入）。
   - 这样 `jenv` 会在进入 `server/` 目录时自动切换 Java 版本。

---

### 问题 4：编译成功但启动报 Flyway `NoSuchMethodException`，实际是 TLS 证书不受信

**现象**
原生可执行编译成功（约 209MB），但启动时报类似：

```
java.lang.NoSuchMethodException: ...isFlywaySpecificVersionOf(java.sql.SQLException)
```

**根因（两层）**
1. **反射元数据缺失**：Flyway 在 Native Image 下通过反射调用内部异常类的方法 `isFlywaySpecificVersionOf(SQLException)`，而 GraalVM 在 AOT 阶段无法静态分析到该反射调用，导致运行时 `NoSuchMethodException`。
2. **真正的根因被掩盖**：底层其实是连接数据库时的 **TLS 证书不受信**（`FlywaySqlServerUntrustedCertificateSqlException`）。因为反射元数据缺失，`NoSuchMethodException` 把底层证书错误掩盖了。

**解决方案**
1. 新建 `FlywayNativeConfig.java`，为 Flyway 内部异常类注册反射元数据（方法级反射）：

   ```java
   // server/src/main/java/com/biliww/zhuangxiu/config/FlywayNativeConfig.java
   package com.biliww.zhuangxiu.config;

   import org.springframework.aot.hint.MemberCategory;
   import org.springframework.aot.hint.RuntimeHints;
   import org.springframework.aot.hint.RuntimeHintsRegistrar;
   import org.springframework.lang.Nullable;

   public class FlywayNativeConfig implements RuntimeHintsRegistrar {
       @Override
       public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
           hints.reflection().registerType(
               org.flywaydb.core.internal.database.sqlserver.FlywaySqlServerUntrustedCertificateSqlException.class,
               MemberCategory.INVOKE_DECLARED_METHODS,
               MemberCategory.DECLARED_FIELDS
           );
       }
   }
   ```

   并在配置类/主类上通过 `@ImportRuntimeHints(FlywayNativeConfig.class)` 启用（或按项目既有方式登记 `RuntimeHintsRegistrar`）。

2. 在 `pom.xml` 的 `native-maven-plugin` 中加入安全服务开关（见问题 5 修正后的参数），把安全提供者（SunEC 等）打进镜像，使 TLS 正常建立连接。

---

### 问题 5：`Unrecognized option '-H:+IncludeAllSecurityServices'`

**现象**
执行 `mvn -Pnative native:compile` 报：

```
Error: Unrecognized option '-H:+IncludeAllSecurityServices'
```

**根因**
`-H:+IncludeAllSecurityServices` 是 **旧版 GraalVM** 的选项名。在 **GraalVM 25** 中该选项已重命名为 `-H:+EnableAllSecurityServices`。

**解决方案**
将 `pom.xml` 中 `native-maven-plugin` 的 `buildArgs` 改为：

```xml
<buildArgs>
    <buildArg>-H:+EnableAllSecurityServices</buildArg>
</buildArgs>
```

---

### 问题 6：启动报 `MissingReflectionRegistrationError`（`SpringWebProvider$$SpringCGLIB$$0` 的 `CGLIB$FACTORY_DATA` 字段）

**现象**
`mvn -Pnative native:compile` 编译成功、能连上数据库并完成 Flyway 迁移，但 Bean 初始化阶段失败：

```
Caused by: org.springframework.aop.framework.AopConfigException: Unexpected AOP exception
Caused by: java.lang.IllegalStateException: Unable to load cache item
Caused by: org.graalvm.nativeimage.MissingReflectionRegistrationError:
  Cannot reflectively read or write field 'public static java.lang.Object
  org.springdoc.core.providers.SpringWebProvider$$SpringCGLIB$$0.CGLIB$FACTORY_DATA'.
  ... add the following to the 'reflection' section of 'reachability-metadata.json' ...
```

**根因**
springdoc 3.x 在初始化 `swaggerWelcome` 等 Bean 时，通过 `ContextAnnotationAutowireCandidateResolver` 对 `SpringWebProvider` 等 Provider 创建 **CGLIB 惰性代理**。Native 镜像默认不为这些**运行时动态生成**的代理子类（如 `SpringWebProvider$$SpringCGLIB$$0`）生成反射元数据，于是访问其 `CGLIB$FACTORY_DATA` 字段时报 `MissingReflectionRegistrationError`，上层表现为 `AopConfigException -> BeanCreationException`，应用启动失败。

> 注意：Spring 7 已从公共 API 移除 `ProxyHints.registerCglibProxy(...)`，CGLIB 代理改由框架在 AOT 阶段自动生成；但 springdoc 通过 lambda 定义的 Bean 其惰性依赖未被 AOT 捕获，故运行时生成的代理子类缺少元数据。

**解决方案（代码注册，不要用 reachability-metadata.json）**
> ⚠️ 教训：最初尝试把代理子类登记写进 `META-INF/native-image/<group>/<artifact>/reachability-metadata.json`，但 Spring 7 的 AOT **已经自动登记了这些代理类（仅构造器权限）**，而 `add-reachability-metadata` 聚合时**不会把 JSON 里的 `allDeclaredFields` 与已有条目合并**，导致字段权限被"降配"丢失，报错依旧。所以该路径不可靠。

正确做法：在 AOT 阶段用 `RuntimeHints` **代码注册**（直接合并进生成的元数据）：用 `TypeReference.of("...SpringWebProvider$$SpringCGLIB$$0")` 按类名登记生成的代理子类，并补全 `DECLARED_FIELDS` / 构造器 / 方法权限。参见 `server/src/main/java/com/biliww/zhuangxiu/config/SpringDocNativeConfig.java`（`@ImportRuntimeHints` + `RuntimeHintsRegistrar`）：

   ```java
   for (String proxy : PROXY_CLASSES) {           // 含 SpringWebProvider$$SpringCGLIB$$0 等
       hints.reflection().registerType(TypeReference.of(proxy),
               MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
               MemberCategory.INVOKE_DECLARED_METHODS,
               MemberCategory.INVOKE_PUBLIC_METHODS,
               MemberCategory.DECLARED_FIELDS,
               MemberCategory.PUBLIC_FIELDS);
   }
   ```

> 若重跑后日志出现**其他** `$$SpringCGLIB$$N` 类的同类错误，在 `PROXY_CLASSES` 数组里追加该全限定名即可（后缀 `N` 为代理计数，通常从 `0` 起）。目标类（如 `SpringWebProvider`）本身也需反射权限，已一并处理。

---

### 问题 7：启动成功，但带 token 的请求 500——jjwt 运行期反射加载 `StandardEncryptionAlgorithms` 失败

**现象**
`mvn -Pnative clean package` 编译成功，应用也正常 `Started ZhuangxiuApplication`、连库与 Flyway 迁移均通过。但一旦前端发来**带 JWT 的请求**（如登录后访问接口），`JwtAuthenticationFilter` 解析 token 时抛异常，接口 500：

```
io.jsonwebtoken.lang.UnknownClassException: Unable to load class named
  [io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms] from the thread context,
  current, or system/application ClassLoaders. ... Have you remembered to include the jjwt-impl.jar?
  at io.jsonwebtoken.lang.Classes.forName(Classes.java:90)
  at io.jsonwebtoken.Jwts$ENC.<clinit>(Jwts.java:82)        // 首次触发 Jwts$ENC 静态初始化
  at io.jsonwebtoken.impl.DefaultJwtParserBuilder.<init>     // 构建 JWT 解析器时引用 Jwts.ENC
  at com.biliww.zhuangxiu.security.JwtProvider.parseUserId(JwtProvider.java:40)
  at com.biliww.zhuangxiu.security.JwtAuthenticationFilter.doFilterInternal(...)
```

后续请求复用失败状态，变为 `NoClassDefFoundError: Could not initialize class io.jsonwebtoken.Jwts$ENC`。

**根因**
jjwt 0.12.x 在构建 JWT 解析器时会触发内部枚举 `Jwts$ENC` 的静态初始化，进而通过 `io.jsonwebtoken.lang.Classes.forName("io.jsonwebtoken.impl.security.StandardEncryptionAlgorithms")` 以**字符串名反射加载**加密算法聚合类。Native 镜像的静态可达性分析**发现不了**这种"运行期按需反射"，于是该类未被打进镜像，`forName` 抛 `UnknownClassException`。

> 即便本接口只用 JWS（签名校验）、不用 JWE（加密），`DefaultJwtParserBuilder` 在构造时也会引用 `Jwts.ENC`，从而触发该静态初始化——这是 jjwt 内部行为，躲不开。

**解决方案（扫描 jar 全量注册 jjwt 反射，一次到位）**
`jjwt-impl` / `jjwt-jackson` jar **自带没有任何 Native 元数据**，必须自己注册。新建 `server/src/main/java/com/biliww/zhuangxiu/config/JwtNativeConfig.java`（`@ImportRuntimeHints` + `RuntimeHintsRegistrar`），两层处理：

1. **兜底注册** 5 个 `Standard*Algorithms` 聚合类（`forName` 的直接目标，必须进镜像）：
   `StandardSignatureAlgorithms` / `StandardEncryptionAlgorithms` / `StandardDecryptionAlgorithms` / `StandardKeyAlgorithms` / `StandardSecureDigestAlgorithms`。
2. **扫描 jar**：在 AOT 阶段遍历 `java.class.path` 上的 `jjwt-impl` / `jjwt-jackson` / `jjwt-api` jar，把 `io.jsonwebtoken` 包下**所有**类登记反射（覆盖聚合类内部 `new` 出来的具体算法实现类，如各种 AES/GCM/HMAC 算法），避免后续再因某个具体算法类反射失败而反复重编。

反射权限用 `MemberCategory.values()` 全量登记（Spring 7 的 `MemberCategory` 已拆分为 `INVOKE_/INTROSPECT_/ACCESS_` 前缀，旧名的 `DECLARED_METHODS` 等已不存在，直接用 `values()` 最稳妥）。

```java
for (String name : STANDARD_AGGREGATES) {            // 5 个聚合类
    hints.reflection().registerType(TypeReference.of(name), MemberCategory.values());
}
// 再扫描 jjwt jar，注册 io.jsonwebtoken 下所有类
```

> 注意：扫描依赖 `java.class.path` 在 `process-aot` 阶段包含 jjwt 依赖 jar（jjwt-impl/jjwt-jackson 为 compile scope，必在其中）。即便扫描未命中，第 1 层仍保证报错的 `StandardEncryptionAlgorithms` 被注册，应用能过这一关。

---

## 三、当前状态与待验证

- ✅ 问题 1~6 已解决并**重新编译验证通过**：`mvn -Pnative clean package` 产出的原生可执行已能 `Started ZhuangxiuApplication`、连库、完成 Flyway 迁移，springdoc CGLIB 代理反射正常。
- ✅ 问题 7 的修复（`JwtNativeConfig` 扫描 jjwt jar 全量注册反射）**已写入代码并通过 JVM 编译，尚未重新走 Native 编译验证**。
- ⏳ 待用户重新编译并验证（重点测"带 JWT 的请求"）：

  ```bash
  export JAVA_HOME="$(jenv prefix graalvm64-25)"
  cd server
  # clean 确保 process-aot 重新生成元数据（之前 target/classes 可能有旧条目）
  mvn -Pnative -DskipTests clean package
  ./target/zhuangxiu-server --server.port=9097
  ```

  > 验证要点：用前端/Postman 发一个**带有效 token** 的请求（如登录后访问任意受保护接口），不再出现 `UnknownClassException` / `NoClassDefFoundError: Jwts$ENC`，接口正常返回即表示 JWT 解析在 Native 下恢复。

- ⏳ 验证通过后，需把以下未提交改动一并 commit：
  - `pom.xml`（springdoc 3.0.0、native-maven-plugin 绑定 + `buildArgs`）
  - `server/src/main/java/com/biliww/zhuangxiu/config/FlywayNativeConfig.java`（新建）
  - `server/src/main/java/com/biliww/zhuangxiu/config/SpringDocNativeConfig.java`（新建）
  - `server/src/main/java/com/biliww/zhuangxiu/config/JwtNativeConfig.java`（新建）
  - `server/.java-version`（内容改为 `graalvm64-25`）

---

## 四、注意事项（务必遵守）

1. **Java 版本**：Native 编译必须用 GraalVM，且版本为 `graalvm64-25`。任何非 GraalVM 的 JDK（尤其 jdk1.8）都会导致 native-image 不可用。
2. **Spring Boot 4 生态版本**：springdoc 必须 `3.x`，不能用 `2.x`（否则 AOT 阶段 `TypeInformation` 报错）。
3. **TLS 连接数据库**：若后端连接云数据库走 TLS，必须加 `-H:+EnableAllSecurityServices`（新版）/ `-H:+IncludeAllSecurityServices`（旧版，已废弃），否则镜像内缺少安全提供者，连接会报证书类错误。
4. **反射配置是 Native 编译的核心坑**：凡是框架在运行时通过反射/动态代理访问的类，都需要在 AOT 阶段显式注册（`RuntimeHints` / `@RegisterReflectionForBinding` / `RuntimeHintsRegistrar`），否则编译成功但启动失败。Flyway（问题 4）、springdoc CGLIB 代理（问题 6）是典型案例。
5. **Spring 7 的 CGLIB 代理登记方式变了**：`ProxyHints.registerCglibProxy(...)` 已在 Spring 7 移除，框架改为 AOT 自动生成。第三方库（如 springdoc）通过 lambda 定义、其惰性依赖未被 AOT 捕获时，运行期生成的 `$$SpringCGLIB$$N` 代理子类需改用 GraalVM 的 `META-INF/native-image/<groupId>/<artifactId>/reachability-metadata.json` 登记反射元数据。
5. **端口冲突**：JVM 后端默认 `server.port=9096`；手动跑 Native 可执行时改用 `--server.port=9097` 避免冲突。
6. **密钥不进仓库**：`application-test.yml` 含真实 Neon/Upstash 密码，已被 `.gitignore` 忽略；仅保留脱敏模板 `application-test.yml.example`。
7. **首次验证前不要 commit native 相关改动**：等 Native 启动验证通过，确认配置无误后再提交，避免把半成品配置带进 git。
