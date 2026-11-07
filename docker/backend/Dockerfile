# Backend Viet Clinic — Spring Boot 2.1 / Java 8, đóng gói thành jar.
# Build stage dùng Maven + Temurin 8 (khớp bản Java 8 của dự án);
# runtime stage chỉ mang JRE 8 + fontconfig (cần cho in ấn ureport/PDF).
# Không dùng settings-mirror của máy dev: máy người dùng kéo dep trực tiếp
# từ Maven Central. Lần build đầu tải vài trăm MB dep nên sẽ chậm, các lần
# sau dùng cache layer (chỉ rebuild khi pom.xml hoặc src đổi).

FROM maven:3.9.9-eclipse-temurin-8 AS build
WORKDIR /build
COPY Code/medical2.0/back/pom.xml ./pom.xml
# Tải trước dependencies để layer này được cache khi chỉ sửa src.
RUN mvn -q dependency:go-offline
COPY Code/medical2.0/back/src ./src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:8-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 7016
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-Xms256m", "-Xmx1024m", "-Dspring.profiles.active=dev", "-jar", "app.jar"]
