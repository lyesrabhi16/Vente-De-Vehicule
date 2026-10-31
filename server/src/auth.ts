import argon2 from 'argon2';
import type { NextFunction, Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import type { Config } from './config';
import { rows, type Db } from './db';
import { forbidden, unauthorized } from './http';

export type Role = 'client' | 'admin';

export interface AuthUser {
  idClient: number;
  role: Role;
}

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Express {
    interface Request {
      /** Renseigné par `authenticate` : qui fait la requête, d'après son jeton (jamais d'après le corps). */
      user?: AuthUser;
    }
  }
}

const ISSUER = 'miniprojet';

export const hashPassword = (password: string): Promise<string> => argon2.hash(password, { type: argon2.argon2id });

// Un utilisateur inconnu doit coûter le même temps qu'un mot de passe faux, pour que le temps de réponse ne dise pas
// quels comptes existent.
let dummyHash: Promise<string> | undefined;
export async function verifyPassword(hash: string | undefined, password: string): Promise<boolean> {
  if (hash === undefined) {
    dummyHash ??= hashPassword('mot de passe qui ne correspond à personne');
    await argon2.verify(await dummyHash, password).catch(() => false);
    return false;
  }
  try {
    return await argon2.verify(hash, password);
  } catch {
    return false;
  }
}

export function signToken(config: Config, idClient: number): string {
  return jwt.sign({}, config.jwt.secret, {
    algorithm: 'HS256',
    issuer: ISSUER,
    subject: String(idClient),
    expiresIn: config.jwt.ttlSeconds,
  });
}

/** Le jeton, s'il est valide et si le compte existe encore : le rôle est relu en base, pas lu dans le jeton. */
export async function userFromToken(db: Db, config: Config, token: string): Promise<AuthUser | null> {
  let subject: string | undefined;
  try {
    const payload = jwt.verify(token, config.jwt.secret, { algorithms: ['HS256'], issuer: ISSUER });
    subject = typeof payload === 'object' ? payload.sub : undefined;
  } catch {
    return null;
  }
  const id = Number(subject);
  if (!Number.isInteger(id) || id <= 0) return null;
  const [user] = await rows<{ idClient: number; role: Role }>(db, 'SELECT idClient, role FROM client WHERE idClient = ?', [id]);
  return user ? { idClient: user.idClient, role: user.role } : null;
}

export function bearerToken(req: Request): string | null {
  const header = req.headers.authorization;
  if (!header) return null;
  const [scheme, token] = header.split(' ');
  return scheme?.toLowerCase() === 'bearer' && token ? token : null;
}

/** Exige un jeton valide. */
export const authenticate =
  (db: Db, config: Config) =>
  (req: Request, _res: Response, next: NextFunction): void => {
    const token = bearerToken(req);
    if (!token) return next(unauthorized());
    userFromToken(db, config, token)
      .then((user) => {
        if (!user) return next(unauthorized('Session invalide ou expirée'));
        req.user = user;
        next();
      })
      .catch(next);
  };

/** Le jeton si on en présente un (et qu'il est mauvais : refusé), sinon la requête reste anonyme. */
export const optionalAuthenticate =
  (db: Db, config: Config) =>
  (req: Request, res: Response, next: NextFunction): void => {
    if (!bearerToken(req)) return next();
    authenticate(db, config)(req, res, next);
  };

export const isAdmin = (user: AuthUser | undefined): boolean => user?.role === 'admin';

/** Soi-même, ou un administrateur. */
export const canActAs = (user: AuthUser, idClient: number): boolean => user.role === 'admin' || user.idClient === idClient;

export function requireSelfOrAdmin(user: AuthUser | undefined, idClient: number): AuthUser {
  if (!user) throw unauthorized();
  if (!canActAs(user, idClient)) throw forbidden();
  return user;
}

export function requireUser(req: Request): AuthUser {
  if (!req.user) throw unauthorized();
  return req.user;
}
