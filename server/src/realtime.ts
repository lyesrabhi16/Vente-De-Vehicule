import type { Server as HttpServer } from 'node:http';
import { Server, type Socket } from 'socket.io';
import { userFromToken, type AuthUser } from './auth';
import type { Config } from './config';
import { run, type Db } from './db';
import { ApiError, parseBody } from './http';
import type { Logger } from './log';
import { messageSchema, saveMessage } from './routes/messages';

const room = (idClient: number): string => `user:${idClient}`;

/** Au plus 30 messages en 10 secondes par connexion. */
const WINDOW_MS = 10_000;
const MAX_PER_WINDOW = 30;

/**
 * Le temps réel (Socket.IO) de la messagerie. Contrairement à l'ancien serveur, on ne croit pas l'identifiant que le client
 * annonce (« init ») : l'identité vient du jeton présenté à la connexion, et l'expéditeur d'un message est toujours celui-là.
 */
export function attachRealtime(http: HttpServer, deps: { db: Db; config: Config; log: Logger }): Server {
  const { db, config, log } = deps;
  const io = new Server(http, { maxHttpBufferSize: 64 * 1024, serveClient: false });

  io.use((socket, next) => {
    const auth = socket.handshake.auth as { token?: unknown } | undefined;
    const token = typeof auth?.token === 'string' ? auth.token : '';
    if (!token) return next(new Error('unauthorized'));
    userFromToken(db, config, token).then(
      (user) => {
        if (!user) return next(new Error('unauthorized'));
        socket.data.user = user;
        next();
      },
      (error) => {
        log.error(`socket auth : ${(error as Error).message}`);
        next(new Error('unavailable'));
      },
    );
  });

  io.on('connection', (socket: Socket) => {
    const user = socket.data.user as AuthUser;
    void socket.join(room(user.idClient));
    let sent: number[] = [];

    socket.emit('connected');
    // L'application répond à « request-init » par « init » avec son identifiant : on l'accepte et on l'ignore.
    socket.emit('request-init');
    socket.on('init', () => {});
    socket.on('client-message', () => {});

    socket.on('message', (payload: unknown) => {
      const at = Date.now();
      sent = sent.filter((t) => at - t < WINDOW_MS);
      if (sent.length >= MAX_PER_WINDOW) {
        socket.emit('message-error', { error: 'Trop de messages, patientez un instant' });
        return;
      }
      sent.push(at);

      (async () => {
        const data = parseBody(messageSchema, normalize(payload));
        const saved = await saveMessage(db, { idClient_sender: user.idClient, idClient_reciever: data.idClient_reciever, contenuMessage: data.contenuMessage, date: data.date });
        socket.emit('message-sent', saved);
        io.to(room(saved.idClient_reciever)).emit('message', saved, saved.idMessage);
      })().catch((error: unknown) => {
        if (error instanceof ApiError) socket.emit('message-error', { error: error.message });
        else {
          log.error(`socket message : ${(error as Error).stack ?? String(error)}`);
          socket.emit('message-error', { error: 'Erreur interne' });
        }
      });
    });

    // Le destinataire confirme la réception : seul lui peut marquer le message.
    socket.on('message-received', (idMessage: unknown) => {
      const n = Number(idMessage);
      if (!Number.isInteger(n) || n <= 0) return;
      run(db, "UPDATE message SET etatMessage = 'received' WHERE idMessage = ? AND idClient_reciever = ?", [n, user.idClient]).catch((error: Error) =>
        log.error(`socket message-received : ${error.message}`),
      );
    });
  });

  return io;
}

/** Le client Java envoie un objet JSON ; un client plus ancien pouvait envoyer sa représentation texte. */
function normalize(payload: unknown): unknown {
  if (typeof payload === 'string') {
    try {
      return JSON.parse(payload);
    } catch {
      return {};
    }
  }
  return payload;
}

