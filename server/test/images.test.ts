import { readdir } from 'node:fs/promises';
import path from 'node:path';
import { afterAll, beforeAll, describe, expect, it } from 'vitest';
import { makeAdmin, postAnnonce, signup, startServer, type Account, type TestServer } from './helpers';

// les premiers octets d'un vrai fichier de chaque format, suffisants pour la détection
const JPEG = Buffer.from([0xff, 0xd8, 0xff, 0xe0, 0, 0x10, 0x4a, 0x46, 0x49, 0x46, 0, 1]);
const PNG = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a, 0, 0, 0, 0x0d]);

let server: TestServer;
let owner: Account;
let other: Account;
let annonce: number;

beforeAll(async () => {
  server = await startServer();
  owner = await signup(server);
  other = await signup(server);
  annonce = await postAnnonce(server, owner);
});
afterAll(() => server.close());

const upload = (who: Account | null, body: Record<string, string>) => {
  const req = server.api.post('/upload/image');
  if (who) req.set(who.as);
  return req.type('form').send(body);
};
const avatar = (account: Account, data: Buffer, extra: Record<string, string> = {}) => ({ userID: String(account.idClient), imgB64: data.toString('base64'), format: 'jpeg', type: 'AVATAR', ...extra });

const files = async (folder: string) => readdir(path.join(server.imagesDir, folder)).catch(() => [] as string[]);

describe('envoi', () => {
  it('enregistre un avatar et répond le texte que l’application attend', async () => {
    const res = await upload(owner, avatar(owner, JPEG));
    expect(res.status).toBe(200);
    expect(res.text).toBe('Image saved successfully');
    expect(await files('client')).toEqual([`imageClient-[${owner.idClient}].jpeg`]);
  });

  it('exige un compte, et refuse de déposer chez un autre', async () => {
    expect((await upload(null, avatar(owner, JPEG))).status).toBe(401);
    expect((await upload(other, avatar(owner, JPEG))).status).toBe(403);
    expect((await upload(other, { userID: String(annonce), imgB64: JPEG.toString('base64'), format: 'jpeg', type: 'ANNONCE', index: '0' })).status).toBe(403);
  });

  it('enregistre les images d’une annonce, y compris par un administrateur', async () => {
    const res = await upload(owner, { userID: String(annonce), imgB64: PNG.toString('base64'), format: 'png', type: 'ANNONCE', index: '2' });
    expect(res.text).toBe('Image saved successfully');
    expect(await files('annonce')).toContain(`imageAnnonce-[${annonce}]-[2].png`);
    const admin = await signup(server);
    await makeAdmin(server, admin);
    expect((await upload(admin, { userID: String(annonce), imgB64: JPEG.toString('base64'), format: 'jpeg', type: 'ANNONCE', index: '3' })).text).toBe('Image saved successfully');
  });

  it('ne fabrique jamais un chemin à partir de la requête (traversée de répertoire)', async () => {
    const before = await files('client');
    for (const userID of ['../../etc/passwd', '..%2f..%2fx', '1/../../x']) {
      const res = await upload(owner, avatar(owner, JPEG, { userID }));
      expect(res.body.error ?? res.text).toBeDefined();
      expect(res.text).not.toBe('Image saved successfully');
    }
    for (const format of ['jpeg/../../x', '../x', 'php']) {
      const res = await upload(owner, avatar(owner, JPEG, { format }));
      expect(res.text).not.toBe('Image saved successfully');
    }
    expect(await files('client')).toEqual(before);
  });

  it('refuse un fichier qui n’est pas l’image annoncée', async () => {
    const script = Buffer.from('<?php system($_GET["c"]); ?>');
    expect((await upload(owner, avatar(owner, script))).body.error).toMatch(/pas une image/);
    expect((await upload(owner, avatar(owner, PNG))).body.error).toMatch(/pas une image/); // un PNG déclaré jpeg
  });

  it('refuse une image trop grande, sans la lire en entier', async () => {
    const huge = Buffer.concat([JPEG, Buffer.alloc(server.config.maxImageBytes + 10)]);
    const res = await upload(owner, avatar(owner, huge));
    expect([200, 413]).toContain(res.status);
    if (res.status === 200) expect(res.body.error).toMatch(/trop grande/);
  });

  it('refuse un très gros corps sur les autres routes', async () => {
    const res = await server.api.post('/signin').type('form').send({ email: 'a@exemple.fr', password: 'x'.repeat(300_000) });
    expect(res.status).toBe(413);
  });
});

describe('lecture', () => {
  it('sert une image d’annonce sans compte', async () => {
    const res = await server.api.post('/download/image').type('form').send({ imgName: `imageAnnonce-[${annonce}]-[2].png` });
    expect(Buffer.from(res.body.imgB64, 'base64').equals(PNG)).toBe(true);
  });

  it('réserve les avatars aux comptes connectés', async () => {
    const anonymous = await server.api.post('/download/image').type('form').send({ imgName: `imageClient-[${owner.idClient}].jpeg` });
    expect(anonymous.status).toBe(401);
    const res = await server.api.post('/download/image').set(other.as).type('form').send({ imgName: `imageClient-[${owner.idClient}].jpeg` });
    expect(Buffer.from(res.body.imgB64, 'base64').equals(JPEG)).toBe(true);
  });

  it('ne lit que des noms d’image connus', async () => {
    for (const imgName of ['../../../../etc/passwd', '..\\..\\secret', `imageClient-[${owner.idClient}].jpeg/../../x`, 'package.json', 'imageClient-[1].svg']) {
      const res = await server.api.post('/download/image').set(owner.as).type('form').send({ imgName });
      expect(res.body.imgB64).toBeUndefined();
      expect(res.body.error).toBe('image not found.');
    }
  });

  it('supprime les images avec l’annonce et avec le compte', async () => {
    await server.api.post('/annonce/remove').set(owner.as).type('form').send({ idAnnonce: String(annonce) });
    expect((await files('annonce')).filter((f) => f.includes(`[${annonce}]`))).toEqual([]);
    await server.api.post('/user/remove').set(owner.as).type('form').send({ idClient: owner.idClient });
    expect(await files('client')).toEqual([]);
  });
});
