import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      // Health probe used by the server-status gate to detect when the backend is down.
      '/actuator': 'http://localhost:8080'
    }
  }
})
