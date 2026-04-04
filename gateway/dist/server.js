import { ApolloServer } from '@apollo/server';
import { startStandaloneServer } from '@apollo/server/standalone';
import { readFileSync, readdirSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';
import { resolvers } from './graphql/resolvers/index.js';
import { createContext } from './graphql/context.js';
const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);
// Load all schema files
// When running from dist/server.js, schemas are in dist/graphql/schemas/
// or if we copy them next to it.
const schemasDir = join(__dirname, 'graphql', 'schemas');
console.log(`Loading schemas from: ${schemasDir}`);
const typeDefs = readdirSync(schemasDir)
    .filter(file => file.endsWith('.graphql'))
    .map(file => readFileSync(join(schemasDir, file), 'utf-8'))
    .join('\n');
const server = new ApolloServer({
    typeDefs,
    resolvers,
});
const { url } = await startStandaloneServer(server, {
    context: async ({ req }) => createContext({ req }),
    listen: { port: 4000 },
});
console.log(`🚀  Server ready at: ${url}`);
