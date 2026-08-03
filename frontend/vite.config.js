import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Port 3000 matches the backend's allowed CORS / WebSocket origins.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api': 'http://localhost:7000',
      '/ws': {
        target: 'ws://localhost:7000',
        ws: true
      }
    }
  }
})
