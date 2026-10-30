# Viet-clinic — Dockerfile (blocker 8, file mới, KHÔNG deploy thật).
#
# Ảnh này build cả frontend lẫn backend từ đúng lệnh của repo, nhưng CHỈ để
# đóng gói/kiểm chứng build; không có production thật nên không chạy deploy nào
# từ file này. Backend chạy Java 8 (pom java.version=1.8, Spring Boot 2.1).
#
# Cách dùng (khi có Docker và đã cấp đủ biến môi trường production, KHÔNG
# ghi secret vào image):
#   docker build -t viet-clinic:local .
#   docker run -p 7016:7016 \
#     -e SPRING_PROFILES_ACTIVE=prod \
#     -e DB_URL='jdbc:mysql://<host>:3306/medical?...' \
#     -e DB_USERNAME='<user>' -e DB_PASSWORD='<secret>' \
#     -e JWT_SECRET='<secret>' viet-clinic:local
# Frontend build xong nằm ở /opt/frontend/dist trong image, để nginx/static
# hosting phục vụ riêng — backend repo này không tự serve thư mục dist đó.
# Health check dùng endpoint đã mở riêng cho monitoring: /actuator/health
# (xem ShiroConfiguration + application.yml: chỉ health được anon/expose).

# ---------- Stage 1: build frontend (Vue 2, vue-cli-service build) ----------
FROM node:20-alpine AS frontend-build
WORKDIR /build/client
COPY Code/medical2.0/client/package.json Code/medical2.0/client/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY Code/medical2.0/client/ ./
RUN npm run build

# ---------- Stage 2: build backend (Java 8, Maven, profile prod) ----------
FROM maven:3.8.6-eclipse-temurin-8 AS backend-build
WORKDIR /build/back
COPY Code/medical2.0/back/pom.xml ./
COPY Code/medical2.0/back/lib ./lib
COPY Code/medical2.0/back/src ./src
# -Pprod: để application-prod.yml được đóng gói vào jar (mặc định của pom là
# dev và sẽ loại application-prod.yml). Test đã có trong CI; ở đây skip để
# image build không phụ thuộc test runner của máy build.
RUN mvn -B -DskipTests package -Pprod

# ---------- Stage 3: runtime (Java 8 JRE) ----------
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=backend-build /build/back/target/*.jar /app/app.jar
COPY --from=frontend-build /build/client/dist /opt/frontend/dist
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 7016
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:7016/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
