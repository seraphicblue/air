# Build stage
FROM gradle:8.11-jdk21 AS builder

# 로컬 Gradle 캐시 복사
COPY --from=gradle:8.11-jdk21 /root/.gradle /root/.gradle

WORKDIR /build
COPY . .

# Gradle 메모리 옵션 설정
ENV GRADLE_OPTS="-Xmx4096m"
RUN ./gradlew bootJar

# Run stage
FROM openjdk:21-slim
WORKDIR /app

# JAR 파일만 복사
COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080

# 단순히 JAR 실행
ENTRYPOINT ["java", "-jar", "app.jar"]