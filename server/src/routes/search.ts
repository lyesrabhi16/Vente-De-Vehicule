import { Router } from 'express';
import { z } from 'zod';
import { authenticate, optionalAuthenticate } from '../auth';
import { rows } from '../db';
import { businessError, handler, parseBody } from '../http';
import { type Ctx } from './shared';

/**
 * Pour chaque table cherchable : les colonnes qu'on peut lire, celles où l'on cherche, et si un compte est requis.
 * Rien de ce nom-là ne vient de la requête sans passer par cette liste : pas de nom de table ni de colonne libre.
 */
const TABLES = {
  annonce: {
    readable: ['idAnnonce', 'titre', 'description', 'typeVehicule', 'marqueVehicule', 'modeleVehicule', 'couleurVehicule', 'transmissionVehicule', 'kilometrageVehicule', 'anneeVehicule', 'moteurVehicule', 'energieVehicule', 'prixVehicule', 'idClient'],
    searched: ['titre', 'description', 'typeVehicule', 'marqueVehicule', 'modeleVehicule', 'couleurVehicule', 'transmissionVehicule', 'moteurVehicule', 'energieVehicule', 'anneeVehicule'],
    account: false,
  },
  client: {
    readable: ['idClient', 'nomClient', 'prenomClient', 'email'],
    searched: ['nomClient', 'prenomClient', 'email'],
    account: true,
  },
} as const;

type TableName = keyof typeof TABLES;

const schema = z.object({
  term: z.string().trim().min(1).max(100),
  table: z.enum(Object.keys(TABLES) as [TableName, ...TableName[]]),
  cols: z.string().max(500),
});

const escapeLike = (term: string): string => term.replace(/[\\%_]/g, (c) => `\\${c}`);

export function searchRoutes(ctx: Ctx) {
  const router = Router();
  const { db, config } = ctx;
  const anonymous = optionalAuthenticate(db, config);
  const required = authenticate(db, config);

  router.post(
    '/search',
    (req, res, next) => (req.body?.table === 'client' ? required(req, res, next) : anonymous(req, res, next)),
    handler(async (req, res) => {
      const { term, table, cols } = parseBody(schema, req.body);
      const spec = TABLES[table];
      const wanted = cols.split(',').map((c) => c.trim()).filter(Boolean);
      const unknown = wanted.filter((c) => !(spec.readable as readonly string[]).includes(c));
      if (wanted.length === 0 || unknown.length > 0) throw businessError(`invalid fields : [${unknown.length > 0 ? unknown.join(', ') : 'cols'}]`);

      // chaque mot doit se trouver dans l'une des colonnes cherchées
      const words = [...new Set(term.split(/\s+/).filter(Boolean))].slice(0, 8);
      const perWord = `(${spec.searched.map((c) => `CAST(${c} AS CHAR) LIKE ?`).join(' OR ')})`;
      const params = words.flatMap((w) => spec.searched.map(() => `%${escapeLike(w)}%`));
      const result = await rows(
        db,
        `SELECT ${wanted.map((c) => `\`${c}\``).join(', ')} FROM \`${table}\` WHERE ${words.map(() => perWord).join(' AND ')} LIMIT 200`,
        params,
      );
      res.json({ result });
    }),
  );

  return router;
}
