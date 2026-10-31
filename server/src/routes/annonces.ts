import { Router } from 'express';
import { z } from 'zod';
import { authenticate, canActAs, optionalAuthenticate, requireUser } from '../auth';
import { rows, run } from '../db';
import { businessError, forbidden, handler, parseBody } from '../http';
import type { Images } from '../images';
import { id, text, type Ctx } from './shared';

const thisYear = new Date().getFullYear();

const fields = {
  titre: text(150),
  description: text(5000),
  typeVehicule: text(50),
  marqueVehicule: text(50),
  modeleVehicule: text(50),
  couleurVehicule: text(30),
  transmissionVehicule: text(30),
  kilometrageVehicule: z.coerce.number().int().min(0).max(5_000_000),
  anneeVehicule: z.coerce.number().int().min(1886).max(thisYear + 1),
  moteurVehicule: text(50),
  energieVehicule: text(30),
  prixVehicule: z.coerce.number().min(0).max(9_999_999_999),
};

/** Les colonnes sur lesquelles l'application filtre : une liste fermée, jamais un nom de colonne venu de la requête. */
export const ANNONCE_COLUMNS = ['idAnnonce', 'idClient', ...Object.keys(fields)] as const;

const MAX_ROWS = 500;

interface AnnonceRow {
  idAnnonce: number;
  idClient: number;
  kilometrageVehicule: number;
  prixVehicule: string;
  [column: string]: unknown;
}

/** L'application lit le kilométrage et le prix comme du texte. */
export const presentAnnonce = (row: AnnonceRow) => ({ ...row, kilometrageVehicule: String(row.kilometrageVehicule), prixVehicule: String(row.prixVehicule) });

/** `{ "marqueVehicule": "Peugeot" }` (en JSON, dans un champ de formulaire) devient `WHERE marqueVehicule = ?` : colonnes d'une liste fermée, valeurs en paramètres. */
export function filterToSql(raw: unknown, allowed: readonly string[], prefix = ''): { where: string; params: unknown[] } {
  let filter: unknown = {};
  if (typeof raw === 'string' && raw.trim() !== '') {
    try {
      filter = JSON.parse(raw);
    } catch {
      throw businessError('invalid fields : [filterObj]');
    }
  } else if (raw && typeof raw === 'object') {
    filter = raw;
  }
  if (!filter || typeof filter !== 'object' || Array.isArray(filter)) throw businessError('invalid fields : [filterObj]');

  const clauses: string[] = [];
  const params: unknown[] = [];
  const unknown: string[] = [];
  for (const [column, value] of Object.entries(filter)) {
    if (!allowed.includes(column)) {
      unknown.push(column);
      continue;
    }
    if (typeof value !== 'string' && typeof value !== 'number') {
      unknown.push(column);
      continue;
    }
    clauses.push(`${prefix}${column} = ?`);
    params.push(value);
  }
  if (unknown.length > 0) throw businessError(`invalid fields : [${unknown.join(', ')}]`);
  return { where: clauses.join(' AND '), params };
}

export function annonceRoutes(ctx: Ctx, images: Images) {
  const router = Router();
  const { db, config } = ctx;
  const auth = authenticate(db, config);
  // Parcourir les annonces ne demande pas de compte (l'application le permet) ; un jeton, s'il est présenté, doit être bon.
  const anonymous = optionalAuthenticate(db, config);

  const oneSchema = z.object({ idAnnonce: id });
  const ofUserSchema = z.object({ idClient: id });
  const addSchema = z.object({ idClient: id.optional(), ...fields });
  const updateSchema = z.object({ idAnnonce: id, idClient: id.optional(), ...fields });

  router.post(
    '/annonce',
    anonymous,
    handler(async (req, res) => {
      const { idAnnonce } = parseBody(oneSchema, req.body);
      const result = await rows<AnnonceRow>(db, 'SELECT * FROM annonce WHERE idAnnonce = ?', [idAnnonce]);
      res.json({ result: result.map(presentAnnonce) });
    }),
  );

  router.post(
    '/annonces',
    anonymous,
    handler(async (req, res) => {
      const { where, params } = filterToSql(req.body?.filterObj, ANNONCE_COLUMNS);
      const result = await rows<AnnonceRow>(db, `SELECT * FROM annonce ${where ? `WHERE ${where}` : ''} ORDER BY idAnnonce DESC LIMIT ${MAX_ROWS}`, params);
      res.json({ result: result.map(presentAnnonce) });
    }),
  );

  router.post(
    '/annonces/user',
    anonymous,
    handler(async (req, res) => {
      const { idClient } = parseBody(ofUserSchema, req.body);
      const result = await rows<AnnonceRow>(db, 'SELECT * FROM annonce WHERE idClient = ? ORDER BY idAnnonce DESC LIMIT ?', [idClient, MAX_ROWS]);
      res.json({ result: result.map(presentAnnonce) });
    }),
  );

  router.post(
    '/annonce/add',
    auth,
    handler(async (req, res) => {
      const data = parseBody(addSchema, req.body);
      const me = requireUser(req);
      // le vendeur est celui qui est connecté ; seul un administrateur publie au nom d'un autre
      const owner = data.idClient ?? me.idClient;
      if (!canActAs(me, owner)) throw forbidden();
      const { idClient: _ignored, ...annonce } = data;
      const header = await run(db, 'INSERT INTO annonce SET ?', [{ idClient: owner, ...annonce }]).catch((error: { code?: string }) => {
        if (error.code === 'ER_NO_REFERENCED_ROW_2') throw businessError('Vendeur introuvable');
        throw error;
      });
      res.json({ result: header.insertId });
    }),
  );

  router.post(
    '/annonce/update',
    auth,
    handler(async (req, res) => {
      const { idAnnonce, idClient: _ignored, ...annonce } = parseBody(updateSchema, req.body);
      const me = requireUser(req);
      const [current] = await rows<{ idClient: number }>(db, 'SELECT idClient FROM annonce WHERE idAnnonce = ?', [idAnnonce]);
      if (!current) throw businessError('Annonce introuvable');
      if (!canActAs(me, current.idClient)) throw forbidden(); // le vendeur ne change pas : on ne transfère pas une annonce
      await run(db, 'UPDATE annonce SET ? WHERE idAnnonce = ?', [annonce, idAnnonce]);
      res.json({ result: idAnnonce });
    }),
  );

  router.post(
    '/annonce/remove',
    auth,
    handler(async (req, res) => {
      const { idAnnonce } = parseBody(oneSchema, req.body);
      const me = requireUser(req);
      const [current] = await rows<{ idClient: number }>(db, 'SELECT idClient FROM annonce WHERE idAnnonce = ?', [idAnnonce]);
      if (!current) throw businessError('Annonce introuvable');
      if (!canActAs(me, current.idClient)) throw forbidden();
      const result = await run(db, 'DELETE FROM annonce WHERE idAnnonce = ?', [idAnnonce]);
      await images.removeAnnonceImages(idAnnonce);
      res.json({ result });
    }),
  );

  return router;
}
