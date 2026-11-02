import { io as connect, type Socket } from 'socket.io-client';
import { afterAll, beforeAll, describe, expect, it } from 'vitest';
import { signup, startServer, type Account, type TestServer } from './helpers';

let server: TestServer;
let alice: Account;
let bob: Account;
let carol: Account;
const sockets: Socket[] = [];

beforeAll(async () => {
  server = await startServer();
  alice = await signup(server);
  bob = await signup(server);
  carol = await signup(server);
});
afterAll(async () => {
  sockets.forEach((s) => s.close());
  await server.close();
});

const open = (token?: string): Promise<Socket> =>
  new Promise((resolve, reject) => {
    const socket = connect(server.url, { auth: token ? { token } : {}, transports: ['websocket'], reconnection: false });
    sockets.push(socket);
    socket.on('connected', () => resolve(socket));
    socket.on('connect_error', reject);
  });

const next = <T = unknown[]>(socket: Socket, event: string): Promise<T> => new Promise((resolve) => socket.once(event, (...args: unknown[]) => resolve(args as T)));

describe('messages HTTP', () => {
  it('enregistre un message, avec l’expéditeur du jeton', async () => {
    const res = await server.api.post('/messages/add').set(alice.as).type('form').send({ idClient_reciever: String(bob.idClient), contenuMessage: 'Bonjour', etatMessage: 'sent', date: '2030-01-01 10:00:00' });
    expect(res.body.result).toMatchObject({ idClient_sender: alice.idClient, idClient_reciever: bob.idClient, contenuMessage: 'Bonjour', etatMessage: 'sent' });
  });

  it('refuse d’écrire au nom d’un autre, à soi-même ou à personne', async () => {
    const forged = await server.api.post('/messages/add').set(carol.as).type('form').send({ idClient_sender: String(alice.idClient), idClient_reciever: String(bob.idClient), contenuMessage: 'Faux' });
    expect(forged.status).toBe(403);
    const self = await server.api.post('/messages/add').set(alice.as).type('form').send({ idClient_reciever: String(alice.idClient), contenuMessage: 'Moi' });
    expect(self.body.error).toBeDefined();
    const nobody = await server.api.post('/messages/add').set(alice.as).type('form').send({ idClient_reciever: '999999', contenuMessage: 'Personne' });
    expect(nobody.body.error).toMatch(/introuvable/);
  });

  it('ne montre une conversation qu’à ses deux participants', async () => {
    const mine = await server.api.post('/messages').set(bob.as).type('form').send({ idClient1: bob.idClient, idClient2: alice.idClient });
    expect(mine.body.result.map((m: { contenuMessage: string }) => m.contenuMessage)).toEqual(['Bonjour']);
    const spying = await server.api.post('/messages').set(carol.as).type('form').send({ idClient1: alice.idClient, idClient2: bob.idClient });
    expect(spying.status).toBe(403);
    const anonymous = await server.api.post('/messages').type('form').send({ idClient1: alice.idClient, idClient2: bob.idClient });
    expect(anonymous.status).toBe(401);
  });

  it('ne liste les conversations que de soi', async () => {
    const own = await server.api.post('/chats').set(alice.as).type('form').send({ idClient: alice.idClient });
    expect(own.body.result.length).toBeGreaterThan(0);
    expect(own.body.result[0]).toHaveProperty('nomClient');
    const other = await server.api.post('/chats').set(carol.as).type('form').send({ idClient: alice.idClient });
    expect(other.status).toBe(403);
  });
});

describe('messagerie temps réel', () => {
  it('refuse une connexion sans jeton ou avec un mauvais jeton', async () => {
    await expect(open()).rejects.toThrow('unauthorized');
    await expect(open('faux')).rejects.toThrow('unauthorized');
  });

  it('livre un message à son destinataire seul, et confirme à l’expéditeur', async () => {
    const [a, b, c] = await Promise.all([open(alice.token), open(bob.token), open(carol.token)]);
    let eavesdropped = false;
    c.on('message', () => (eavesdropped = true));

    const received = next<[{ contenuMessage: string; idClient_sender: number }, number]>(b, 'message');
    const confirmed = next<[{ idMessage: number }]>(a, 'message-sent');
    a.emit('message', { idClient_reciever: bob.idClient, contenuMessage: 'Salut Bob' });

    const [[message, id], [saved]] = await Promise.all([received, confirmed]);
    expect(message).toMatchObject({ contenuMessage: 'Salut Bob', idClient_sender: alice.idClient });
    expect(saved.idMessage).toBe(id);
    await new Promise((r) => setTimeout(r, 100));
    expect(eavesdropped).toBe(false);
  });

  it('prend l’expéditeur dans le jeton, pas dans le message', async () => {
    const [a, b] = await Promise.all([open(alice.token), open(bob.token)]);
    const received = next<[{ idClient_sender: number }]>(b, 'message');
    a.emit('message', { idClient_sender: carol.idClient, idClient_reciever: bob.idClient, contenuMessage: 'Je suis Carol' });
    const [message] = await received;
    expect(message.idClient_sender).toBe(alice.idClient);
  });

  it('signale un message invalide à l’expéditeur sans rien enregistrer', async () => {
    const a = await open(alice.token);
    const failure = next<[{ error: string }]>(a, 'message-error');
    a.emit('message', { idClient_reciever: bob.idClient, contenuMessage: '' });
    expect((await failure)[0].error).toMatch(/missing required fields/);
  });

  it('limite le débit d’un expéditeur', async () => {
    const a = await open(alice.token);
    const errors: string[] = [];
    a.on('message-error', (e: { error: string }) => errors.push(e.error));
    for (let i = 0; i < 40; i++) a.emit('message', { idClient_reciever: bob.idClient, contenuMessage: `spam ${i}` });
    await new Promise((r) => setTimeout(r, 800));
    expect(errors.length).toBeGreaterThanOrEqual(5);
    expect(errors[0]).toMatch(/Trop de messages/);
  });

  it('seul le destinataire marque un message comme reçu', async () => {
    const [a, b] = await Promise.all([open(alice.token), open(bob.token)]);
    const confirmed = next<[{ idMessage: number }]>(a, 'message-sent');
    a.emit('message', { idClient_reciever: bob.idClient, contenuMessage: 'Accusé ?' });
    const [{ idMessage }] = await confirmed;
    const state = async () => ((await server.db.query('SELECT etatMessage FROM message WHERE idMessage = ?', [idMessage]))[0] as { etatMessage: string }[])[0]?.etatMessage;

    a.emit('message-received', idMessage); // l'expéditeur ne peut pas
    await new Promise((r) => setTimeout(r, 200));
    expect(await state()).toBe('sent');
    b.emit('message-received', idMessage);
    await new Promise((r) => setTimeout(r, 200));
    expect(await state()).toBe('received');
  });
});
