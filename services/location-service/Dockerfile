# BUILD
FROM node:18.20-alpine AS builder
WORKDIR /app

RUN npm install -g npm@latest

# Copier les fichiers de dependances
COPY package*.json ./

# Installer les dependances
RUN npm install --production

# Copier le reste du code source
COPY . .

# RUNTIME
FROM node:18.20-alpine
WORKDIR /app

RUN apk upgrade --no-cache && \
    npm install -g npm@latest

RUN addgroup -g 1001 -S nodejs && \
    adduser -S nodejs -u 1001

COPY --from=builder --chown=nodejs:nodejs /app .

USER nodejs
EXPOSE 3002

CMD ["node", "src/app.js"]
