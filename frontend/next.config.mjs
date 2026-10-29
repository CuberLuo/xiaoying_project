/** @type {import('next').NextConfig} */
const nextConfig = {
  output: "standalone",

  // 同源代理：把后端托管的静态资源（封面 /demo、素材与结果 /files）转发到 Java 后端。
  // 这样即使 <img> 用相对路径，也不会打到前端 3000 而 404。
  // 目标地址在构建期确定：Docker 内为 http://backend:8080，本地开发回退到 localhost:8080。
  async rewrites() {
    const target = process.env.INTERNAL_API_BASE || "http://localhost:8080";
    return [
      { source: "/demo/:path*", destination: `${target}/demo/:path*` },
      { source: "/files/:path*", destination: `${target}/files/:path*` }
    ];
  }
};

export default nextConfig;
