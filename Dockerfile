# ─── Stage 1: Build ──────────────────────────────────────────────────────────# 실행 스테이지 (ARM64 기반 JRE)
FROM --platform=linux/arm64 eclipse-temurin:25-jre-alpine

WORKDIR /app

# GitHub Actions Runner에서 미리 빌드된 JAR 파일을 복사 (매우 빠름)
COPY build/libs/*.jar app.jar

# 보안을 위한 비관리자 계정 설정
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# OCI Private Key 마운트 경로 (컨테이너 내부 기준)
VOLUME /app/secrets

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
