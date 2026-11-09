/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // 服务端组件（装修指南 SSR）直连后端用的基础地址；容器/生产环境用 BACKEND_URL 覆盖。
  env: {
    BACKEND_URL: process.env.BACKEND_URL || "http://localhost:9096",
  },
  // 开发期把 /api 代理到后端（Spring Boot 4，端口 9096）
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: "http://localhost:9096/api/:path*",
      },
    ];
  },
};

export default nextConfig;
