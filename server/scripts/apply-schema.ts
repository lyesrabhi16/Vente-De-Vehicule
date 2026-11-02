import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { createConnection } from 'mysql2/promise';
import { loadConfig } from '../src/config';

/** Applique db/schema.sql (idempotent) sur la base de la configuration. */
async function main(): Promise<void> {
  const { db } = loadConfig();
  const sql = await readFile(path.join(__dirname, '..', 'db', 'schema.sql'), 'utf8');
  const connection = await createConnection({ ...db, multipleStatements: true });
  try {
    await connection.query(sql);
    console.log(`Schéma appliqué sur ${db.host}:${db.port}/${db.database}`);
  } finally {
    await connection.end();
  }
}

main().catch((error: Error) => {
  console.error(error.message);
  process.exit(1);
});
