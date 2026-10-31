import jwt from 'jsonwebtoken';
import { afterAll, beforeAll, describe, expect, it } from 'vitest';
import { makeAdmin, signup, startServer, type TestServer } from './helpers';

let server: TestServer;
beforeAll(async () => {
  server = await startServer();
});
afterAll(() => server.close());

const profile = { nomClient: 'Durand', prenomClient: 'Léa', ageClient: '28', email: 'lea@exemple.fr', numTel: '0612345678', password: 'motdepasse-solide' };

describe('inscription et connexion', () => {
  it("crée un compte, renvoie le profil et un jeton, jamais le mot de passe", async () => {
    const res = await server.api.post('/signup').type('form').send(profile);
    expect(res.status).toBe(200);
    expect(res.body).toMatchObject({ nomClient: 'Durand', prenomClient: 'Léa', email: 'lea@exemple.fr', numTel: '0612345678' });
    expect(res.body.token).toEqual(expect.any(String));
    expect(res.body.ADMIN).toBeUndefined();
    expect(JSON.stringify(res.body)).not.toContain('motdepasse');
    expect(res.body).not.toHaveProperty('password');
  });

  it('stocke un hash argon2id, pas le mot de passe', async () => {
    const [row] = (await server.db.query('SELECT password FROM client WHERE email = ?', ['lea@exemple.fr']))[0] as { password: string }[];
    expect(row?.password).toMatch(/^\$argon2id\$/);
  });

  it('refuse un e-mail ou un téléphone déjà pris, en HTTP 200 + error comme avant', async () => {
    const same = await server.api.post('/signup').type('form').send({ ...profile, numTel: '0699999999' });
    expect(same.status).toBe(200);
    expect(same.body.error).toMatch(/existe déjà/);
    const phone = await server.api.post('/signup').type('form').send({ ...profile, email: 'autre@exemple.fr' });
    expect(phone.body.error).toMatch(/existe déjà/);
  });

  it('range les champs manquants dans le message que l’application affiche', async () => {
    const res = await server.api.post('/signup').type('form').send({ nomClient: 'X' });
    expect(res.status).toBe(200);
    expect(res.body.error).toMatch(/^missing required fields : \[.*prenomClient.*\]$/);
  });

  it('refuse un mot de passe trop court', async () => {
    const res = await server.api.post('/signup').type('form').send({ ...profile, email: 'court@exemple.fr', numTel: '0611111111', password: '1234' });
    expect(res.body.error).toMatch(/password/);
  });

  it('connecte par e-mail ou par téléphone (même formatage toléré)', async () => {
    const byEmail = await server.api.post('/signin').type('form').send({ email: 'LEA@exemple.fr', password: profile.password });
    expect(byEmail.body.token).toEqual(expect.any(String));
    const byPhone = await server.api.post('/signin').type('form').send({ numTel: '06 12 34 56 78', password: profile.password });
    expect(byPhone.body.idClient).toBe(byEmail.body.idClient);
  });

  it('répond pareil pour un mauvais mot de passe et un compte inconnu', async () => {
    const wrong = await server.api.post('/signin').type('form').send({ email: 'lea@exemple.fr', password: 'mauvais-mot-de-passe' });
    const unknown = await server.api.post('/signin').type('form').send({ email: 'inconnu@exemple.fr', password: 'mauvais-mot-de-passe' });
    expect(wrong.body).toEqual({ error: 'Identifiants invalides' });
    expect(unknown.body).toEqual(wrong.body);
  });

  it.each([
    ["' OR '1'='1", 'x'],
    ["lea@exemple.fr' -- ", 'x'],
    ["lea@exemple.fr", "' OR '1'='1"],
  ])("ne se laisse pas injecter (%s / %s)", async (email, password) => {
    const res = await server.api.post('/signin').type('form').send({ email, password });
    expect(res.body.token).toBeUndefined();
    expect(res.body.error).toBeDefined();
  });

  it('un objet à la place d’une chaîne n’est pas pris pour un opérateur', async () => {
    const res = await server.api.post('/signin').send({ email: { $ne: '' }, password: { $ne: '' } });
    expect(res.body.token).toBeUndefined();
  });

  it('ne laisse pas un compte se déclarer administrateur à l’inscription', async () => {
    const res = await server.api.post('/signup').type('form').send({ ...profile, email: 'pirate@exemple.fr', numTel: '0600000099', role: 'admin', ADMIN: 'true' });
    expect(res.body.ADMIN).toBeUndefined();
    const [row] = (await server.db.query('SELECT role FROM client WHERE email = ?', ['pirate@exemple.fr']))[0] as { role: string }[];
    expect(row?.role).toBe('client');
  });
});

describe('jetons', () => {
  it('exige un jeton pour modifier, supprimer ou lire des coordonnées', async () => {
    const me = await signup(server);
    const identity = { idClient: me.idClient, nomClient: 'X', prenomClient: 'Y', ageClient: '30', email: 'x@exemple.fr', numTel: '0600004321' };
    expect((await server.api.post('/user/update').type('form').send(identity)).status).toBe(401);
    expect((await server.api.post('/user/remove').type('form').send({ idClient: me.idClient })).status).toBe(401);
  });

  it('montre sans compte le nom d’un vendeur, mais ni ses coordonnées ni son âge', async () => {
    const me = await signup(server, { ageClient: '52' });
    const res = await server.api.post('/user').type('form').send({ idClient: me.idClient });
    expect(res.status).toBe(200);
    expect(res.body.user[0]).toMatchObject({ nomClient: 'Nom', email: '', numTel: '', ageClient: 0 });
  });

  it('refuse un jeton falsifié, expiré ou signé avec un autre secret', async () => {
    const me = await signup(server);
    const forged = jwt.sign({}, 'un-autre-secret-un-autre-secret-un-autre', { algorithm: 'HS256', issuer: 'miniprojet', subject: String(me.idClient) });
    const expired = jwt.sign({}, server.config.jwt.secret, { algorithm: 'HS256', issuer: 'miniprojet', subject: String(me.idClient), expiresIn: -10 });
    const none = `${Buffer.from('{"alg":"none"}').toString('base64url')}.${Buffer.from(`{"sub":"${me.idClient}","iss":"miniprojet"}`).toString('base64url')}.`;
    for (const token of [forged, expired, none, 'n-importe-quoi']) {
      const res = await server.api.post('/user').set('Authorization', `Bearer ${token}`).type('form').send({ idClient: me.idClient });
      expect(res.status).toBe(401);
    }
  });

  it('relit le rôle en base : un compte promu l’est tout de suite, un compte supprimé n’a plus accès', async () => {
    const a = await signup(server);
    const b = await signup(server);
    const before = await server.api.post('/user/update').set(a.as).type('form').send({ idClient: b.idClient, nomClient: 'Z', prenomClient: 'Z', ageClient: '30', email: 'z@exemple.fr', numTel: '0600001234' });
    expect(before.status).toBe(403);
    await makeAdmin(server, a);
    const after = await server.api.post('/user/update').set(a.as).type('form').send({ idClient: b.idClient, nomClient: 'Z', prenomClient: 'Z', ageClient: '30', email: 'z@exemple.fr', numTel: '0600001234' });
    expect(after.status).toBe(200);
    expect(after.body.error).toBeUndefined();

    await server.api.post('/user/remove').set(b.as).type('form').send({ idClient: b.idClient });
    const gone = await server.api.post('/user').set(b.as).type('form').send({ idClient: b.idClient });
    expect(gone.status).toBe(401);
  });
});

describe('profils', () => {
  it('montre le profil complet à son propriétaire et masque l’âge aux autres', async () => {
    const a = await signup(server, { ageClient: '41' });
    const b = await signup(server);
    const own = await server.api.post('/user').set(a.as).type('form').send({ idClient: a.idClient });
    expect(own.body.user[0].ageClient).toBe(41);
    expect(own.body.user[0]).not.toHaveProperty('password');
    const other = await server.api.post('/user').set(b.as).type('form').send({ idClient: a.idClient });
    expect(other.body.user[0].ageClient).toBe(0);
    expect(other.body.user[0].nomClient).toBe('Nom');
    expect(other.body.user[0].email).toBe(a.email);
  });

  it('refuse de modifier ou supprimer le compte d’un autre', async () => {
    const a = await signup(server);
    const b = await signup(server);
    const update = await server.api.post('/user/update').set(a.as).type('form').send({ idClient: b.idClient, nomClient: 'Pirate', prenomClient: 'P', ageClient: '30', email: 'p@exemple.fr', numTel: '0600005555' });
    expect(update.status).toBe(403);
    const remove = await server.api.post('/user/remove').set(a.as).type('form').send({ idClient: b.idClient });
    expect(remove.status).toBe(403);
  });
});

describe('limiteur de tentatives', () => {
  it('bloque après trop de connexions échouées, pas après des connexions réussies', async () => {
    const strict = await startServer({ signinFailures: 3 });
    try {
      await signup(strict, { email: 'ok@exemple.fr' });
      for (let i = 0; i < 6; i++) {
        const ok = await strict.api.post('/signin').type('form').send({ email: 'ok@exemple.fr', password: 'motdepasse-solide' });
        expect(ok.status).toBe(200);
        expect(ok.body.token).toBeDefined();
      }
      const statuses: number[] = [];
      for (let i = 0; i < 5; i++) {
        statuses.push((await strict.api.post('/signin').type('form').send({ email: 'ok@exemple.fr', password: 'faux-mot-de-passe' })).status);
      }
      expect(statuses.slice(0, 3)).toEqual([200, 200, 200]);
      expect(statuses.slice(3)).toEqual([429, 429]);
    } finally {
      await strict.close();
    }
  });
});
