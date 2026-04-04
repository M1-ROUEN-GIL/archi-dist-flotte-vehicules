# Stage 1: Build
FROM node:20.18-alpine AS builder

WORKDIR /app

# Update npm to latest
RUN npm install -g npm@latest

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source and schemas
COPY . .

# Build TypeScript
RUN npm run build

# Stage 2: Runtime
FROM node:20.18-alpine

WORKDIR /app

RUN apk upgrade --no-cache && \
    npm install -g npm@latest && \
    # Force patch picomatch to 4.0.4 in npm's own tree to resolve CVE-2026-33671
    find /usr/local/lib/node_modules/npm -name picomatch -type d -exec npm install --prefix {} picomatch@4.0.4 \;

# Copy production dependencies only
COPY package*.json ./
RUN npm install --omit=dev

# Copy compiled code and schemas (needed for runtime loading)
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/graphql/schemas ./dist/graphql/schemas

# Aligné sur compose.yaml (services vehicle, driver, maintenance — pas *-service).
ENV PORT=4000
ENV VEHICLE_SERVICE_URL=http://vehicle:8080
ENV DRIVER_SERVICE_URL=http://driver:8080
ENV MAINTENANCE_SERVICE_URL=http://maintenance:8080

EXPOSE 4000

CMD ["npm", "start"]
