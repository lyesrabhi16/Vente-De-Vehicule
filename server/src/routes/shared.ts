import { z } from 'zod';
import type { Config } from '../config';
import type { Db } from '../db';
import type { Logger } from '../log';

export interface Ctx {
  db: Db;
  config: Config;
  log: Logger;
}

export const text = (max: number) => z.string().trim().min(1).max(max);

/** Un identifiant : l'application l'envoie en texte. */
export const id = z.coerce.number().int().positive().max(2_147_483_647);

/** Un téléphone : chiffres et « + » initial, les espaces, points, tirets et parenthèses sont ôtés (même forme à l'inscription et à la connexion). */
export const phone = z
  .string()
  .trim()
  .transform((s) => s.replace(/[ ().-]/g, ''))
  .pipe(z.string().regex(/^\+?\d{6,20}$/));

export const email = z.string().trim().toLowerCase().pipe(z.email().max(255));

/** « 2023-06-01 10:30:00 » (ce que l'application envoie) ou « 2023-06-01T10:30:00.000Z » : la forme de MySQL, ou rien. */
export const datetime = z
  .string()
  .trim()
  .regex(/^\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}(:\d{2})?/)
  .transform((s) => {
    const day = s.slice(0, 10);
    const time = s.slice(11, 19);
    return `${day} ${time.length === 5 ? `${time}:00` : time}`;
  })
  .refine((s) => !Number.isNaN(Date.parse(`${s.replace(' ', 'T')}Z`)), 'date invalide');

export interface ClientRow {
  idClient: number;
  nomClient: string;
  prenomClient: string;
  ageClient: number;
  email: string;
  numTel: string;
  role: 'client' | 'admin';
}

/** Un compte tel que l'application le lit : jamais le mot de passe (ni son hash), et le rôle sous la forme qu'elle connaît. */
export function presentClient(row: ClientRow, extra: Record<string, unknown> = {}) {
  return {
    ...(row.role === 'admin' ? { ADMIN: true } : {}),
    idClient: row.idClient,
    nomClient: row.nomClient,
    prenomClient: row.prenomClient,
    ageClient: row.ageClient,
    email: row.email,
    numTel: row.numTel,
    ...extra,
  };
}

export const CLIENT_COLUMNS = 'idClient, nomClient, prenomClient, ageClient, email, numTel, role';
