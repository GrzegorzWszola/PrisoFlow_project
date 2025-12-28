import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react({
      babel: {
        plugins: [['babel-plugin-react-compiler']],
      },
    }),
  ],
  server: {
    watch: {
      usePolling: true,
    },
    host: true,
  },
  test: {
    globals: true, 
    environment: 'happy-dom', 
    setupFiles: './src/setupTests.js',
    exclude: [
      '**/node_modules/**', 
      '**/dist/**', 
      'tests/**',      // Wyklucza folder tests w głównym katalogu
      'src/tests/**'   // Wyklucza folder tests w src
    ],
  },
})
