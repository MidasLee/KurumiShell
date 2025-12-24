import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from "path"
import { viteCommonjs } from '@originjs/vite-plugin-commonjs'

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())

  return {
    plugins: [vue(), viteCommonjs()],
    server: {
      host: '0.0.0.0',
      port: Number(env.VITE_APP_PORT),
      strictPort: true,
      open: true
    },
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    }
  }
})
