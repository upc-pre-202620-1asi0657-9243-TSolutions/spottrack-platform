# Dockerfile for SpotTrack Platform
# Multi-stage build: Maven + OpenJDK 26 para build, eclipse-temurin 26 JRE en runtime.
# Variables de entorno requeridas en prod: DATABASE_URL, DATABASE_PORT, DATABASE_NAME,
# DATABASE_USER, DATABASE_PASSWORD, JWT_SECRET, PORT, SPRING_PROFILES_ACTIVE=prod.

FROM maven:3.9.16-eclipse-temurin-26 AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:26-jre AS runtime
ENV SPRING_PROFILES_ACTIVE=prod
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
