/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
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
