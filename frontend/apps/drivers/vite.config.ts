import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import federation from '@originjs/vite-plugin-federation';

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'drivers_app',
      filename: 'remoteEntry.js',
      // On expose le composant principal de notre application
      exposes: {
        './DriverList': './src/App.tsx',
      },
      // On partage React et Apollo pour éviter les doublons avec le Shell
      shared: ['react', 'react-dom', 'react-router-dom', '@apollo/client', 'graphql'],
    }),
  ],
  server: {
    port: 5003,
    strictPort: true,
  },
  preview: {
    port: 5003,
    strictPort: true,
  },
  build: {
    modulePreload: false,
    target: 'esnext',
    minify: false,
    cssCodeSplit: false,
  },
});