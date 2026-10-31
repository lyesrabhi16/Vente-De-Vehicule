import { Router } from 'express';
import { z } from 'zod';
import { authenticate, canActAs, requireUser } from '../auth';
import { rows, run, type Db } from '../db';
import { businessError, forbidden, handler, parseBody } from '../http';
import { datetime, id, text, type Ctx } from './shared';

export const MAX_MESSAGE_LENGTH = 2000;

export interface NewMessage {
  idClient_sender: number;
  idClient_reciever: number; // sic : l'orthographe de la base et de l'application
  contenuMessage: string;
  date?: string;
}

export interface SavedMessage extends NewMessage {
  idMessage: number;
  etatMessage: 'sent';
  date: string;
}

const now = (): string => new Date().toISOString().slice(0, 19).replace('T', ' ');

/** Enregistre un message (route HTTP et temps réel passent ici) ; le destinataire doit exister. */
export async function saveMessage(db: Db, message: NewMessage): Promise<SavedMessage> {
  if (message.idClient_sender === message.idClient_reciever) throw businessError('Vous ne pouvez pas vous écrire à vous-même');
  const [receiver] = await rows(db, 'SELECT idClient FROM client WHERE idClient = ?', [message.idClient_reciever]);
  if (!receiver) throw businessError('Destinataire introuvable');
  const date = message.date ?? now();
  const header = await run(db, 'INSERT INTO message (idClient_sender, idClient_reciever, contenuMessage, etatMessage, date) VALUES (?, ?, ?, ?, ?)', [
    message.idClient_sender,
    message.idClient_reciever,
    message.contenuMessage,
    'sent',
    date,
  ]);
  return { ...message, idMessage: header.insertId, etatMessage: 'sent', date };
}

export const messageSchema = z.object({
  idClient_reciever: id,
  contenuMessage: text(MAX_MESSAGE_LENGTH),
  date: datetime.optional(),
});

const chatsSchema = z.object({ idClient: id });
const conversationSchema = z.object({ idClient1: id, idClient2: id });
const addSchema = z.object({ idClient_sender: id.optional(), ...messageSchema.shape, etatMessage: z.string().optional() });

export function messageRoutes(ctx: Ctx) {
  const router = Router();
  const { db, config } = ctx;
  const auth = authenticate(db, config);

  // Les conversations d'un client : chaque message avec les deux participants (la forme que l'application lit).
  router.post(
    '/chats',
    auth,
    handler(async (req, res) => {
      const { idClient } = parseBody(chatsSchema, req.body);
      if (!canActAs(requireUser(req), idClient)) throw forbidden();
      const result = await rows(
        db,
        `SELECT c.idClient, c.nomClient, c.prenomClient, r.*
         FROM client c
         JOIN (SELECT * FROM message WHERE idClient_sender = ? OR idClient_reciever = ?) r
           ON c.idClient = r.idClient_sender OR c.idClient = r.idClient_reciever
         ORDER BY r.date DESC, r.idMessage DESC
         LIMIT 2000`,
        [idClient, idClient],
      );
      res.json({ result });
    }),
  );

  router.post(
    '/messages',
    auth,
    handler(async (req, res) => {
      const { idClient1, idClient2 } = parseBody(conversationSchema, req.body);
      const me = requireUser(req);
      // on ne lit que ses propres conversations
      if (me.role !== 'admin' && me.idClient !== idClient1 && me.idClient !== idClient2) throw forbidden();
      const result = await rows(
        db,
        `SELECT * FROM message
         WHERE (idClient_sender = ? AND idClient_reciever = ?) OR (idClient_sender = ? AND idClient_reciever = ?)
         ORDER BY date ASC, idMessage ASC
         LIMIT 2000`,
        [idClient1, idClient2, idClient2, idClient1],
      );
      res.json({ result });
    }),
  );

  router.post(
    '/messages/add',
    auth,
    handler(async (req, res) => {
      const data = parseBody(addSchema, req.body);
      const me = requireUser(req);
      const sender = data.idClient_sender ?? me.idClient;
      if (!canActAs(me, sender)) throw forbidden(); // on n'écrit pas au nom d'un autre
      const saved = await saveMessage(db, { idClient_sender: sender, idClient_reciever: data.idClient_reciever, contenuMessage: data.contenuMessage, date: data.date });
      res.json({ result: saved });
    }),
  );

  return router;
}
