# Build stage
FROM gradle:8.5-jdk21 AS builder
WORKDIR /build
COPY . .
RUN gradle bootJar

# Run stage
FROM openjdk:21-slim
COPY --from=builder /build/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]