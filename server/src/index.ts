import { createServer } from 'node:http';
import { mkdir } from 'node:fs/promises';
import { createApp } from './app';
import { ConfigError, loadConfig } from './config';
import { createDb } from './db';
import { consoleLogger as log } from './log';
import { attachRealtime } from './realtime';

async function main(): Promise<void> {
  const config = loadConfig();
  for (const name of config.generated) log.warn(`${name} absent : valeur temporaire pour ce processus (les sessions ne survivront pas à un redémarrage)`);

  const db = createDb(config);
  await db.query('SELECT 1'); // échoue tout de suite, avec un message clair, si la base est injoignable
  await mkdir(config.imagesDir, { recursive: true });

  const http = createServer(createApp({ config, db, log }));
  const io = attachRealtime(http, { db, config, log });
  http.listen(config.port, () => log.info(`Serveur à l'écoute sur le port ${config.port} (${config.env})`));

  const stop = (signal: string) => {
    log.info(`${signal} reçu : arrêt`);
    io.close(() => db.end().finally(() => process.exit(0)));
    setTimeout(() => process.exit(1), 10_000).unref();
  };
  process.on('SIGTERM', () => stop('SIGTERM'));
  process.on('SIGINT', () => stop('SIGINT'));
}

main().catch((error: unknown) => {
  log.error(error instanceof ConfigError ? error.message : `Démarrage impossible : ${(error as Error).message}`);
  process.exit(1);
});
