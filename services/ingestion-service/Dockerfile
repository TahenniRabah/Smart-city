# =========================
# Build stage
# =========================
FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY config ./config
COPY src ./src

RUN mvn clean package -DskipTests -B --no-transfer-progress

# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:25-jre-alpine

RUN apk add --no-cache curl \
    && addgroup -S urbanhub \
    && adduser -S urbanhub -G urbanhub

WORKDIR /app

COPY --from=build --chown=urbanhub:urbanhub \
    /app/target/*.jar app.jar

USER urbanhub

EXPOSE 8082

ENTRYPOINT [
    "java",
    "-XX:+UseContainerSupport",
    "-XX:MaxRAMPercentage=75.0",
    "-jar",
    "app.jar"
]