import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import federation from '@originjs/vite-plugin-federation'

export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'vehicles_app',
      filename: 'remoteEntry.js',
      exposes: {
        './VehicleList': './src/App.tsx',
      },
      shared: ['react', 'react-dom', '@apollo/client', 'graphql']
    })
  ],
  server: { port: 5002, strictPort: true }, // On met l'app sur le port 5002
  preview: {
    port: 5002,
    strictPort: true,
  },
  build: { modulePreload: false, target: 'esnext', minify: false, cssCodeSplit: false }
})