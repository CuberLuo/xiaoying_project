# 依赖安装 + 构建（npm）
FROM node:22-alpine AS builder
WORKDIR /app

ARG NEXT_PUBLIC_API_BASE=http://localhost:8080
ENV NEXT_PUBLIC_API_BASE=$NEXT_PUBLIC_API_BASE

# 构建期用于对齐静态资源的同源代理目标（容器内用服务名 backend）
ARG INTERNAL_API_BASE=http://localhost:8080
ENV INTERNAL_API_BASE=$INTERNAL_API_BASE

COPY package.json ./
RUN npm install

COPY . .
RUN npm run build

# 运行（Next.js standalone 输出）
FROM node:22-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
