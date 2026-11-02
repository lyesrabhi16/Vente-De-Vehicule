import express from 'express';
import helmet from 'helmet';
import { rateLimit } from 'express-rate-limit';
import type { Config } from './config';
import type { Db } from './db';
import { errorHandler, notFound } from './http';
import { Images } from './images';
import type { Logger } from './log';
import { accountRoutes } from './routes/accounts';
import { annonceRoutes } from './routes/annonces';
import { RENDEZVOUS, RESERVATION, demandeRoutes } from './routes/demandes';
import { imageRoutes, uploadBodyParsers } from './routes/images';
import { messageRoutes } from './routes/messages';
import { searchRoutes } from './routes/search';
import type { Ctx } from './routes/shared';

export interface RateLimits {
  /** Requêtes par minute et par adresse, toutes routes confondues. */
  global: number;
  /** Connexions échouées par quart d'heure et par adresse. */
  signinFailures: number;
  /** Inscriptions par heure et par adresse. */
  signups: number;
}

export const DEFAULT_LIMITS: RateLimits = { global: 600, signinFailures: 10, signups: 20 };

const tooMany = { error: 'Trop de requêtes, réessayez plus tard' };

export function createApp(options: { config: Config; db: Db; log: Logger; limits?: Partial<RateLimits> }) {
  const { config, db, log } = options;
  const limits = { ...DEFAULT_LIMITS, ...options.limits };
  const ctx: Ctx = { db, config, log };
  const images = new Images(config.imagesDir, config.maxImageBytes);

  const app = express();
  app.disable('x-powered-by');
  app.set('trust proxy', config.trustProxy ? 1 : false);
  app.use(helmet());
  app.use(rateLimit({ windowMs: 60_000, limit: limits.global, standardHeaders: 'draft-7', legacyHeaders: false, message: tooMany }));

  app.get('/', (_req, res) => {
    res.type('text').send('Serveur de vente de véhicules');
  });
  app.get('/health', (_req, res, next) => {
    db.query('SELECT 1').then(() => res.json({ status: 'ok' }), next);
  });

  // L'envoi d'image a ses propres lecteurs de corps (plus gros), posés avant la limite générale, petite.
  app.use('/upload/image', ...uploadBodyParsers(images));
  app.use(express.json({ limit: '100kb' }));
  app.use(express.urlencoded({ extended: false, limit: '100kb' }));

  const signin = rateLimit({
    windowMs: 15 * 60_000,
    limit: limits.signinFailures,
    standardHeaders: 'draft-7',
    legacyHeaders: false,
    message: tooMany,
    // l'application reçoit un échec de connexion en HTTP 200 : la route le marque, et seuls les échecs comptent
    skipSuccessfulRequests: true,
    requestWasSuccessful: (_req, res) => !res.locals.authFailed,
  });
  const signup = rateLimit({ windowMs: 60 * 60_000, limit: limits.signups, standardHeaders: 'draft-7', legacyHeaders: false, message: tooMany });

  app.use(imageRoutes(ctx, images));
  app.use(accountRoutes(ctx, images, { signin, signup }));
  app.use(annonceRoutes(ctx, images));
  app.use(demandeRoutes(ctx, RESERVATION));
  app.use(demandeRoutes(ctx, RENDEZVOUS));
  app.use(messageRoutes(ctx));
  app.use(searchRoutes(ctx));

  app.use(notFound);
  app.use(errorHandler(log));
  return app;
}
