import { randomBytes } from 'node:crypto';
import { z } from 'zod';

/** Valeur de développement (le docker-compose.yml de la racine l'utilise) : refusée en production. */
export const DEV_DB_PASSWORD = 'miniprojet-dev';

const schema = z.object({
  NODE_ENV: z.enum(['development', 'test', 'production']).default('development'),
  PORT: z.coerce.number().int().min(1).max(65535).default(5000),
  DB_HOST: z.string().default('localhost'),
  DB_PORT: z.coerce.number().int().min(1).max(65535).default(3306),
  DB_USER: z.string().default('miniprojet'),
  DB_PASSWORD: z.string().optional(),
  DB_NAME: z.string().default('miniprojet'),
  JWT_SECRET: z.string().optional(),
  JWT_TTL_SECONDS: z.coerce.number().int().positive().default(7 * 24 * 3600),
  IMAGES_DIR: z.string().default('./images'),
  MAX_IMAGE_BYTES: z.coerce.number().int().positive().default(5 * 1024 * 1024),
  // derrière un reverse proxy (Traefik, nginx) : l'adresse du visiteur vient de X-Forwarded-For
  TRUST_PROXY: z
    .string()
    .optional()
    .transform((v) => v === 'true' || v === '1'),
});

export interface Config {
  env: 'development' | 'test' | 'production';
  port: number;
  db: { host: string; port: number; user: string; password: string; database: string };
  jwt: { secret: string; ttlSeconds: number };
  imagesDir: string;
  maxImageBytes: number;
  trustProxy: boolean;
  /** Ce que la configuration a dû inventer faute de valeur (hors production) : à dire dans le journal. */
  generated: string[];
}

export class ConfigError extends Error {}

/**
 * Lit la configuration de l'environnement. En production le serveur refuse de démarrer avec un secret manquant ou
 * un compte de base de données dangereux (root, mot de passe vide ou celui du développement) ; ailleurs, il invente
 * le secret des jetons pour le processus (les sessions ne survivent pas à un redémarrage) et le dit.
 */
export function loadConfig(env: NodeJS.ProcessEnv = process.env): Config {
  const parsed = schema.safeParse(env);
  if (!parsed.success) {
    throw new ConfigError(
      `Configuration invalide : ${parsed.error.issues.map((i) => `${i.path.join('.')} (${i.message})`).join(', ')}`,
    );
  }
  const v = parsed.data;
  const production = v.NODE_ENV === 'production';
  const problems: string[] = [];
  const generated: string[] = [];

  let secret = v.JWT_SECRET ?? '';
  if (secret.length === 0) {
    if (production) problems.push('JWT_SECRET est requis');
    else {
      secret = randomBytes(48).toString('base64url');
      generated.push('JWT_SECRET');
    }
  } else if (production && secret.length < 32) {
    problems.push('JWT_SECRET doit faire au moins 32 caractères');
  }

  let password = v.DB_PASSWORD;
  if (password === undefined) {
    if (production) problems.push('DB_PASSWORD est requis');
    password = DEV_DB_PASSWORD;
  }
  if (production && (password === '' || password === DEV_DB_PASSWORD)) {
    problems.push('DB_PASSWORD ne peut être ni vide ni celui du développement');
  }
  if (production && v.DB_USER === 'root') problems.push('DB_USER ne peut pas être root');

  if (problems.length > 0) throw new ConfigError(`Configuration dangereuse ou incomplète : ${problems.join(' ; ')}`);

  return {
    env: v.NODE_ENV,
    port: v.PORT,
    db: { host: v.DB_HOST, port: v.DB_PORT, user: v.DB_USER, password, database: v.DB_NAME },
    jwt: { secret, ttlSeconds: v.JWT_TTL_SECONDS },
    imagesDir: v.IMAGES_DIR,
    maxImageBytes: v.MAX_IMAGE_BYTES,
    trustProxy: v.TRUST_PROXY,
    generated,
  };
}
