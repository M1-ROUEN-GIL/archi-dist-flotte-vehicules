import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import federation from '@originjs/vite-plugin-federation' // 👈 1. Il manquait l'import du plugin

export default defineConfig({
  plugins: [
    react(),
    // 👈 2. Le bloc remotes DOIT être à l'intérieur du plugin federation !
    federation({
      name: 'shell_app',
      remotes: {
        vehicles_app: process.env.NODE_ENV === 'production' 
          ? '/remotes/vehicles/assets/remoteEntry.js'
          : 'http://localhost:5002/assets/remoteEntry.js',
        drivers_app: process.env.NODE_ENV === 'production'
          ? '/remotes/drivers/assets/remoteEntry.js'
          : 'http://localhost:5003/assets/remoteEntry.js',
        maintenance_app: process.env.NODE_ENV === 'production'
          ? '/remotes/maintenance/assets/remoteEntry.js'
          : 'http://localhost:5004/assets/remoteEntry.js',
        location_app: process.env.NODE_ENV === 'production'
          ? '/remotes/location/assets/remoteEntry.js'
          : 'http://localhost:5005/assets/remoteEntry.js'
      },
      // On partage les librairies pour que le Shell et les apps utilisent le même contexte
      shared: ['react', 'react-dom', 'react-router-dom', '@flotte/shared-auth', '@apollo/client', 'graphql']
    })
  ],
  resolve: {
    dedupe: ['react', 'react-dom', 'react-router-dom'],
  },
  // Requis par Vite Federation pour que la compilation marche bien
  build: {
    modulePreload: false,
    target: 'esnext',
    minify: false, // 👈 Désactivé pour le debug
  }
})