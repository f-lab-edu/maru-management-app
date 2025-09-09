# 개발 환경용
FROM node:20-alpine AS development
WORKDIR /app

# 의존성 먼저 복사 (캐싱 활용)
COPY package*.json ./
RUN npm ci

# 소스 코드 복사
COPY . .

# 개발 서버 실행
EXPOSE 3000
CMD ["npm", "run", "dev"]

# 프로덕션 빌드용
FROM node:20-alpine AS build
WORKDIR /app

# 의존성 설치
COPY package*.json ./
RUN npm ci

# 소스 코드 복사 및 빌드
COPY . .
RUN npm run build

# 프로덕션 서빙용
FROM nginx:alpine AS production
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx 설정
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]