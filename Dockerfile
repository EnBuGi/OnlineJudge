# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM --platform=linux/arm64 eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /workspace

COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test -q

# ─── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM --platform=linux/arm64 eclipse-temurin:25-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar

# OCI Private Key 마운트 경로 (컨테이너 내부 기준)
VOLUME /app/secrets

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
