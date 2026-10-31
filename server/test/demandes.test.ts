import { afterAll, beforeAll, describe, expect, it } from 'vitest';
import { makeAdmin, postAnnonce, signup, startServer, type Account, type TestServer } from './helpers';

let server: TestServer;
let seller: Account;
let buyer: Account;
let stranger: Account;
let annonce: number;

beforeAll(async () => {
  server = await startServer();
  seller = await signup(server);
  buyer = await signup(server);
  stranger = await signup(server);
  annonce = await postAnnonce(server, seller);
});
afterAll(() => server.close());

const reservation = (overrides: Record<string, string> = {}) => ({
  idClient: String(buyer.idClient),
  idAnnonce: String(annonce),
  dateDebut: '2030-06-01 10:00:00',
  dateFin: '2030-06-08 10:00:00',
  lieuReservation: 'Paris',
  ...overrides,
});

describe('réservations', () => {
  it('un client réserve, la demande démarre toujours « Pending » même s’il veut mieux', async () => {
    const res = await server.api.post('/reservation/add').set(buyer.as).type('form').send({ ...reservation(), etatReservation: 'Accepted' });
    expect(res.body.error).toBeUndefined();
    const read = await server.api.post('/reservation').set(buyer.as).type('form').send({ idClient: buyer.idClient, idAnnonce: annonce });
    expect(read.body.result[0]).toMatchObject({ etatReservation: 'Pending', lieuReservation: 'Paris', dateDebut: '2030-06-01 10:00:00' });
  });

  it('refuse un doublon, une fin avant le début, sa propre annonce et un autre nom', async () => {
    const dup = await server.api.post('/reservation/add').set(buyer.as).type('form').send(reservation());
    expect(dup.body.error).toMatch(/existe déjà/);
    const reversed = await server.api.post('/reservation/add').set(stranger.as).type('form').send(reservation({ idClient: String(stranger.idClient), dateFin: '2030-05-01 10:00:00' }));
    expect(reversed.body.error).toMatch(/dateFin/);
    const own = await server.api.post('/reservation/add').set(seller.as).type('form').send(reservation({ idClient: String(seller.idClient) }));
    expect(own.body.error).toMatch(/propre annonce/);
    const impersonate = await server.api.post('/reservation/add').set(stranger.as).type('form').send(reservation());
    expect(impersonate.status).toBe(403);
  });

  it('seuls le client, le vendeur et l’administrateur la voient', async () => {
    const key = { idClient: buyer.idClient, idAnnonce: annonce };
    for (const who of [buyer, seller]) {
      const res = await server.api.post('/reservation').set(who.as).type('form').send(key);
      expect(res.body.result).toHaveLength(1);
      expect(res.body.result[0]).not.toHaveProperty('idVendeur');
    }
    const hidden = await server.api.post('/reservation').set(stranger.as).type('form').send(key);
    expect(hidden.body.result).toEqual([]);

    const mine = await server.api.post('/reservations').set(stranger.as).type('form').send({});
    expect(mine.body.result).toEqual([]);
    const sellerList = await server.api.post('/reservations').set(seller.as).type('form').send({});
    expect(sellerList.body.result).toHaveLength(1);
    const unauthenticated = await server.api.post('/reservations').type('form').send({});
    expect(unauthenticated.status).toBe(401);

    const admin = await signup(server);
    await makeAdmin(server, admin);
    const all = await server.api.post('/reservations').set(admin.as).type('form').send({});
    expect(all.body.result).toHaveLength(1);
  });

  it('filtre la liste sans sortir de ce que l’on a le droit de voir', async () => {
    const filtered = await server.api.post('/reservations').set(stranger.as).type('form').send({ filterObj: JSON.stringify({ idClient: buyer.idClient }) });
    expect(filtered.body.result).toEqual([]);
    const injected = await server.api.post('/reservations').set(stranger.as).type('form').send({ filterObj: JSON.stringify({ '1=1 OR idClient': 1 }) });
    expect(injected.body.error).toMatch(/invalid fields/);
  });

  it("le vendeur décide de l'état, le client des dates, pas l'inverse", async () => {
    const base = reservation();
    const state = (etatReservation: string, extra: Record<string, string> = {}) => ({ ...base, etatReservation, ...extra });

    const selfAccept = await server.api.post('/reservation/update').set(buyer.as).type('form').send(state('Accepted'));
    expect(selfAccept.status).toBe(403);

    const sellerMoves = await server.api.post('/reservation/update').set(seller.as).type('form').send(state('Accepted', { lieuReservation: 'Ailleurs' }));
    expect(sellerMoves.status).toBe(403);

    const accept = await server.api.post('/reservation/update').set(seller.as).type('form').send(state('Accepted'));
    expect(accept.body.error).toBeUndefined();

    const reschedule = await server.api.post('/reservation/update').set(buyer.as).type('form').send(state('Accepted', { lieuReservation: 'Lyon' }));
    expect(reschedule.body.error).toBeUndefined();
    const read = await server.api.post('/reservation').set(buyer.as).type('form').send({ idClient: buyer.idClient, idAnnonce: annonce });
    expect(read.body.result[0]).toMatchObject({ etatReservation: 'Accepted', lieuReservation: 'Lyon' });

    const outsider = await server.api.post('/reservation/update').set(stranger.as).type('form').send(state('Rejected'));
    expect(outsider.status).toBe(403);
  });

  it('refuse un état hors de la liste', async () => {
    const res = await server.api.post('/reservation/update').set(seller.as).type('form').send({ ...reservation(), etatReservation: 'Granted' });
    expect(res.body.error).toMatch(/etatReservation/);
  });

  it('seuls les concernés la suppriment', async () => {
    const key = { idClient: String(buyer.idClient), idAnnonce: String(annonce) };
    expect((await server.api.post('/reservation/remove').set(stranger.as).type('form').send(key)).status).toBe(403);
    expect((await server.api.post('/reservation/remove').set(buyer.as).type('form').send(key)).body.error).toBeUndefined();
    expect((await server.api.post('/reservation/remove').set(buyer.as).type('form').send(key)).body.error).toMatch(/introuvable/);
  });
});

describe('rendez-vous', () => {
  const rdv = (overrides: Record<string, string> = {}) => ({
    idClient: String(buyer.idClient),
    idAnnonce: String(annonce),
    dateRendezVous: '2030-07-01T14:30:00.000Z',
    lieuRendezVous: 'Garage du centre',
    ...overrides,
  });

  it('suit les mêmes règles', async () => {
    const add = await server.api.post('/rendezvous/add').set(buyer.as).type('form').send(rdv());
    expect(add.body.error).toBeUndefined();
    const read = await server.api.post('/rendezvous').set(seller.as).type('form').send({ idClient: buyer.idClient, idAnnonce: annonce });
    expect(read.body.result[0]).toMatchObject({ etatRendezVous: 'Pending', dateRendezVous: '2030-07-01 14:30:00' });

    expect((await server.api.post('/rendezvous/all').set(stranger.as).type('form').send({})).body.result).toEqual([]);
    expect((await server.api.post('/rendezvous/all').set(seller.as).type('form').send({})).body.result).toHaveLength(1);
    expect((await server.api.post('/rendezvous/update').set(buyer.as).type('form').send({ ...rdv(), etatRendezVous: 'Accepted' })).status).toBe(403);
    expect((await server.api.post('/rendezvous/update').set(seller.as).type('form').send({ ...rdv(), etatRendezVous: 'Accepted' })).body.error).toBeUndefined();
    expect((await server.api.post('/rendezvous/remove').set(stranger.as).type('form').send({ idClient: buyer.idClient, idAnnonce: annonce })).status).toBe(403);
  });

  it("disparaît avec l'annonce", async () => {
    await server.api.post('/annonce/remove').set(seller.as).type('form').send({ idAnnonce: String(annonce) });
    const all = await server.api.post('/rendezvous/all').set(buyer.as).type('form').send({});
    expect(all.body.result).toEqual([]);
  });
});
