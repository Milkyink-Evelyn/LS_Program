import path from "path";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

const pathSrc = path.resolve(__dirname, "src");

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "~/": `${pathSrc}/`,
    },
  },
  server: {
    port: 3000,
    watch: {
      ignored: ["**/*.tmpdir/**", "**/.*.tmpdir/**", "**/*.tmp"],
    },
    proxy: {
      "/api": {
        target: "http://localhost:22222",
        changeOrigin: true,
      },
      "/auth": {
        target: "http://localhost:22222",
        changeOrigin: true,
      },
    },
  },
  build: {
    // 最终提交时按课程要求输出到后端 public；本地联调可用 `vite build --outDir dist`
    outDir: "../java-server/src/main/resources/public",
    emptyOutDir: true,
    assetsDir: "assets",
    cssCodeSplit: true,
    sourcemap: false,
    minify: "terser",
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
    rollupOptions: {
      output: {
        chunkFileNames: "js/[name]-[hash].js",
        entryFileNames: "js/[name]-[hash].js",
        assetFileNames: "[ext]/[name]-[hash].[ext]",
      },
    },
  },
});

