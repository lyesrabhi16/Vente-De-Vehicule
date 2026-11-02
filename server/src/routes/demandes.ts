import { Router } from 'express';
import { z } from 'zod';
import { authenticate, canActAs, requireUser, type AuthUser } from '../auth';
import { isDuplicate, rows, run } from '../db';
import { businessError, forbidden, handler, parseBody } from '../http';
import { filterToSql } from './annonces';
import { datetime, id, text, type Ctx } from './shared';

/**
 * Une demande d'un client sur l'annonce d'un vendeur : une réservation (du … au …) ou un rendez-vous (à telle date). Les deux
 * ont la même vie : le client la fait (« Pending »), le vendeur l'accepte ou la refuse (« Accepted », « Rejected »), le client
 * peut changer la date et le lieu tant qu'il veut, jamais l'état.
 */
export interface DemandeKind {
  /** La route d'une demande : /reservation, et /reservation/add… */
  route: string;
  /** La route de la liste filtrée. */
  listRoute: string;
  table: 'reservation' | 'rendezvous';
  dates: string[];
  place: string;
  state: string;
  /** Une date de fin après le début, quand il y en a deux. */
  ordered?: boolean;
}

export const RESERVATION: DemandeKind = {
  route: '/reservation',
  listRoute: '/reservations',
  table: 'reservation',
  dates: ['dateDebut', 'dateFin'],
  place: 'lieuReservation',
  state: 'etatReservation',
  ordered: true,
};

export const RENDEZVOUS: DemandeKind = {
  route: '/rendezvous',
  listRoute: '/rendezvous/all',
  table: 'rendezvous',
  dates: ['dateRendezVous'],
  place: 'lieuRendezVous',
  state: 'etatRendezVous',
};

const STATES = ['Pending', 'Accepted', 'Rejected'] as const;

type Row = Record<string, string | number> & { idClient: number; idAnnonce: number; idVendeur: number };

export function demandeRoutes(ctx: Ctx, kind: DemandeKind) {
  const router = Router();
  const { db, config } = ctx;
  const auth = authenticate(db, config);
  const { table, dates, place, state } = kind;
  const filterable = ['idClient', 'idAnnonce', ...dates, place, state] as const;

  const keySchema = z.object({ idClient: id, idAnnonce: id });
  const dateFields = Object.fromEntries(dates.map((d) => [d, datetime]));
  const baseFields = { ...dateFields, [place]: text(200) };
  const addSchema = z.object({ ...keySchema.shape, ...baseFields, [state]: z.enum(STATES).optional() });
  const updateSchema = z.object({ ...keySchema.shape, ...baseFields, [state]: z.enum(STATES) });

  function checkOrder(values: Record<string, unknown>): void {
    if (kind.ordered && String(values[dates[1] as string]) <= String(values[dates[0] as string])) {
      throw businessError(`invalid fields : [${dates[1]}]`);
    }
  }

  async function find(idClient: number, idAnnonce: number): Promise<Row | undefined> {
    const [row] = await rows<Row>(
      db,
      `SELECT d.*, a.idClient AS idVendeur FROM ${table} d JOIN annonce a ON a.idAnnonce = d.idAnnonce WHERE d.idClient = ? AND d.idAnnonce = ?`,
      [idClient, idAnnonce],
    );
    return row;
  }

  /** Le client de la demande, son vendeur, ou un administrateur. */
  const concerned = (user: AuthUser, row: { idClient: number; idVendeur: number }): boolean =>
    user.role === 'admin' || user.idClient === row.idClient || user.idClient === row.idVendeur;

  router.post(
    kind.listRoute,
    auth,
    handler(async (req, res) => {
      const me = requireUser(req);
      const { where, params } = filterToSql(req.body?.filterObj, filterable, 'd.');
      // le client voit ses demandes, le vendeur celles de ses annonces, l'administrateur toutes
      const mine = me.role === 'admin' ? '' : '(d.idClient = ? OR a.idClient = ?)';
      const conditions = [mine, where].filter(Boolean).join(' AND ');
      const result = await rows(
        db,
        `SELECT d.* FROM ${table} d JOIN annonce a ON a.idAnnonce = d.idAnnonce ${conditions ? `WHERE ${conditions}` : ''} ORDER BY d.idAnnonce DESC, d.idClient LIMIT 500`,
        [...(mine ? [me.idClient, me.idClient] : []), ...params],
      );
      res.json({ result });
    }),
  );

  router.post(
    kind.route,
    auth,
    handler(async (req, res) => {
      const { idClient, idAnnonce } = parseBody(keySchema, req.body);
      const me = requireUser(req);
      const row = await find(idClient, idAnnonce);
      // pas le droit de la voir, ou elle n'existe pas : la même réponse, qui ne dit pas laquelle
      if (!row || !concerned(me, row)) {
        res.json({ result: [] });
        return;
      }
      const { idVendeur: _seller, ...demande } = row;
      res.json({ result: [demande] });
    }),
  );

  router.post(
    `${kind.route}/add`,
    auth,
    handler(async (req, res) => {
      const data = parseBody(addSchema, req.body) as Record<string, string | number> & { idClient: number; idAnnonce: number };
      const me = requireUser(req);
      if (!canActAs(me, data.idClient)) throw forbidden();
      checkOrder(data);
      const [annonce] = await rows<{ idClient: number }>(db, 'SELECT idClient FROM annonce WHERE idAnnonce = ?', [data.idAnnonce]);
      if (!annonce) throw businessError('Annonce introuvable');
      if (annonce.idClient === data.idClient) throw businessError('Vous ne pouvez pas faire une demande sur votre propre annonce');
      // l'état d'une nouvelle demande est « Pending » : c'est au vendeur de l'accepter
      const created = { ...data, [state]: me.role === 'admin' ? (data[state] ?? 'Pending') : 'Pending' };
      try {
        const result = await run(db, `INSERT INTO ${table} SET ?`, [created]);
        res.json({ result });
      } catch (error) {
        if (isDuplicate(error)) throw businessError('Une demande existe déjà pour cette annonce');
        throw error;
      }
    }),
  );

  router.post(
    `${kind.route}/update`,
    auth,
    handler(async (req, res) => {
      const data = parseBody(updateSchema, req.body) as Record<string, string | number> & { idClient: number; idAnnonce: number };
      const me = requireUser(req);
      checkOrder(data);
      const current = await find(data.idClient, data.idAnnonce);
      if (!current) throw businessError('Demande introuvable');
      if (!concerned(me, current)) throw forbidden();

      const changed = (column: string) => String(data[column]) !== String(current[column]);
      const asClient = me.role !== 'admin' && me.idClient === current.idClient;
      const asSeller = me.role !== 'admin' && me.idClient === current.idVendeur;
      // le client change la date et le lieu, le vendeur l'état ; chacun peut renvoyer le reste tel quel
      const forbiddenColumns = asClient ? [state].filter(changed) : asSeller ? [...dates, place].filter(changed) : [];
      if (forbiddenColumns.length > 0) throw forbidden(`Modification non autorisée : ${forbiddenColumns.join(', ')}`);

      const { idClient, idAnnonce, ...values } = data;
      const result = await run(db, `UPDATE ${table} SET ? WHERE idClient = ? AND idAnnonce = ?`, [values, idClient, idAnnonce]);
      res.json({ result });
    }),
  );

  router.post(
    `${kind.route}/remove`,
    auth,
    handler(async (req, res) => {
      const { idClient, idAnnonce } = parseBody(keySchema, req.body);
      const me = requireUser(req);
      const current = await find(idClient, idAnnonce);
      if (!current) throw businessError('Demande introuvable');
      if (!concerned(me, current)) throw forbidden();
      const result = await run(db, `DELETE FROM ${table} WHERE idClient = ? AND idAnnonce = ?`, [idClient, idAnnonce]);
      res.json({ result });
    }),
  );

  return router;
}
