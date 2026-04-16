import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import federation from '@originjs/vite-plugin-federation';

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'location_app',
      filename: 'remoteEntry.js',
      exposes: {
        './LocationMap': './src/App.tsx',
      },
      shared: ['react', 'react-dom', '@apollo/client', 'graphql'],
    }),
  ],
  server: { port: 5005, strictPort: true },
  preview: { port: 5005, strictPort: true },
  build: { modulePreload: false, target: 'esnext', minify: false, cssCodeSplit: false },
});