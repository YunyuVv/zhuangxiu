# JWT 认证与安全

> 适用项目：装修记录网站后端（`server/`）
> 依赖：`jjwt-api` / `jjwt-impl` / `jjwt-jackson`（JWT 编解码）
> 认证框架：`spring-boot-starter-security` + 自定义 `JwtAuthenticationFilter`

本文介绍 JWT 的作用、在本项目中的认证流程，以及联调要点。

---

## 一、JWT 是什么

**JWT**（JSON Web Token）是一种无状态的令牌认证机制。登录成功后，服务端签发一个带签名的 token；之后客户端每次请求在请求头带上它，服务端校验签名即可确认身份，**无需在服务端保存会话**。

对比传统 Session：

| 维度 | Session | JWT |
| --- | --- | --- |
| 状态 | 服务端存会话 | 无状态（token 自包含信息） |
| 存储 | 服务端内存/Redis | 客户端（localStorage/Cookie） |
| 扩展性 | 多实例需共享会话 | 天然适合多实例/微服务 |
| 失效 | 删除会话即可 | 靠过期时间（或黑名单） |

本项目用 **JWT + Spring Security**，契合无状态、易扩展的架构。

---

## 二、认证流程（本项目）

```
1. 登录
   POST /api/auth/login  {username, password}
        │
        ▼
   校验密码 → 生成 JWT（含用户标识、过期时间，用密钥签名）
        │
        ▼
   返回 { token: "eyJ..." }

2. 后续请求
   GET /api/projects   Header: Authorization: Bearer eyJ...
        │
        ▼
   JwtAuthenticationFilter 校验签名/过期 → 设置 SecurityContext
        │
        ▼
   Controller 执行业务（可从上下文取当前用户）
```

- `JwtAuthenticationFilter`：拦截请求，解析并校验 token，成功则写入 `SecurityContext`（启动日志里能看到 `Filter 'jwtAuthenticationFilter' configured for use`）。
- 受保护的接口需携带合法 token，否则返回 401。

---

## 三、在本项目中的关键点

- **依赖**：`jjwt`（Java JWT）负责 token 的创建与解析；`spring-boot-starter-security` 提供过滤器链与鉴权框架。
- **无状态**：不依赖 Redis 存会话（Redis 另作缓存用途）。
- **密码安全**：用户表 `users.password_hash` 存的是哈希（非明文）；登录时比对哈希。
- **token 内容**：含用户标识与过期时间，服务端用密钥验签（密钥不进仓库，见安全约定）。

---

## 四、联调与测试

1. 调登录接口拿 token；
2. 在 Swagger UI（`/swagger-ui.html`）点 **Authorize**，填 `Bearer <token>`；
3. 受保护接口即可正常调用。

> 前端（Next.js）把 token 存于客户端，请求时统一加 `Authorization` 头。

---

## 五、安全约定（提醒）

- **密钥不进仓库**：JWT 签名密钥、数据库密码、Redis 密码等敏感信息放在 `application-test.yml`（已被 `.gitignore` 忽略），仓库仅保留脱敏模板 `application-test.yml.example`。
- **HTTPS / TLS**：连接云数据库走 `sslmode=require`（Native 下需 `-H:+EnableAllSecurityServices` 把安全提供者打进镜像，详见 Native 文档与踩坑记录）。
- **过期策略**：token 设合理过期时间；敏感操作仍按接口粒度鉴权。

---

## 六、小结

- JWT 提供无状态令牌认证：登录拿 token，后续请求带 `Authorization: Bearer <token>`。
- 本项目用 `jjwt` + `JwtAuthenticationFilter` + Spring Security 实现。
- 密钥与密码等敏感信息不进仓库；联调时 Swagger UI 的 Authorize 填 token 即可。
