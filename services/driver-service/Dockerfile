# ==========================================
# Étape 1 : Build (Construction)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# On copie d'abord le pom.xml pour télécharger les dépendances
# (Astuce Docker : ça met en cache les téléchargements Maven !)
COPY pom.xml .
RUN mvn dependency:go-offline

# On copie tout le code source et on compile le projet
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Étape 2 : Production (Exécution)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Sécurité exigée par ton projet : création d'un utilisateur non-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# On récupère l'application compilée depuis l'étape 1
COPY --from=builder /build/target/*.jar app.jar

# On expose le port de Spring Boot
EXPOSE 8081

# Commande de lancement
ENTRYPOINT ["java", "-jar", "app.jar"]