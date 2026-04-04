# ==========================================
# Étape 1 : Build (Construction)
# ==========================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. On copie d'abord le pom.xml pour télécharger les dépendances
# (Ça permet à Docker de mettre cette étape en cache et de gagner du temps !)
COPY pom.xml .
RUN mvn dependency:go-offline

# 2. On copie le code source et on compile l'application
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Étape 2 : Production (Exécution)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Bonne pratique de sécurité utilisateur non-root
RUN addgroup -S spring && adduser -S spring -G spring \
        && apk upgrade --no-cache
USER spring:spring

# On récupère le fichier .jar généré à l'étape 1
COPY --from=builder /build/target/*.jar app.jar

# On expose le port sur lequel Spring Boot va tourner (par défaut 8080)
EXPOSE 8080

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]