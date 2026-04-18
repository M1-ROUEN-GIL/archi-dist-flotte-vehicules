# BUILD STAGE (On compile le TypeScript)
FROM node:18.20-alpine AS builder
WORKDIR /app

# Copier les fichiers de dépendances
COPY package*.json ./

# Installer TOUTES les dépendances (y compris les devDependencies pour compiler)
RUN npm ci

# Copier tout le code source
COPY . .

# Compiler le code TypeScript (crée le dossier /dist)
RUN npm run build

# Nettoyer et ne garder QUE les dépendances de production (allège l'image finale)
RUN npm ci --only=production && npm cache clean --force

# RUNTIME STAGE (On ne garde que ce qui est utile pour tourner)
FROM node:18.20-alpine
WORKDIR /app

RUN apk upgrade --no-cache && \
    addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001

# Copier les node_modules de production
COPY --from=builder --chown=nodejs:nodejs /app/node_modules ./node_modules
# Copier le code compilé
COPY --from=builder --chown=nodejs:nodejs /app/dist ./dist
# Copier le package.json (nécessaire pour certains scripts/modules)
COPY --from=builder --chown=nodejs:nodejs /app/package.json ./

# ⚠️ TRÈS IMPORTANT POUR gRPC : S'assurer que les fichiers .proto sont copiés
# (Par défaut, NestJS ignore les fichiers non-TS lors du build)
COPY --from=builder --chown=nodejs:nodejs /app/src/grpc ./dist/proto

# Copier le simulateur JS (non compilé par tsc car pas TypeScript)
COPY --from=builder --chown=nodejs:nodejs /app/src/simulation ./dist/simulation

USER nodejs

# Le port classique de NestJS est 3000, mais tu peux garder 3002 si ton infra l'exige
EXPOSE 3002

# Lancement de l'application compilée avec instrumentation OTel
CMD ["node", "-r", "./dist/tracing.js", "dist/main.js"]