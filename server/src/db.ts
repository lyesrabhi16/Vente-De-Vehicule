import { createPool, type Pool, type PoolOptions, type ResultSetHeader, type RowDataPacket } from 'mysql2/promise';
import type { Config } from './config';

export type Db = Pool;

export function createDb(config: Config, overrides: PoolOptions = {}): Db {
  return createPool({
    host: config.db.host,
    port: config.db.port,
    user: config.db.user,
    password: config.db.password,
    database: config.db.database,
    waitForConnections: true,
    connectionLimit: 10,
    charset: 'utf8mb4',
    // 'YYYY-MM-DD HH:mm:ss', comme l'application les envoie et les affiche (elle les traite comme du texte)
    dateStrings: true,
    // une requête = une instruction : pas d'injection par « ; DROP TABLE »
    multipleStatements: false,
    ...overrides,
  });
}

/** SELECT : les lignes. */
export async function rows<T extends object = Record<string, unknown>>(db: Db, sql: string, params: unknown[] = []): Promise<T[]> {
  const [result] = await db.query<RowDataPacket[]>(sql, params);
  return result as unknown as T[];
}

/** INSERT / UPDATE / DELETE : le résumé (affectedRows, insertId…). */
export async function run(db: Db, sql: string, params: unknown[] = []): Promise<ResultSetHeader> {
  const [result] = await db.query<ResultSetHeader>(sql, params);
  return result;
}

/** Une violation d'unicité (e-mail ou téléphone déjà pris, demande déjà faite…). */
export const isDuplicate = (error: unknown): boolean => (error as { code?: string })?.code === 'ER_DUP_ENTRY';
