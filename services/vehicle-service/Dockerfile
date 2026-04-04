# Étape 1 : Construction
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# On copie d'abord le pom.xml pour télécharger les dépendances
# (ça met en cache les téléchargements Maven)
COPY pom.xml .
RUN mvn dependency:go-offline

# Télécharger l'agent OpenTelemetry
RUN wget -O opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

# On copie tout le code source et on compile le projet
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Étape 2 : Production
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Sécurité : création d'un utilisateur non-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# On récupère l'application compilée depuis l'étape 1
COPY --from=builder /build/target/*.jar app.jar
COPY --from=builder /build/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

# On expose le port de Spring Boot
EXPOSE 8080

# Même modèle que driver/maintenance : l'agent est injecté via JAVA_TOOL_OPTIONS (Compose / Helm), pas en double ici.
ENTRYPOINT ["java", "-jar", "app.jar"]