# 装修记录网站 · Linux 上 Docker 部署指南

本文给出在 Linux 服务器（CentOS / Ubuntu / Debian 等）上，用 Docker 把「装修记录网站」整套跑起来的具体操作步骤。部署架构背景见同目录 [1-部署架构与方式介绍.md](./1-部署架构与方式介绍.md)。

> 2026-11-09 核查补充：Nginx 域名分流和 JVM/Native profile 已在本地修正，指南 SSR 容器地址、图床变量映射及实际镜像运行仍需处理。首次部署请先按 [首次部署核对与执行清单](3-首次部署核对与执行清单.md) 检查；本文原有步骤不能作为现版本已可直接部署的证明。当前指南含 SSR，外部前端托管也需要支持对应运行能力，不能直接等同于纯静态托管。

> 镜像由 GitHub Actions 自动构建并推送到 `ghcr.io`。如果你尚未拥有自己的镜像，请先按「第 0 步」让 CI 为你构建，或直接使用公开示例镜像（仅适合演示，密钥需自行覆盖）。

## 第 0 步（可选）：让 GitHub Actions 为你构建镜像

1. 把本仓库推送到你自己的 GitHub（Fork 或新建仓库）。
2. 确保仓库的 **Settings → Actions → General → Workflow permissions** 设为 `Read and write permissions`（用于推送镜像到 GHCR）。
3. 推送代码到 `main` 分支，或到 **Actions** 页手动 `Run workflow`，触发三个构建工作流。
4. 构建完成后，镜像会出现在 `Packages` 中，地址形如：
   - `ghcr.io/<你的GitHub用户名>/zhuangxiu-server-native:latest`
   - `ghcr.io/<你的GitHub用户名>/zhuangxiu-server:latest`
   - `ghcr.io/<你的GitHub用户名>/zhuangxiu-web:latest`

> 也可完全跳过 CI，直接在服务器上 `docker build -f docker/backend/Dockerfile.native ...` 本地构建，但首次 Native 构建较慢，推荐用 CI。

## 第 1 步：前置条件

```bash
# 1) 安装 Docker（以官方脚本为例，已装可跳过）
curl -fsSL https://get.docker.com | sudo sh
sudo systemctl enable --now docker

# 2) 安装 Docker Compose 插件（docker compose 子命令）
# 多数现代 Docker 已自带；验证：
docker compose version
```

要求：Docker ≥ 24，且支持 `docker compose`（注意是 compose v2，不是旧的 `docker-compose`）。

## 第 2 步：获取编排文件

方式一：直接 clone 整个仓库（含 `docker/` 目录）：

```bash
git clone <你的仓库地址> zhuangxiu
cd zhuangxiu
```

方式二：只取部署所需文件（在服务器上新建目录并放入 `docker/` 整个目录即可）：

```bash
mkdir -p /opt/zhuangxiu && cd /opt/zhuangxiu
# 把仓库里的 docker/ 目录整个拷到这里
```

## 第 3 步：准备环境变量

```bash
cd docker
cp .env.example .env
vim .env      # 按下面说明修改
```

`.env` 关键项（全部都有默认值，正式环境务必修改带密码/密钥的项）：

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `DB_NAME` | PostgreSQL 业务库名 | `zhuangxiu` |
| `DB_USERNAME` | 数据库用户 | `zhuangxiu` |
| `DB_PASSWORD` | 数据库密码 | `zhuangxiu`（**正式务必改**） |
| `REDIS_PASSWORD` | Redis 密码（默认空，与后端一致） | 空 |
| `ZHUANGXIU_SERVER_IMAGE` | 后端镜像；默认 Native 版，可改 JVM 版 | `ghcr.io/idea-flow/zhuangxiu-server-native:latest` |
| `APP_JWT_SECRET` | JWT 签名密钥，**必须 ≥32 字节** | 示例值（**务必改**） |
| `SPRING_PROFILES_ACTIVE` | 后端激活的 profile | `prod-docker` |
| `SERVER_PORT` | 仅 `docker-compose-server.yml` 模式对外暴露的端口 | `9096` |
| `ZHUANGXIU_WEB_IMAGE` | 前端镜像 | `ghcr.io/idea-flow/zhuangxiu-web:latest` |
| `NGINX_HTTP_PORT` | nginx 对外端口 | `80` |
| `FRONTEND_DOMAIN` | 前端页面域名（仅域名，不带协议/路径） | `localhost` |
| `BACKEND_DOMAIN` | 后端 API 域名，与前端域名不同 | `api.localhost` |
| `TZ` | 时区 | `Asia/Shanghai` |

生成强 JWT 密钥：

```bash
openssl rand -hex 32
# 把输出填到 .env 的 APP_JWT_SECRET
```

> ⚠️ 若你用自己的 GitHub 用户名构建了镜像，请把 `ZHUANGXIU_SERVER_IMAGE` / `ZHUANGXIU_WEB_IMAGE` 中的 `idea-flow` 替换为你的用户名。

## 第 4 步：启动（形态 A：纯 Docker 全栈）

```bash
cd docker
docker compose -f docker-compose.yml up -d
```

首次会拉取镜像并启动 5 个容器。查看状态：

```bash
docker compose -f docker-compose.yml ps
docker compose -f docker-compose.yml logs -f zhuangxiu-server   # 看后端日志，确认 Flyway 建表成功
```

浏览器访问 `http://<服务器IP>:80`（若 `NGINX_HTTP_PORT` 改过则用对应端口）。应能看到首页；用账号登录后进入概览。

## 第 5 步：启动（形态 B：前端外托管 + 后端）

小程序需要独立 API 域名时，参见 [Spring Boot 后端独立域名配置](4-SpringBoot后端独立域名配置.md)：全栈直接在现有 Nginx 新增 API server，通过容器网络访问后端。

若前端托管在 Cloudflare Pages 等，服务器只跑后端：

```bash
cd docker
docker compose -f docker-compose-server.yml up -d
```

后端会对外暴露 `9096` 端口。再用 1Panel / 手工 Nginx 把域名（如 `api.yourdomain.com`）反代到 `127.0.0.1:9096` 即可。

手工 Nginx 反代示例（server 块内）：

```nginx
location / {
    proxy_pass http://127.0.0.1:9096;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

## 第 6 步：常用运维命令

```bash
# 停止（保留数据卷）
docker compose -f docker-compose.yml stop

# 停止并删除容器（不删数据卷，数据库数据保留）
docker compose -f docker-compose.yml down

# 完全清理（含数据库数据，慎用）
docker compose -f docker-compose.yml down -v

# 查看实时日志
docker compose -f docker-compose.yml logs -f

# 仅重启后端（改了 .env 后需重启使环境变量生效）
docker compose -f docker-compose.yml up -d zhuangxiu-server

# 升级到新版本镜像
docker compose -f docker-compose.yml pull
docker compose -f docker-compose.yml up -d
```

## 第 7 步：故障排查

| 现象 | 可能原因 | 处理 |
| --- | --- | --- |
| 前端白屏 / 接口 404 | nginx 未启动或配置未挂载 | `docker compose ps` 看 nginx 状态；`docker logs zhuangxiu-nginx` |
| 登录后瞬间被踢回登录 | `APP_JWT_SECRET` 前后端/重启不一致，或 nginx 未正确转发 `/api` | 确认 `.env` 中密钥 ≥32 字节且重启后端；检查 `/api/` 转发 |
| 后端起不来，报数据库连接失败 | postgres 尚未就绪 / 密码不对 | 等 healthcheck 通过；核对 `DB_*` 与 `.env` 一致 |
| 后端报 Flyway 校验失败 | 手动改过库表结构 | 不要手动改表；必要时备份后重置数据卷 |
| 镜像拉取 401/403 | 私有镜像未登录 GHCR | `echo <token> | docker login ghcr.io -u <用户> --password-stdin`（公开镜像无需） |

## 第 8 步：升级流程（发新版本）

1. 代码推 `main`，GitHub Actions 重新构建镜像（tag 为 `:latest` 与 `:sha-<commit>`）。
2. 服务器上：

```bash
cd docker
docker compose -f docker-compose.yml pull
docker compose -f docker-compose.yml up -d
```

数据库由 Flyway 管理，结构变更会随后端启动自动迁移，无需手动处理（不要在外部手动改表）。

## 附：目录与文件速查

```
docker/
├── docker-compose.yml            # 形态 A：全栈（nginx+web+server+pg+redis）
├── docker-compose-server.yml     # 形态 B：仅后端（server+pg+redis，暴露 9096）
├── .env.example                  # 环境变量模板
├── backend/
│   ├── Dockerfile.native         # 后端 Native 镜像
│   └── Dockerfile.jvm            # 后端 JVM 镜像
├── frontend/
│   └── Dockerfile                # 前端 Next.js 镜像
└── nginx/
    └── nginx.conf                # 反向代理配置（/api→server，其余→web）
```

根目录 `.dockerignore` 已排除 `node_modules`、`target`、`.env`、文档等，保证构建上下文干净。
