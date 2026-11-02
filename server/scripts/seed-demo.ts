import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { loadConfig } from '../src/config';
import { createDb, run } from '../src/db';

/**
 * Remplit un serveur en marche de données de démonstration (comptes, annonces avec photos, demandes, conversations) en passant
 * par l'API, comme le ferait l'application : `npm run seed:demo` (API_URL pour un autre serveur que http://localhost:5000).
 * Les lieux sont des coordonnées « longitude latitude », comme les enregistre le sélecteur de carte de l'application.
 * Les comptes ont tous le mot de passe « demo1234 » ; le compte admin@getcars.dz est administrateur.
 * Rejouable : un compte ou une annonce déjà présente n'est pas recréé.
 */
const API = process.env.API_URL ?? 'http://localhost:5000';
const PASSWORD = 'demo1234';
const IMAGES = path.join(__dirname, '..', 'demo', 'images');

type Json = Record<string, any>;

async function call(route: string, body: Record<string, string | number>, token?: string): Promise<Json> {
  const res = await fetch(API + route, {
    method: 'POST',
    headers: { 'content-type': 'application/x-www-form-urlencoded', ...(token ? { authorization: `Bearer ${token}` } : {}) },
    body: new URLSearchParams(Object.entries(body).map(([k, v]) => [k, String(v)])),
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`${route} : HTTP ${res.status} ${text}`);
  try {
    const json = JSON.parse(text) as Json;
    if (json.error) throw new Error(`${route} : ${json.error}`);
    return json;
  } catch (error) {
    if (error instanceof SyntaxError) return { text };
    throw error;
  }
}

interface Account {
  idClient: number;
  token: string;
  email: string;
}

const PEOPLE = [
  { key: 'admin', nom: 'GetCars', prenom: 'Admin', age: 35, email: 'admin@getcars.dz', tel: '0550000001', avatar: 'avatar-admin.jpg' },
  { key: 'karim', nom: 'Benali', prenom: 'Karim', age: 38, email: 'karim@getcars.dz', tel: '0550000002', avatar: 'avatar-karim.jpg' },
  { key: 'lina', nom: 'Haddad', prenom: 'Lina', age: 31, email: 'lina@getcars.dz', tel: '0550000003', avatar: 'avatar-lina.jpg' },
  { key: 'yanis', nom: 'Mokrani', prenom: 'Yanis', age: 44, email: 'yanis@getcars.dz', tel: '0550000004', avatar: 'avatar-yanis.jpg' },
  { key: 'sara', nom: 'Amrani', prenom: 'Sara', age: 27, email: 'sara@getcars.dz', tel: '0550000005', avatar: 'avatar-sara.jpg' },
] as const;

type Key = (typeof PEOPLE)[number]['key'];

const ANNONCES: { owner: Key; image: string; extra?: string; a: Record<string, string | number> }[] = [
  { owner: 'karim', image: 'mercedes.jpg', a: { titre: 'Mercedes Classe C 220d', description: 'Berline élégante, première main, entretien complet en concession. Intérieur cuir, aide au stationnement.', typeVehicule: 'Vente', marqueVehicule: 'Mercedes', modeleVehicule: 'Classe C', couleurVehicule: 'noire', transmissionVehicule: 'Automatique', kilometrageVehicule: 68000, anneeVehicule: 2019, moteurVehicule: '2.0 diesel 194 ch', energieVehicule: 'diesel', prixVehicule: 4850000 } },
  { owner: 'lina', image: 'bmw.jpg', a: { titre: 'BMW Série 4 Coupé', description: 'Coupé sportif en excellent état, pack M, toit ouvrant. Carnet d’entretien à jour.', typeVehicule: 'Vente', marqueVehicule: 'BMW', modeleVehicule: 'Série 4', couleurVehicule: 'blanche', transmissionVehicule: 'Automatique', kilometrageVehicule: 32000, anneeVehicule: 2021, moteurVehicule: '2.0 essence 184 ch', energieVehicule: 'essence', prixVehicule: 7900000 } },
  { owner: 'yanis', image: 'audi.jpg', a: { titre: 'Audi Q5 à la journée', description: 'SUV confortable pour vos déplacements en famille ou en voyage d’affaires. Kilométrage illimité.', typeVehicule: 'Location', marqueVehicule: 'Audi', modeleVehicule: 'Q5', couleurVehicule: 'rouge', transmissionVehicule: 'Automatique', kilometrageVehicule: 54000, anneeVehicule: 2020, moteurVehicule: '2.0 TDI 190 ch', energieVehicule: 'diesel', prixVehicule: 12000 } },
  { owner: 'karim', image: 'porsche.jpg', a: { titre: 'Porsche 911 Carrera', description: 'Icône de la sportive : moteur six cylindres à plat, échappement sport, jantes 20 pouces.', typeVehicule: 'Vente', marqueVehicule: 'Porsche', modeleVehicule: '911', couleurVehicule: 'jaune', transmissionVehicule: 'Automatique', kilometrageVehicule: 41000, anneeVehicule: 2018, moteurVehicule: '3.0 essence 370 ch', energieVehicule: 'essence', prixVehicule: 18500000 } },
  { owner: 'lina', image: 'hyundai.jpg', a: { titre: 'Hyundai Tucson à la journée', description: 'SUV récent et économique, idéal pour un week-end. Livraison possible sur Alger.', typeVehicule: 'Location', marqueVehicule: 'Hyundai', modeleVehicule: 'Tucson', couleurVehicule: 'bleue', transmissionVehicule: 'Manuelle', kilometrageVehicule: 18000, anneeVehicule: 2022, moteurVehicule: '1.6 CRDi 136 ch', energieVehicule: 'diesel', prixVehicule: 9000 } },
  { owner: 'yanis', image: 'tesla.jpg', a: { titre: 'Tesla Model 3 Long Range', description: 'Autonomie 580 km, autopilot, mises à jour à distance. Batterie en parfaite santé.', typeVehicule: 'Vente', marqueVehicule: 'Tesla', modeleVehicule: 'Model 3', couleurVehicule: 'blanche', transmissionVehicule: 'Automatique', kilometrageVehicule: 24000, anneeVehicule: 2022, moteurVehicule: 'Double moteur', energieVehicule: 'electrique', prixVehicule: 11200000 } },
  { owner: 'karim', image: 'golf.jpg', a: { titre: 'Volkswagen Golf 8', description: 'Compacte polyvalente, écran tactile, régulateur adaptatif. Très peu utilisée.', typeVehicule: 'Vente', marqueVehicule: 'Volkswagen', modeleVehicule: 'Golf 8', couleurVehicule: 'verte', transmissionVehicule: 'Manuelle', kilometrageVehicule: 29000, anneeVehicule: 2021, moteurVehicule: '1.5 TSI 130 ch', energieVehicule: 'essence', prixVehicule: 3950000 } },
  { owner: 'lina', image: 'toyota.jpg', a: { titre: 'Toyota RAV4 hybride', description: 'Hybride rechargeable, consommation maîtrisée, garantie constructeur encore valable.', typeVehicule: 'Vente', marqueVehicule: 'Toyota', modeleVehicule: 'RAV4', couleurVehicule: 'grise', transmissionVehicule: 'Automatique', kilometrageVehicule: 12000, anneeVehicule: 2023, moteurVehicule: '2.5 hybride 222 ch', energieVehicule: 'hybride', prixVehicule: 8400000 } },
  { owner: 'yanis', image: 'peugeot.jpg', a: { titre: 'Peugeot 508 à la journée', description: 'Berline spacieuse et silencieuse pour vos déplacements professionnels.', typeVehicule: 'Location', marqueVehicule: 'Peugeot', modeleVehicule: '508', couleurVehicule: 'beige', transmissionVehicule: 'Automatique', kilometrageVehicule: 47000, anneeVehicule: 2020, moteurVehicule: '1.5 BlueHDi 130 ch', energieVehicule: 'diesel', prixVehicule: 8000 } },
];

const ago = (days: number, hour: number, minute = 0): string => {
  const d = new Date(Date.now() - days * 86_400_000);
  d.setHours(hour, minute, 0, 0);
  return d.toISOString().slice(0, 19).replace('T', ' ');
};
const inDays = (days: number, hour = 10): string => ago(-days, hour);

async function upload(token: string, id: number, file: string, extra: Record<string, string | number>): Promise<void> {
  const bytes = await readFile(path.join(IMAGES, file));
  await call('/upload/image', { userID: id, imgB64: bytes.toString('base64'), format: 'jpeg', ...extra }, token);
}

async function main(): Promise<void> {
  const config = loadConfig();
  const db = createDb(config);
  const accounts = {} as Record<Key, Account>;

  try {
    for (const p of PEOPLE) {
      let res: Json;
      try {
        res = await call('/signup', { nomClient: p.nom, prenomClient: p.prenom, ageClient: p.age, email: p.email, numTel: p.tel, password: PASSWORD });
        console.log(`compte créé : ${p.email}`);
        accounts[p.key] = { idClient: res.idClient, token: res.token, email: p.email };
        await upload(res.token, res.idClient, p.avatar, { type: 'AVATAR' });
      } catch {
        res = await call('/signin', { email: p.email, password: PASSWORD });
        accounts[p.key] = { idClient: res.idClient, token: res.token, email: p.email };
      }
    }
    await run(db, "UPDATE client SET role = 'admin' WHERE email = 'admin@getcars.dz'");

    const ids: number[] = [];
    for (const item of ANNONCES) {
      const owner = accounts[item.owner];
      const existing = (await call('/annonces/user', { idClient: owner.idClient })).result as Json[];
      const found = existing.find((e) => e.titre === item.a.titre);
      if (found) {
        ids.push(found.idAnnonce);
        continue;
      }
      const { result } = await call('/annonce/add', item.a, owner.token);
      ids.push(result);
      await upload(owner.token, result, item.image, { type: 'ANNONCE', index: 0 });
      console.log(`annonce créée : ${item.a.titre}`);
    }
    const idOf = (title: string): number => ids[ANNONCES.findIndex((a) => a.a.titre === title)] as number;

    const sara = accounts.sara;
    const safe = async (what: string, fn: () => Promise<unknown>) => fn().catch((e: Error) => console.log(`${what} : ${e.message}`));

    // réservations (locations) et rendez-vous (ventes)
    await safe('réservation Audi', async () => {
      await call('/reservation/add', { idClient: sara.idClient, idAnnonce: idOf('Audi Q5 à la journée'), dateDebut: inDays(10), dateFin: inDays(14), lieuReservation: '3.2154 36.6910' }, sara.token);
      await call('/reservation/update', { idClient: sara.idClient, idAnnonce: idOf('Audi Q5 à la journée'), dateDebut: inDays(10), dateFin: inDays(14), lieuReservation: '3.2154 36.6910', etatReservation: 'Accepted' }, accounts.yanis.token);
    });
    await safe('réservation Tucson', () => call('/reservation/add', { idClient: sara.idClient, idAnnonce: idOf('Hyundai Tucson à la journée'), dateDebut: inDays(20), dateFin: inDays(23), lieuReservation: '5.0689 36.7509' }, sara.token));
    await safe('réservation Peugeot', async () => {
      await call('/reservation/add', { idClient: accounts.karim.idClient, idAnnonce: idOf('Peugeot 508 à la journée'), dateDebut: inDays(5), dateFin: inDays(7), lieuReservation: '3.0420 36.7440' }, accounts.karim.token);
      await call('/reservation/update', { idClient: accounts.karim.idClient, idAnnonce: idOf('Peugeot 508 à la journée'), dateDebut: inDays(5), dateFin: inDays(7), lieuReservation: '3.0420 36.7440', etatReservation: 'Rejected' }, accounts.yanis.token);
    });
    await safe('rendez-vous Mercedes', () => call('/rendezvous/add', { idClient: sara.idClient, idAnnonce: idOf('Mercedes Classe C 220d'), dateRendezVous: inDays(3, 15), lieuRendezVous: '3.0420 36.7440' }, sara.token));
    await safe('rendez-vous BMW', async () => {
      await call('/rendezvous/add', { idClient: sara.idClient, idAnnonce: idOf('BMW Série 4 Coupé'), dateRendezVous: inDays(6, 11), lieuRendezVous: '3.1830 36.7220' }, sara.token);
      await call('/rendezvous/update', { idClient: sara.idClient, idAnnonce: idOf('BMW Série 4 Coupé'), dateRendezVous: inDays(6, 11), lieuRendezVous: '3.1830 36.7220', etatRendezVous: 'Accepted' }, accounts.lina.token);
    });
    await safe('rendez-vous Tesla', () => call('/rendezvous/add', { idClient: accounts.karim.idClient, idAnnonce: idOf('Tesla Model 3 Long Range'), dateRendezVous: inDays(4, 17), lieuRendezVous: '3.0810 36.7280' }, accounts.karim.token));

    // conversations
    const exists = (await call('/messages', { idClient1: sara.idClient, idClient2: accounts.karim.idClient }, sara.token)).result as Json[];
    if (exists.length === 0) {
      const say = (from: Key, to: Key, text: string, date: string) =>
        call('/messages/add', { idClient_reciever: accounts[to].idClient, contenuMessage: text, date }, accounts[from].token);
      await say('sara', 'karim', 'Bonjour, la Mercedes Classe C est-elle toujours disponible ?', ago(2, 9, 12));
      await say('karim', 'sara', 'Bonjour Sara, oui elle est disponible. Elle est à Hydra, vous pouvez la voir quand vous voulez.', ago(2, 9, 40));
      await say('sara', 'karim', 'Parfait. Le prix est-il négociable ?', ago(2, 10, 5));
      await say('karim', 'sara', 'Un peu, selon la visite. Je vous propose un rendez-vous cette semaine.', ago(2, 10, 31));
      await say('sara', 'karim', 'Très bien, je vous envoie une demande de rendez-vous.', ago(1, 18, 2));
      await say('lina', 'sara', 'Votre rendez-vous pour la BMW est confirmé, à bientôt !', ago(1, 12, 15));
      await say('sara', 'lina', 'Merci Lina, j’y serai.', ago(1, 12, 20));
      await say('yanis', 'sara', 'Votre réservation de l’Audi Q5 est acceptée. Bon voyage !', ago(0, 8, 45));
      console.log('conversations créées');
    }
    console.log(`\nDémonstration prête sur ${API}\n  comptes : ${PEOPLE.map((p) => p.email).join(', ')}\n  mot de passe : ${PASSWORD} (admin@getcars.dz est administrateur)`);
  } finally {
    await db.end();
  }
}

main().catch((error: Error) => {
  console.error(error.message);
  process.exit(1);
});
