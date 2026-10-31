import { afterAll, beforeAll, describe, expect, it } from 'vitest';
import { annonceForm, makeAdmin, postAnnonce, signup, startServer, type Account, type TestServer } from './helpers';

let server: TestServer;
let seller: Account;
let other: Account;
beforeAll(async () => {
  server = await startServer();
  seller = await signup(server);
  other = await signup(server);
});
afterAll(() => server.close());

describe('annonces', () => {
  it('publie au nom de l’utilisateur connecté et lit km et prix comme du texte', async () => {
    const id = await postAnnonce(server, seller);
    const res = await server.api.post('/annonce').type('form').send({ idAnnonce: id });
    expect(res.body.result).toHaveLength(1);
    expect(res.body.result[0]).toMatchObject({ idAnnonce: id, idClient: seller.idClient, kilometrageVehicule: '42000', prixVehicule: '11500.00' });
  });

  it('exige un compte pour publier', async () => {
    const res = await server.api.post('/annonce/add').type('form').send(annonceForm());
    expect(res.status).toBe(401);
  });

  it('ignore un idClient glissé dans le formulaire pour publier au nom d’un autre', async () => {
    const res = await server.api.post('/annonce/add').set(seller.as).type('form').send({ ...annonceForm(), idClient: String(other.idClient) });
    expect(res.status).toBe(403);
  });

  it('laisse l’administrateur publier au nom d’un autre', async () => {
    const admin = await signup(server);
    await makeAdmin(server, admin);
    const res = await server.api.post('/annonce/add').set(admin.as).type('form').send({ ...annonceForm(), idClient: String(other.idClient) });
    expect(res.status).toBe(200);
    const read = await server.api.post('/annonce').type('form').send({ idAnnonce: res.body.result });
    expect(read.body.result[0].idClient).toBe(other.idClient);
  });

  it('valide les champs (année future, prix négatif, champ manquant)', async () => {
    const future = await server.api.post('/annonce/add').set(seller.as).type('form').send(annonceForm({ anneeVehicule: '2999' }));
    expect(future.body.error).toMatch(/anneeVehicule/);
    const negative = await server.api.post('/annonce/add').set(seller.as).type('form').send(annonceForm({ prixVehicule: '-5' }));
    expect(negative.body.error).toMatch(/prixVehicule/);
    const { titre: _titre, ...incomplete } = annonceForm();
    const missing = await server.api.post('/annonce/add').set(seller.as).type('form').send(incomplete);
    expect(missing.body.error).toMatch(/missing required fields : \[titre\]/);
  });

  it('seul le vendeur (ou un administrateur) modifie et supprime son annonce', async () => {
    const id = await postAnnonce(server, seller);
    const edit = await server.api.post('/annonce/update').set(other.as).type('form').send({ idAnnonce: String(id), ...annonceForm({ titre: 'Détournée' }) });
    expect(edit.status).toBe(403);
    const del = await server.api.post('/annonce/remove').set(other.as).type('form').send({ idAnnonce: String(id) });
    expect(del.status).toBe(403);
    const still = await server.api.post('/annonce').type('form').send({ idAnnonce: id });
    expect(still.body.result[0].titre).toBe('Peugeot 208 en parfait état');

    const mine = await server.api.post('/annonce/update').set(seller.as).type('form').send({ idAnnonce: String(id), ...annonceForm({ titre: 'Mise à jour' }) });
    expect(mine.body.error).toBeUndefined();
    const changed = await server.api.post('/annonce').type('form').send({ idAnnonce: id });
    expect(changed.body.result[0].titre).toBe('Mise à jour');
    expect(changed.body.result[0].idClient).toBe(seller.idClient); // le vendeur ne change pas

    const removed = await server.api.post('/annonce/remove').set(seller.as).type('form').send({ idAnnonce: String(id) });
    expect(removed.status).toBe(200);
    const gone = await server.api.post('/annonce').type('form').send({ idAnnonce: id });
    expect(gone.body.result).toEqual([]);
  });

  it('liste publiquement les annonces, d’un vendeur ou filtrées', async () => {
    const mine = await postAnnonce(server, seller, { marqueVehicule: 'Renault', modeleVehicule: 'Clio' });
    const list = await server.api.post('/annonces').type('form').send({});
    expect(list.body.result.length).toBeGreaterThan(0);
    const filtered = await server.api.post('/annonces').type('form').send({ filterObj: JSON.stringify({ marqueVehicule: 'Renault' }) });
    expect(filtered.body.result.map((a: { idAnnonce: number }) => a.idAnnonce)).toContain(mine);
    expect(filtered.body.result.every((a: { marqueVehicule: string }) => a.marqueVehicule === 'Renault')).toBe(true);
    const ofSeller = await server.api.post('/annonces/user').type('form').send({ idClient: seller.idClient });
    expect(ofSeller.body.result.every((a: { idClient: number }) => a.idClient === seller.idClient)).toBe(true);
  });
});

describe('filtres et recherche : injection SQL', () => {
  const attempts: [string, string][] = [
    ['une colonne inconnue', JSON.stringify({ 'idAnnonce` OR 1=1 -- ': '1' })],
    ['une valeur piégée', JSON.stringify({ marqueVehicule: "x' OR '1'='1" })],
    ['un nom de colonne piégé', JSON.stringify({ password: 'x' })],
    ['un tableau', JSON.stringify([1, 2])],
    ['un objet imbriqué', JSON.stringify({ marqueVehicule: { $ne: '' } })],
    ['du JSON cassé', '{marqueVehicule'],
  ];

  it.each(attempts)('filterObj : %s', async (_name, filterObj) => {
    const res = await server.api.post('/annonces').type('form').send({ filterObj });
    expect(res.status).toBe(200);
    if (res.body.result) expect(res.body.result.every((a: { marqueVehicule: string }) => a.marqueVehicule !== undefined && !('password' in a))).toBe(true);
    // une valeur piégée est une valeur comme une autre : aucune annonce ne s'appelle ainsi
    if (_name === 'une valeur piégée') expect(res.body.result).toEqual([]);
    else expect(res.body.error).toMatch(/invalid fields/);
  });

  it('ne laisse jamais la table des comptes se lire par /search', async () => {
    const res = await server.api.post('/search').type('form').send({ term: 'a', table: 'client; DROP TABLE client', cols: 'password' });
    expect(res.body.error).toBeDefined();
    const cols = await server.api.post('/search').set(seller.as).type('form').send({ term: 'a', table: 'client', cols: 'password' });
    expect(cols.body.error).toMatch(/password/);
    const injected = await server.api.post('/search').type('form').send({ term: 'a', table: 'annonce', cols: 'titre FROM client -- ' });
    expect(injected.body.error).toBeDefined();
  });

  it("traite le terme cherché comme du texte, pas comme du SQL ni comme un joker", async () => {
    await postAnnonce(server, seller, { titre: "100% fiable d'occasion" });
    const sql = await server.api.post('/search').type('form').send({ term: "' OR '1'='1", table: 'annonce', cols: 'idAnnonce,titre' });
    expect(sql.body.result).toEqual([]);
    const joker = await server.api.post('/search').type('form').send({ term: '%', table: 'annonce', cols: 'titre' });
    expect(joker.body.result.map((r: { titre: string }) => r.titre)).toEqual(["100% fiable d'occasion"]);
  });

  it('cherche chaque mot dans les annonces, sans compte', async () => {
    await postAnnonce(server, seller, { marqueVehicule: 'Tesla', modeleVehicule: 'Model 3', energieVehicule: 'Électrique' });
    const res = await server.api.post('/search').type('form').send({ term: 'tesla électrique', table: 'annonce', cols: 'idAnnonce, marqueVehicule' });
    expect(res.body.result).toHaveLength(1);
    expect(res.body.result[0].marqueVehicule).toBe('Tesla');
  });

  it('réserve la recherche de personnes aux comptes connectés', async () => {
    const anonymous = await server.api.post('/search').type('form').send({ term: 'Nom', table: 'client', cols: 'idClient,nomClient' });
    expect(anonymous.status).toBe(401);
    const res = await server.api.post('/search').set(seller.as).type('form').send({ term: 'Nom', table: 'client', cols: 'idClient, nomClient, prenomClient, email' });
    expect(res.body.result.length).toBeGreaterThan(0);
    expect(res.body.result[0]).not.toHaveProperty('password');
  });
});
