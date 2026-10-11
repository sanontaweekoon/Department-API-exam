FROM eclipse-temurin:21.0.12.1_1-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN chmod +x gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21.0.12.1_1-jdk-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8200

ENTRYPOINT ["java", "-jar", "app.jar"]