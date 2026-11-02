import type { NextFunction, Request, RequestHandler, Response } from 'express';
import type { ZodType, z } from 'zod';
import type { Logger } from './log';

/**
 * L'application lit toujours une réponse JSON et regarde si elle a une clé `error` : une erreur « métier »
 * (champ manquant, identifiants invalides, e-mail déjà pris…) reste donc une réponse HTTP 200 `{ error }`, comme avant.
 * Ce qui touche à la sécurité a son vrai code : 401 (pas de jeton valide), 403 (pas le droit), 429 (trop de tentatives).
 */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
  }
}

export const businessError = (message: string): ApiError => new ApiError(200, message);
export const unauthorized = (message = 'Authentification requise'): ApiError => new ApiError(401, message);
export const forbidden = (message = 'Action non autorisée'): ApiError => new ApiError(403, message);

type Async = (req: Request, res: Response) => Promise<unknown> | unknown;

/** Une route asynchrone dont les erreurs vont au gestionnaire d'erreurs au lieu de rester sans réponse. */
export const handler =
  (fn: Async): RequestHandler =>
  (req, res, next) => {
    Promise.resolve(fn(req, res)).catch(next);
  };

/**
 * Valide un corps de requête. L'application envoie des formulaires (tout est du texte) : un champ vide vaut un champ absent.
 * Les messages gardent la forme que l'application affichait : « missing required fields : [a, b] ».
 */
export function parseBody<S extends ZodType>(schema: S, body: unknown): z.infer<S> {
  const input: Record<string, unknown> = {};
  if (body && typeof body === 'object') {
    for (const [key, value] of Object.entries(body)) {
      if (value !== '' && value !== null && value !== undefined) input[key] = value;
    }
  }
  const result = schema.safeParse(input);
  if (result.success) return result.data;

  const missing = new Set<string>();
  const invalid = new Set<string>();
  for (const issue of result.error.issues) {
    const key = String(issue.path[0] ?? 'body');
    (key in input ? invalid : missing).add(key);
  }
  if (missing.size > 0) throw businessError(`missing required fields : [${[...missing].join(', ')}]`);
  throw businessError(`invalid fields : [${[...invalid].join(', ')}]`);
}

export const notFound: RequestHandler = (_req, res) => {
  res.status(404).json({ error: 'Route inconnue' });
};

/** Dernier filet : ce qui n'est pas une erreur prévue est journalisé (sans le corps de la requête) et ne fuit pas vers le client. */
export const errorHandler =
  (log: Logger) =>
  (err: unknown, req: Request, res: Response, _next: NextFunction): void => {
    if (err instanceof ApiError) {
      res.status(err.status).json({ error: err.message });
      return;
    }
    const known = err as { type?: string; status?: number };
    if (known.type === 'entity.too.large') {
      res.status(413).json({ error: 'Requête trop volumineuse' });
      return;
    }
    if (known.type === 'entity.parse.failed' || err instanceof SyntaxError) {
      res.status(400).json({ error: 'Corps de requête illisible' });
      return;
    }
    log.error(`${req.method} ${req.path} : ${(err as Error)?.stack ?? String(err)}`);
    res.status(500).json({ error: 'Erreur interne' });
  };
