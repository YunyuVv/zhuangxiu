# syntax=docker/dockerfile:1.7

# 构建阶段：安装依赖并构建 Next.js 15 应用。
# 构建上下文为项目根目录，通过 COPY web/ 引入前端源码。
FROM node:22-alpine AS builder

WORKDIR /app

# 启用 pnpm（锁定 v9，与 pnpm-lock.yaml 匹配）。
RUN corepack enable && corepack prepare pnpm@9 --activate

# 配置 pnpm store 路径，与 cache mount 保持一致。
RUN pnpm config set store-dir /root/.pnpm-store

# 先复制依赖描述文件，利用 Docker 层缓存加速重复构建。
COPY web/package.json web/pnpm-lock.yaml ./

RUN --mount=type=cache,target=/root/.pnpm-store \
    pnpm install --frozen-lockfile

# 复制全部前端源码并构建。
COPY web/ ./

RUN pnpm build

# 运行阶段：保留 .next 构建产物与运行时依赖，使用 next start 提供服务。
FROM node:22-alpine AS runtime

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000

COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/next.config.mjs ./next.config.mjs

EXPOSE 3000

# 启动 Next.js 生产服务（浏览器端 /api 请求经 nginx 同域转发到后端，无需在此配置）。
CMD ["node", "node_modules/next/dist/bin/next", "start"]
