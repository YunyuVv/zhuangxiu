# 小程序通过独立 API 域名访问后端

更新日期：2026-11-09。用户已明确：使用现有 Nginx 增加 api 域名，供微信小程序调用。

## 当前配置

全栈部署中的 Nginx 与后端已经位于同一 Docker 网络，直接增加一个 server 即可。本次已修改 `docker/nginx/nginx.conf`：

```nginx
server {
    listen 80;
    server_name api.example.com;

    location / {
        proxy_pass http://zhuangxiu-server:9096;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

原 Web 入口继续处理页面和同域 /api；api.example.com 的所有路径直接发给后端。不需要额外 Nginx、不需要后端映射宿主机端口，也不需要额外 Compose 文件。前次提供的回环端口覆盖及宿主机代理示例已移除，历史过程保留在开发记录。

```text
小程序 → https://api.真实域名/api/... → 现有 Nginx → zhuangxiu-server:9096
浏览器 → Web 域名 → 同一 Nginx → Next.js 或 /api 对应后端
```

当前落地的是 HTTP 域名分流；正式小程序还需下面的真实域名和 HTTPS 配置。

## 服务器配置

1. DNS 添加 api 的 A 记录指向服务器公网 IP，并将 nginx.conf 中 api.example.com 替换成真实域名。
2. 用该域名的有效证书启用 HTTPS。若由当前 Nginx 终止 TLS，需要在 API server 中将 listen 改成：

```nginx
listen 443 ssl;
ssl_certificate /etc/nginx/certs/api/fullchain.pem;
ssl_certificate_key /etc/nginx/certs/api/privkey.pem;
ssl_protocols TLSv1.2 TLSv1.3;
```

路径是部署约定，需先放入真实证书文件，再启用此配置，不能用不存在的证书路径直接启动。

3. 在现有 `docker-compose.yml` 的 zhuangxiu-nginx 服务中保留原来的 HTTP 映射和 nginx.conf 挂载，再增加 HTTPS 端口与证书目录：

```yaml
ports:
  - "${NGINX_HTTP_PORT:-80}:80"
  - "443:443"
volumes:
  - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
  - ./nginx/certs:/etc/nginx/certs:ro
```

证书文件实际存放在服务器 `docker/nginx/certs/api/`，私钥不提交仓库。安全组/防火墙放行 443。若现有外层网关已处理 HTTPS，可由它转发到此 Nginx 的 HTTP API 虚拟主机并保留 Host，不必重复占用 443；本项目不自动部署外层网关。

4. 在服务器应用配置：

```bash
cd /opt/zhuangxiu/docker
# 检查 Compose 并让新增端口/卷生效；首次启动还需镜像、数据库等前置条件
# 如果已有容器且仅修改 nginx.conf，也应先验证再 reload
# nginx -t 需在服务可启动且上游/证书可用的目标环境执行

docker compose --env-file .env -f docker-compose.yml config --quiet
docker compose --env-file .env -f docker-compose.yml up -d zhuangxiu-nginx
docker compose --env-file .env -f docker-compose.yml exec zhuangxiu-nginx nginx -t
docker compose --env-file .env -f docker-compose.yml exec zhuangxiu-nginx nginx -s reload
```

若之前试过前次的端口覆盖文件，改回仅使用基础 Compose 重建后端，移除临时 9096 映射；后续统一使用基础全栈 Compose。

## 微信小程序配置与调用

- 小程序后台「开发管理 → 开发设置 → 服务器域名」中添加 request 合法域名：`https://api.真实域名`，不带 /api 路径。按平台要求完成域名备案等校验。
- 正式请求使用有效 HTTPS 证书；开发工具中关闭合法域名校验不能代替正式配置。
- 用 wx.request 调用 `https://api.真实域名/api/...`；登录返回的 token 在受保护请求中通过 Authorization: Bearer <token> 传入。
- wx.request 不受浏览器 CORS 规则限制，为此次小程序接入无需修改 Web 前端 baseURL 或 CORS。
- 若小程序直接上传 imgbb，再按实际上传接口配置 uploadFile 合法域名；若使用 wx.request 提交则配置 request 合法域名。现有小程序目录仍为占位，尚未实现上传逻辑。

## 验证与边界

```bash
# HTTP 域名分流：端口按 NGINX_HTTP_PORT 调整
curl --noproxy '*' -i -H 'Host: api.真实域名' http://127.0.0.1/api/guides

# 真实证书/DNS 生效后，从外部验证，不使用 -k
curl -i https://api.真实域名/api/guides
```

应返回指南 JSON；受保护接口匿名 401 属于正常鉴权。不要用根路径 / 是否返回网页判断 API 是否正常。

本次未启动服务、未申请证书或设置微信后台；本机 Docker daemon 未运行，未执行 nginx -t。现有部署前置项见 [首次部署清单](3-首次部署核对与执行清单.md)，指南 SSR 等独立问题仍按台账处理。
