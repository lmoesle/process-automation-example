import react from "@vitejs/plugin-react";
import checker from "vite-plugin-checker";
import { defineConfig } from "vite";

const DEV_PORT = 3000;
const API_TARGET = "http://localhost:8080";

process.env.LAUNCH_EDITOR = "idea";

export default defineConfig({
  plugins: [
    react(),
    checker({
      typescript: true,
      eslint: {
        lintCommand: 'eslint "./src/**/*.{ts,tsx}"',
        useFlatConfig: true,
      },
    }),
  ],
  cacheDir: "node_modules/.vite/process-automation-example-frontend",
  server: {
    host: true,
    port: DEV_PORT,
    strictPort: true,
    proxy: {
      "/api": API_TARGET,
      "/v3/api-docs": API_TARGET,
    },
  },
  build: {
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes("node_modules")) {
            return undefined;
          }

          if (id.includes("react-router") || id.includes("/react/") || id.includes("/react-dom/")) {
            return "vendor-react";
          }

          if (id.includes("@mui") || id.includes("@emotion")) {
            return "vendor-mui";
          }

          if (id.includes("@tanstack") || id.includes("openapi-fetch")) {
            return "vendor-data";
          }

          return undefined;
        },
      },
    },
  },
});
