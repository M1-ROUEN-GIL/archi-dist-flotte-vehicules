import 'dotenv/config';
import { ApolloServer } from '@apollo/server';
import { expressMiddleware } from '@apollo/server/express4';
import { ApolloServerPluginDrainHttpServer } from '@apollo/server/plugin/drainHttpServer';
import cors from 'cors';
import express from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import http from 'http';
import { readFileSync, readdirSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';
import { DRIVER_SERVICE_URL, MAINTENANCE_SERVICE_URL, VEHICLE_SERVICE_URL, runningInDocker, } from './config.js';
import { resolvers } from './graphql/resolvers/index.js';
import { createContext } from './graphql/context.js';
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
if (!runningInDocker) {
    console.warn('[gateway] Processus hors Docker : backends par défaut sur 127.0.0.1:8080/8081/8082. ' +
        'Surcharge possible via VEHICLE_SERVICE_URL, DRIVER_SERVICE_URL, MAINTENANCE_SERVICE_URL.');
}
const schemasDir = join(__dirname, 'graphql', 'schemas');
console.log(`Loading schemas from: ${schemasDir}`);
const typeDefs = readdirSync(schemasDir)
    .filter((file) => file.endsWith('.graphql'))
    .map((file) => readFileSync(join(schemasDir, file), 'utf-8'))
    .join('\n');
const app = express();
const httpServer = http.createServer(app);
const apollo = new ApolloServer({
    typeDefs,
    resolvers,
    plugins: [ApolloServerPluginDrainHttpServer({ httpServer })],
});
await apollo.start();
app.use('/graphql', cors(), express.json({ limit: '50mb' }), expressMiddleware(apollo, {
    context: async ({ req }) => createContext({ req }),
}));
/**
 * Avec app.use('/api/vehicles', …), Express enlève le préfixe : req.url vaut / ou /xxx, pas /api/vehicles/xxx.
 * Il faut donc préfixer par le chemin Spring (/vehicles, /drivers, /maintenance).
 */
const proxyOpts = { changeOrigin: true, logLevel: 'warn' };
function rewriteMounted(prefix) {
    return (path) => prefix + (path === '/' ? '/' : path);
}
app.use('/api/vehicles', createProxyMiddleware({
    ...proxyOpts,
    target: VEHICLE_SERVICE_URL,
    pathRewrite: rewriteMounted('/vehicles'),
}));
app.use('/api/drivers', createProxyMiddleware({
    ...proxyOpts,
    target: DRIVER_SERVICE_URL,
    pathRewrite: rewriteMounted('/drivers'),
}));
app.use('/api/maintenance', createProxyMiddleware({
    ...proxyOpts,
    target: MAINTENANCE_SERVICE_URL,
    pathRewrite: rewriteMounted('/maintenance'),
}));
app.use((_req, res) => {
    res.status(404).type('json').send(JSON.stringify({ error: 'Not found. Utilisez /graphql ou /api/{vehicles,drivers,maintenance}/…' }));
});
await new Promise((resolve) => {
    httpServer.listen({ port: 4000 }, resolve);
});
console.log(`🚀  Gateway — GraphQL /graphql — REST /api/* → vehicle=${VEHICLE_SERVICE_URL} driver=${DRIVER_SERVICE_URL} maintenance=${MAINTENANCE_SERVICE_URL}`);
