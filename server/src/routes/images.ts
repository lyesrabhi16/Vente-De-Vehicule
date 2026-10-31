import express, { Router, type RequestHandler } from 'express';
import { z } from 'zod';
import { authenticate, canActAs, optionalAuthenticate, requireUser } from '../auth';
import { rows } from '../db';
import { businessError, forbidden, handler, parseBody, unauthorized } from '../http';
import { EXTENSIONS, detectFormat, extensionMatches, parseImageName, type Images, type ImageRef } from '../images';
import { id, type Ctx } from './shared';

const uploadSchema = z.object({
  userID: id, // l'identifiant du client (avatar) ou de l'annonce, comme l'application l'envoie
  imgB64: z.string().min(8),
  format: z.enum(EXTENSIONS),
  type: z.enum(['AVATAR', 'ANNONCE']),
  index: z.coerce.number().int().min(0).max(9).optional(),
});
const downloadSchema = z.object({ imgName: z.string().max(100) });

/** Seule route à recevoir un gros corps (une image en base64 pèse un tiers de plus) : la limite des autres reste petite. */
export function uploadBodyParsers(images: Images): RequestHandler[] {
  const limit = Math.ceil(images.maxBytes * 1.4) + 4096;
  return [express.json({ limit }), express.urlencoded({ extended: false, limit })];
}

export function imageRoutes(ctx: Ctx, images: Images) {
  const router = Router();
  const { db, config } = ctx;

  router.post(
    '/upload/image',
    authenticate(db, config),
    handler(async (req, res) => {
      const data = parseBody(uploadSchema, req.body);
      const me = requireUser(req);

      let ref: ImageRef;
      if (data.type === 'AVATAR') {
        if (!canActAs(me, data.userID)) throw forbidden();
        ref = { kind: 'avatar', id: data.userID, ext: data.format };
      } else {
        if (data.index === undefined) throw businessError('missing required fields : [index]');
        const [annonce] = await rows<{ idClient: number }>(db, 'SELECT idClient FROM annonce WHERE idAnnonce = ?', [data.userID]);
        if (!annonce) throw businessError('Annonce introuvable');
        if (!canActAs(me, annonce.idClient)) throw forbidden();
        ref = { kind: 'annonce', id: data.userID, index: data.index, ext: data.format };
      }

      const buffer = Buffer.from(data.imgB64, 'base64');
      if (buffer.length === 0 || buffer.length > images.maxBytes) throw businessError(`Image trop grande (${Math.round(images.maxBytes / 1024 / 1024)} Mo au plus)`);
      const detected = detectFormat(buffer);
      if (!detected || !extensionMatches(data.format, detected)) throw businessError("Ce n'est pas une image jpeg, png ou webp");

      await images.save(ref, buffer);
      res.send('Image saved successfully'); // le texte que l'application attend (elle n'en lit que le succès)
    }),
  );

  // Les images des annonces sont publiques (on parcourt les annonces sans compte) ; les avatars ne le sont pas.
  router.post(
    '/download/image',
    optionalAuthenticate(db, config),
    handler(async (req, res) => {
      const { imgName } = parseBody(downloadSchema, req.body);
      const ref = parseImageName(imgName);
      if (!ref) throw businessError('image not found.');
      if (ref.kind === 'avatar' && !req.user) throw unauthorized();
      const data = await images.read(ref);
      if (!data) throw businessError('image not found.');
      res.json({ imgB64: data.toString('base64') });
    }),
  );

  return router;
}
