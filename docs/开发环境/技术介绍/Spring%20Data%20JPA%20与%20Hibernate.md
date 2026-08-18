# Spring Data JPA 与 Hibernate

> 适用项目：装修记录网站后端（`server/`）
> 依赖：`spring-boot-starter-data-jpa`（底层 Hibernate ORM）
> 关键约定：`ddl-auto: none`，表结构由 Flyway 管理

本文介绍 JPA/Hibernate 的作用、在本项目中的配置，以及和 Flyway 的分工。

---

## 一、JPA / Hibernate 是什么

- **JPA**（Java Persistence API）是 Java 的 ORM 规范；**Hibernate** 是其最主流的实现。
- **作用**：把 Java 对象（实体类 `@Entity`）和数据库表做映射，开发者用面向对象的方式（Repository 方法、JPQL）操作数据，而不手写 SQL。
- **Spring Data JPA** 在 JPA 之上再封装一层：定义接口 `XxxRepository extends JpaRepository<T, ID>` 就能获得增删改查，无需实现类。

一句话：**用 Java 类描述表，用方法名/注解描述查询，Hibernate 负责生成并执行 SQL。**

---

## 二、在本项目中的配置

`server/src/main/resources/application.yml`：

```yaml
jpa:
  hibernate:
    ddl-auto: none          # 表结构完全交给 Flyway，Hibernate 不自动建/改表
  open-in-view: false       # 不在视图渲染阶段保持 JPA Session（避免懒加载泄漏）
  properties:
    hibernate:
      format_sql: true      # 控制台打印格式化 SQL，便于调试
```

依赖（`server/pom.xml`）：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>   <!-- PostgreSQL JDBC 驱动 -->
</dependency>
```

---

## 三、实体与表的关系（本项目）

表结构由 `V1__init.sql`（Flyway）定义，JPA 实体与之**映射对应**：

| 表（Flyway 建） | 实体（JPA） | 说明 |
| --- | --- | --- |
| `users` | `User` | 用户，含 `password_hash`、`nickname` |
| `projects` | `Project` | 装修项目（一套房一个项目） |
| `phases` | `Phase` | 阶段（标准 8 工序），关联 `project_id` |
| `expenses` | `Expense` | 花销，关联 `project_id`/`phase_id` |
| `attachments` | `Attachment` | 图片（imgbb URL），多态关联 `ref_type/ref_id` |

约定：

- 实体类用 `@Entity`、`@Table` 映射；字段用 `@Id`、`@Column` 等注解；
- 关联关系（`@ManyToOne`/`@OneToMany`）对应表里的外键；
- **表结构以 Flyway 为准**，实体的注解（尤其是长度、类型）应与 `V1__init.sql` 保持一致，否则运行期可能出错。

---

## 四、Repository 用法

典型写法：

```java
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserId(Long userId);          // 方法名派生查询
    @Query("select p from Project p where p.status = :s")
    List<Project> findByStatus(@Param("s") String status);  // JPQL
}
```

- `findByXxx` 按命名约定自动生成查询；
- 复杂查询用 `@Query` 写 JPQL（面向实体，不是原生 SQL）；
- 分页/排序传 `Pageable`，返回 `Page<T>`。

---

## 五、JPA 与 Flyway 的分工（重要）

| 职责 | 工具 | 本项目设置 |
| --- | --- | --- |
| **建表 / 改表结构** | Flyway | `V1__init.sql` 等迁移脚本 |
| **对象↔表的映射与 CRUD** | Hibernate/JPA | `ddl-auto: none` |
| **启动时校验映射** | Hibernate | 仅校验实体与已存在表是否匹配 |

为什么 `ddl-auto: none` 而不是 `update`：

- `update` 让 Hibernate 自己偷偷改表，不可控、不可回滚、生产危险；
- 改结构应走 Flyway 的新增迁移脚本（可审查、可版本化）；
- 二者配合：**Flyway 管"长什么样"，JPA 管"怎么用"**。

> 顺序：应用启动时 **Flyway 先建/升级表**，之后 Hibernate 才做映射校验，最后 Repository 可用。

---

## 六、Native 编译注意

- 实体类、DTO 需要在 Native 下可被反射（JSON 序列化 / JPA 实例化）。本项目通过 `@RegisterReflectionForBinding` 等为 DTO 注册反射，实体由 Spring Boot 自动处理。
- `ddl-auto: none` 在 Native 下同样适用，不依赖 Hibernate 的运行时元数据生成。

---

## 七、小结

- JPA/Hibernate 让数据库操作面向对象化；Spring Data JPA 进一步简化 Repository。
- 本项目 `ddl-auto: none`：表结构交给 Flyway，JPA 只做映射与 CRUD。
- 改表结构加 Flyway 迁移脚本；改查询改 Repository/实体映射。
