import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    include: ['test/**/*.test.ts'],
    // les fichiers de test partagent une base MySQL : ils passent l'un après l'autre
    fileParallelism: false,
    testTimeout: 20_000,
    hookTimeout: 60_000,
  },
});
