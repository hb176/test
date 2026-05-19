import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

/**
 * Vite构建配置 - GMP前端项目
 * 开发环境代理到网关(8000)，解决跨域问题
 */
export default defineConfig({
  plugins: [
    vue(),
    // 自动导入Vue/Element Plus的API，无需手动import
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
    }),
    // 自动注册Element Plus组件
    Components({
      resolvers: [ElementPlusResolver()],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 3000,
    proxy: {
      // 代理API请求到网关
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
      // BPMN设计器API代理
      '/flow': {
        target: 'http://localhost:8000',
        changeOrigin: true,
      },
    },
  },
})
