import { Router, type RequestHandler } from 'express';
import { z } from 'zod';
import { authenticate, hashPassword, optionalAuthenticate, requireSelfOrAdmin, signToken, verifyPassword } from '../auth';
import { isDuplicate, rows, run } from '../db';
import { businessError, handler, parseBody } from '../http';
import type { Images } from '../images';
import { CLIENT_COLUMNS, email, id, phone, presentClient, text, type ClientRow, type Ctx } from './shared';

const profile = {
  nomClient: text(100),
  prenomClient: text(100),
  ageClient: z.coerce.number().int().min(16).max(120),
  email,
  numTel: phone,
};

const signupSchema = z.object({ ...profile, password: z.string().min(8).max(128) });
const signinSchema = z.object({ email: email.optional(), numTel: phone.optional(), password: z.string().min(1).max(128) });
const userSchema = z.object({ idClient: id });
const updateSchema = z.object({ idClient: id, ...profile });

const TAKEN = 'Un compte existe déjà avec cet e-mail ou ce numéro de téléphone';

export function accountRoutes(ctx: Ctx, images: Images, limiters: { signup: RequestHandler; signin: RequestHandler }) {
  const router = Router();
  const { db, config } = ctx;
  const auth = authenticate(db, config);

  router.post(
    '/signup',
    limiters.signup,
    handler(async (req, res) => {
      const data = parseBody(signupSchema, req.body);
      let insertId: number;
      try {
        const header = await run(
          db,
          'INSERT INTO client (nomClient, prenomClient, ageClient, email, numTel, password) VALUES (?, ?, ?, ?, ?, ?)',
          [data.nomClient, data.prenomClient, data.ageClient, data.email, data.numTel, await hashPassword(data.password)],
        );
        insertId = header.insertId;
      } catch (error) {
        if (isDuplicate(error)) throw businessError(TAKEN);
        throw error;
      }
      const [row] = await rows<ClientRow>(db, `SELECT ${CLIENT_COLUMNS} FROM client WHERE idClient = ?`, [insertId]);
      res.json(presentClient(row as ClientRow, { token: signToken(config, insertId) }));
    }),
  );

  router.post(
    '/signin',
    limiters.signin,
    handler(async (req, res) => {
      const data = parseBody(signinSchema, req.body);
      if (!data.email && !data.numTel) throw businessError('missing required fields : [email/numTel]');
      const [row] = data.email
        ? await rows<ClientRow & { password: string }>(db, `SELECT ${CLIENT_COLUMNS}, password FROM client WHERE email = ?`, [data.email])
        : await rows<ClientRow & { password: string }>(db, `SELECT ${CLIENT_COLUMNS}, password FROM client WHERE numTel = ?`, [data.numTel]);
      // le même message et le même coût que le compte existe ou non : la réponse ne dit pas lequel des deux était faux
      if (!(await verifyPassword(row?.password, data.password)) || !row) {
        res.locals.authFailed = true; // compte comme un échec pour le limiteur de tentatives
        throw businessError('Identifiants invalides');
      }
      res.json(presentClient(row, { token: signToken(config, row.idClient) }));
    }),
  );

  router.post(
    '/user',
    optionalAuthenticate(db, config),
    handler(async (req, res) => {
      const { idClient } = parseBody(userSchema, req.body);
      const me = req.user;
      const [row] = await rows<ClientRow>(db, `SELECT ${CLIENT_COLUMNS} FROM client WHERE idClient = ?`, [idClient]);
      if (!row) throw businessError('Utilisateur introuvable');
      // Qui parcourt les annonces sans compte voit le nom du vendeur ; ses coordonnées, pour le contacter, demandent un compte ;
      // son âge n'est montré qu'à lui (l'application lit les trois : d'où « vide » et 0, pas l'absence).
      const own = me !== undefined && (me.role === 'admin' || me.idClient === idClient);
      const visible = own ? row : { ...row, ageClient: 0, ...(me ? {} : { email: '', numTel: '' }) };
      res.json({ user: [presentClient(visible)] });
    }),
  );

  router.post(
    '/user/update',
    auth,
    handler(async (req, res) => {
      const data = parseBody(updateSchema, req.body);
      requireSelfOrAdmin(req.user, data.idClient);
      try {
        const result = await run(
          db,
          'UPDATE client SET nomClient = ?, prenomClient = ?, ageClient = ?, email = ?, numTel = ? WHERE idClient = ?',
          [data.nomClient, data.prenomClient, data.ageClient, data.email, data.numTel, data.idClient],
        );
        res.json({ result });
      } catch (error) {
        if (isDuplicate(error)) throw businessError(TAKEN);
        throw error;
      }
    }),
  );

  router.post(
    '/user/remove',
    auth,
    handler(async (req, res) => {
      const { idClient } = parseBody(userSchema, req.body);
      requireSelfOrAdmin(req.user, idClient);
      const annonces = await rows<{ idAnnonce: number }>(db, 'SELECT idAnnonce FROM annonce WHERE idClient = ?', [idClient]);
      const result = await run(db, 'DELETE FROM client WHERE idClient = ?', [idClient]); // ses annonces, messages et demandes suivent (ON DELETE CASCADE)
      await images.removeAvatar(idClient);
      for (const { idAnnonce } of annonces) await images.removeAnnonceImages(idAnnonce);
      res.json({ user: result });
    }),
  );

  return router;
}
