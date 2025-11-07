# 빌드 스테이지
FROM gradle:8.10-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle build -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/build/libs/*.jar app.jar

# 개발 환경에서 디버깅 포트 열기
EXPOSE 8080 5005

# 헬스체크 (Actuator가 없으면 루트로 대체)
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 CMD sh -c 'curl -fsS http://localhost:8080/actuator/health || curl -fsS http://localhost:8080 || exit 1'

# JVM 옵션 설정 (메모리 최적화)
ENTRYPOINT ["java", "-jar", "-Xmx512m", "-Dspring.profiles.active=local", "app.jar"]