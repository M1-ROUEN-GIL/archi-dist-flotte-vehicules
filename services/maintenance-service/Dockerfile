# ==========================================
# Etape 1 : Build (Construction)
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. On copie d'abord le pom.xml pour telecharger les dependances
COPY pom.xml .
RUN mvn dependency:go-offline

# Telecharger l'agent OpenTelemetry
RUN wget -O opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# 2. On copie tout le code source et on compile le projet
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# ==========================================
# Etape 2 : Production (Execution)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# On cree un utilisateur non-root par securite
RUN addgroup -S spring && adduser -S spring -G spring \
        && apk upgrade --no-cache \
        && apk add --no-cache curl
USER spring:spring
# On recupere le fichier .jar genere a l'etape 1
COPY --from=builder /build/target/*.jar app.jar
COPY --from=builder /build/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

# On expose le port sur lequel Spring Boot va tourner (ici 8080)
EXPOSE 8080

# Commande de demarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
