# Build stage
FROM gradle:8.5-jdk21 AS builder
WORKDIR /build
COPY . .
RUN ./gradlew bootJar

# Run stage
FROM openjdk:21-slim
WORKDIR /app

# JAR 파일만 복사
COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080

# 단순히 JAR 실행
ENTRYPOINT ["java", "-jar", "app.jar"]