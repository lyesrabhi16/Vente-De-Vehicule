import { mkdtemp, readFile, rm } from 'node:fs/promises';
import { createServer, type Server as HttpServer } from 'node:http';
import type { AddressInfo } from 'node:net';
import os from 'node:os';
import path from 'node:path';
import { createConnection } from 'mysql2/promise';
import request from 'supertest';
import { createApp, type RateLimits } from '../src/app';
import { loadConfig, type Config } from '../src/config';
import { createDb, run, type Db } from '../src/db';
import { silentLogger } from '../src/log';
import { attachRealtime } from '../src/realtime';

/** La base de test : `docker compose -f docker-compose.test.yml up -d` (voir le README), ou les variables TEST_DB_*. */
export const testEnv = (): NodeJS.ProcessEnv => ({
  NODE_ENV: 'test',
  DB_HOST: process.env.TEST_DB_HOST ?? '127.0.0.1',
  DB_PORT: process.env.TEST_DB_PORT ?? '33306',
  DB_USER: process.env.TEST_DB_USER ?? 'miniprojet',
  DB_PASSWORD: process.env.TEST_DB_PASSWORD ?? 'miniprojet-dev',
  DB_NAME: process.env.TEST_DB_NAME ?? 'miniprojet_test',
  JWT_SECRET: 'secret-de-test-secret-de-test-secret-de-test',
});

export interface TestServer {
  config: Config;
  db: Db;
  http: HttpServer;
  url: string;
  api: ReturnType<typeof request>;
  imagesDir: string;
  close(): Promise<void>;
}

export async function startServer(limits: Partial<RateLimits> = {}): Promise<TestServer> {
  const imagesDir = await mkdtemp(path.join(os.tmpdir(), 'vente-images-'));
  const config = { ...loadConfig(testEnv()), imagesDir };
  const db = createDb(config);

  const connection = await createConnection({ ...config.db, multipleStatements: true });
  await connection.query(await readFile(path.join(__dirname, '..', 'db', 'schema.sql'), 'utf8'));
  await connection.query('SET FOREIGN_KEY_CHECKS = 0; TRUNCATE message; TRUNCATE reservation; TRUNCATE rendezvous; TRUNCATE annonce; TRUNCATE client; SET FOREIGN_KEY_CHECKS = 1');
  await connection.end();

  const http = createServer(createApp({ config, db, log: silentLogger, limits: { global: 100_000, signinFailures: 100_000, signups: 100_000, ...limits } }));
  const io = attachRealtime(http, { db, config, log: silentLogger });
  await new Promise<void>((resolve) => http.listen(0, '127.0.0.1', resolve));
  const url = `http://127.0.0.1:${(http.address() as AddressInfo).port}`;

  return {
    config,
    db,
    http,
    url,
    api: request(http),
    imagesDir,
    async close() {
      await io.close();
      await db.end();
      await rm(imagesDir, { recursive: true, force: true });
    },
  };
}

let counter = 0;

export interface Account {
  idClient: number;
  token: string;
  email: string;
  /** Les en-têtes d'une requête de ce compte. */
  as: { Authorization: string };
}

export async function signup(server: TestServer, overrides: Record<string, string> = {}): Promise<Account> {
  counter += 1;
  const email = overrides.email ?? `user${counter}@exemple.fr`;
  const res = await server.api
    .post('/signup')
    .type('form')
    .send({ nomClient: 'Nom', prenomClient: `Prenom${counter}`, ageClient: '30', email, numTel: `06000000${String(counter).padStart(2, '0')}`, password: 'motdepasse-solide', ...overrides });
  if (res.body.error) throw new Error(`signup : ${res.body.error}`);
  return { idClient: res.body.idClient, token: res.body.token, email, as: { Authorization: `Bearer ${res.body.token}` } };
}

export async function makeAdmin(server: TestServer, account: Account): Promise<void> {
  await run(server.db, "UPDATE client SET role = 'admin' WHERE idClient = ?", [account.idClient]);
}

export const annonceForm = (overrides: Record<string, string> = {}) => ({
  titre: 'Peugeot 208 en parfait état',
  description: 'Première main, entretien à jour.',
  typeVehicule: 'Citadine',
  marqueVehicule: 'Peugeot',
  modeleVehicule: '208',
  couleurVehicule: 'Bleu',
  transmissionVehicule: 'Manuelle',
  kilometrageVehicule: '42000',
  anneeVehicule: '2019',
  moteurVehicule: '1.2 PureTech',
  energieVehicule: 'Essence',
  prixVehicule: '11500',
  ...overrides,
});

export async function postAnnonce(server: TestServer, seller: Account, overrides: Record<string, string> = {}): Promise<number> {
  const res = await server.api.post('/annonce/add').set(seller.as).type('form').send(annonceForm(overrides));
  if (res.body.error) throw new Error(`annonce/add : ${res.body.error}`);
  return res.body.result;
}
