# Flyway 数据库版本管理介绍

> 适用项目：装修记录网站后端（`server/`）
> 数据库：PostgreSQL（Neon 云库，走 TLS `sslmode=require`）
> 相关依赖：`spring-boot-starter-flyway` + `flyway-database-postgresql`

本文介绍 Flyway 的作用、核心原理，以及它在本项目中的具体用法与注意事项。

---

## 一、Flyway 是什么

**Flyway** 是一款开源的**数据库迁移（migration）/版本管理**工具。它把数据库 schema 的演进（建表、加字段、改索引、初始数据等）写成带版本号的 SQL 脚本，并在应用启动时**自动、按顺序、且仅执行一次**地应用到数据库，从而保证：

- 每个环境（本地、测试、生产）的库结构一致；
- 数据库结构与代码版本对齐，可随代码一起做版本控制（Git）；
- 多人协作时，谁的脚本先合入就先执行，不会互相覆盖或漏执行。

一句话：**Flyway 让"建表 SQL"像代码一样被版本化管理，并由应用自动落地。**

---

## 二、为什么本项目需要它

本项目后端用 **Spring Data JPA + Hibernate**，但 `application.yml` 中特意关闭了 Hibernate 自动建表：

```yaml
jpa:
  hibernate:
    ddl-auto: none        # 表结构完全交给 Flyway 管理
```

原因：

1. **可控、可审计**：`ddl-auto: update` 会让 Hibernate 偷偷改表结构，不可控、不可回滚、生产环境危险。交给 Flyway 后，每一次结构变更都是一条明确的、可审查的 SQL。
2. **多环境一致**：本地、测试（Neon）、未来的生产库，都用同一套迁移脚本初始化，结构天然一致。
3. **与代码同源**：迁移脚本放在 `src/main/resources/db/migration`，随代码提交，部署即生效。

---

## 三、核心概念

| 概念 | 说明 |
| --- | --- |
| **Migration（迁移）** | 一个带版本号的变更脚本，通常是 `.sql` 文件，也可写 Java 类。 |
| **Version（版本号）** | 脚本名里的数字，决定执行顺序，如 `V1`、`V2`。 |
| **`flyway_schema_history`** | Flyway 在目标库自动建的一张元数据表，记录"已执行过哪些迁移、校验和、执行时间"。**这是 Flyway 判断"哪些脚本还没跑过"的依据。** |
| **Checksum（校验和）** | 每个脚本内容的哈希。若已执行的脚本内容被改动，校验和不匹配，Flyway 会报错（防止偷偷改历史）。 |
| **Baseline（基线）** | 对"已经存在旧表结构"的库，设定一个基线版本，低于基线的脚本不再执行。 |

---

## 四、Flyway 的工作流程（应用启动时）

每次后端启动，Flyway 大致做这几步：

1. **找脚本**：扫描配置的 `locations`（本项目 `classpath:db/migration`）。
2. **建/读历史表**：若 `flyway_schema_history` 不存在则创建它。
3. **比对版本**：把脚本版本与历史表已记录版本对比，找出"未执行"的脚本。
4. **按顺序执行**：从最小未执行版本开始，逐条执行 SQL，并在历史表插入一条记录（含校验和）。
5. **完成**：全部执行完，应用继续初始化（JPA/Hibernate 此时只做映射校验，不再改表）。

> 关键点：**已执行过的脚本永远不会再执行。** 想改表结构，只能新增一个更高版本的脚本（`V2__xxx.sql`），不能去改 `V1`。

---

## 五、本项目的实际配置

`server/src/main/resources/application.yml`：

```yaml
flyway:
  enabled: true
  locations: classpath:db/migration     # 迁移脚本目录
  baseline-on-migrate: true             # 首次连到"已有表"的库时自动打基线
  baseline-version: 0                   # 基线版本号
```

依赖（`server/pom.xml`）：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>   <!-- PostgreSQL 方言支持 -->
</dependency>
```

当前迁移脚本：`server/src/main/resources/db/migration/V1__init.sql`
- 一次性建好核心表：`users`、`projects`、`phases`、`expenses`、`attachments`；
- 含外键、索引、默认值；金额统一 `NUMERIC(14,2)`，阶段状态用三态枚举字符串。

---

## 六、迁移脚本命名规范

Flyway 靠文件名识别版本与类型，**命名必须规范**：

```
V{版本}__{描述}.sql          # 例：V1__init.sql、V2__add_project_cover.sql
```

- `V` 开头 = 版本化迁移（只会执行一次）；
- 版本号与描述之间是**双下划线** `__`；
- 描述用下划线连单词，见名知意。

常用约定：
- 加字段：`V2__add_project_cover.sql` → `ALTER TABLE projects ADD COLUMN cover_url TEXT;`
- 加索引：`V3__add_expense_category_idx.sql`
- 初始数据：`V4__seed_admin_user.sql`（插入必需的基础数据）

> 切勿修改已执行过的 `V1__init.sql`。要改动结构，新增 `V2`、`V3`……

---

## 七、常见操作

| 场景 | 做法 |
| --- | --- |
| 首次初始化库 | 直接启动后端，Flyway 自动建表（`baseline-on-migrate: true` 兼容已有库）。 |
| 新增表/字段 | 在 `db/migration` 新增 `V{n+1}__xxx.sql`，重启后端即自动执行。 |
| 本地想重来 | 删库或清空 `flyway_schema_history` 后再启动（注意会重跑全部脚本）。 |
| 多人协作冲突 | 各自加自己的 `V{n}`，版本号不重复；合并后按版本顺序执行。 |

---

## 八、本项目踩过的坑（与 Native 编译相关）

Flyway 在 **GraalVM Native Image** 下曾导致启动失败，根因是：

- Flyway 内部通过反射调用异常类的方法（`isFlywaySpecificVersionOf(SQLException)`）来判断异常类型；
- Native 镜像默认不为这些**内部异常类**生成反射元数据，导致 `NoSuchMethodException`；
- 该错误一度**掩盖了真正的问题**——连接 Neon 库时的 **TLS 证书不受信**（`FlywaySqlServerUntrustedCertificateSqlException`）。

对应修复（详见《GraalVM Native 编译踩坑记录》）：

1. 新建 `SpringDocNativeConfig` 之外，还新建了 `FlywayNativeConfig.java`，用 `RuntimeHints` 为 Flyway 内部异常类注册反射元数据；
2. 在 `pom.xml` 的 `native-maven-plugin` 中加入 `-H:+EnableAllSecurityServices`，把 SunEC 等安全提供者打进镜像，使 TLS 正常建立。

> 经验：Native 下 Flyway 报奇怪的反射/`NoSuchMethodException`，先怀疑是**底层连接/TLS 错误被掩盖**，而不是 Flyway 本身的问题。

---

## 九、小结

- Flyway = 把数据库 schema 变更写成带版本号的 SQL，由应用启动时自动、幂等地执行。
- 本项目用 Flyway 完全接管建表（`ddl-auto: none`），脚本在 `db/migration`，从 `V1__init.sql` 起步。
- 改结构**永远新增更高版本脚本**，不修改历史脚本。
- 在 Native 镜像场景下，需额外处理 Flyway 的反射元数据与 TLS 安全服务，否则会启动失败。
