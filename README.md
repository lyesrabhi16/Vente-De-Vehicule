<p align="center"><img src="docs/logo.png" alt="GetCars" width="120"></p>

# GetCars: vente et location de véhicules

Mini-projet de fin de cycle de Licence : une application Android pour publier des annonces de véhicules, les parcourir sans compte, réserver une location ou demander un rendez-vous, et discuter en temps réel avec le vendeur. Elle parle à un serveur Express / TypeScript / MySQL.

<p align="center">
  <img src="docs/screenshots/01-accueil.png" alt="Annonces" width="200">
  <img src="docs/screenshots/08-annonce-location.png" alt="Détail d'une annonce" width="200">
  <img src="docs/screenshots/14-conversation.png" alt="Conversation" width="200">
  <img src="docs/screenshots/26-sombre-accueil.png" alt="Mode sombre" width="200">
</p>

## Fonctionnalités

- Annonces : publier, modifier, supprimer, filtrer (type, marque, couleur…), rechercher par mots, photos
- Comptes : inscription, connexion par e-mail ou téléphone, profil, avatar, rôle administrateur
- Réservations (du … au …) et rendez-vous : le client les demande, le vendeur les accepte ou les refuse
- Messagerie en temps réel (Socket.IO) entre acheteur et vendeur
- Carte (Mapbox) pour choisir et consulter le lieu d'un rendez-vous (satellite, repère à déplacer du doigt)

## Captures

Toutes les pages de l'application, dans l'ordre d'un parcours. Les données viennent de `npm run seed:demo` (voir plus bas) . Les voitures sont des dessins faits par un script (ImageMagick), pas des photos.

**Sans compte** : on parcourt et on cherche, on ne fait rien d'autre.

<table>
<tr><td align="center"><img src="docs/screenshots/01-accueil.png" width="170"><br><sub>Annonces</sub></td><td align="center"><img src="docs/screenshots/02-filtres.png" width="170"><br><sub>Filtres</sub></td><td align="center"><img src="docs/screenshots/03-recherche.png" width="170"><br><sub>Recherche</sub></td><td align="center"><img src="docs/screenshots/04-connexion.png" width="170"><br><sub>Connexion</sub></td><td align="center"><img src="docs/screenshots/05-inscription.png" width="170"><br><sub>Inscription</sub></td></tr>
</table>

**Acheteur** : consulter une annonce, réserver une location, demander un rendez-vous, suivre ses demandes, discuter.

<table>
<tr><td align="center"><img src="docs/screenshots/06-accueil-connecte.png" width="170"><br><sub>Accueil, connecté</sub></td><td align="center"><img src="docs/screenshots/07-annonce-vente.png" width="170"><br><sub>Annonce à vendre</sub></td><td align="center"><img src="docs/screenshots/08-annonce-location.png" width="170"><br><sub>Annonce à louer</sub></td><td align="center"><img src="docs/screenshots/09-reservation.png" width="170"><br><sub>Réservation en attente</sub></td><td align="center"><img src="docs/screenshots/10-nouvelle-demande.png" width="170"><br><sub>Nouvelle demande</sub></td></tr>
<tr><td align="center"><img src="docs/screenshots/11-rendez-vous.png" width="170"><br><sub>Rendez-vous accepté</sub></td><td align="center"><img src="docs/screenshots/12-mon-espace.png" width="170"><br><sub>Mon espace</sub></td><td align="center"><img src="docs/screenshots/13-messages.png" width="170"><br><sub>Messages</sub></td><td align="center"><img src="docs/screenshots/14-conversation.png" width="170"><br><sub>Conversation</sub></td><td align="center"><img src="docs/screenshots/15-profil.png" width="170"><br><sub>Mon profil</sub></td></tr>
<tr><td align="center"><img src="docs/screenshots/16-publier.png" width="170"><br><sub>Publier une annonce</sub></td><td align="center"><img src="docs/screenshots/17-profil-vendeur.png" width="170"><br><sub>Profil d'un vendeur</sub></td><td align="center"><img src="docs/screenshots/18-annonces-vendeur.png" width="170"><br><sub>Ses annonces</sub></td></tr>
</table>

**Vendeur** : recevoir une demande, l'accepter ou la refuser.

<table>
<tr><td align="center"><img src="docs/screenshots/19-demandes.png" width="170"><br><sub>Demandes reçues</sub></td><td align="center"><img src="docs/screenshots/20-demande-recue.png" width="170"><br><sub>Une demande reçue</sub></td><td align="center"><img src="docs/screenshots/21-aucune-demande.png" width="170"><br><sub>Rien en attente</sub></td></tr>
</table>

**Carte** (Mapbox) : le lieu d'un rendez-vous. Le vendeur touche la carte pour placer le repère puis enregistre . L'acheteur le voit en lecture seule, sans bouton d'enregistrement. Le lieu se retrouve dans la demande (`longitude latitude`). Il faut un jeton `mapboxToken` (voir plus bas).

<table>
<tr><td align="center"><img src="docs/screenshots/30-carte-lecture.jpg" width="170"><br><sub>Acheteur : lecture seule</sub></td><td align="center"><img src="docs/screenshots/31-carte-ecriture.jpg" width="170"><br><sub>Vendeur : repère déplacé</sub></td></tr>
</table>

**Administrateur** : toutes les annonces, tous les rendez-vous et toutes les locations.

<table>
<tr><td align="center"><img src="docs/screenshots/22-admin-annonces.png" width="170"><br><sub>Annonces</sub></td><td align="center"><img src="docs/screenshots/23-admin-rendez-vous.png" width="170"><br><sub>Rendez-vous</sub></td><td align="center"><img src="docs/screenshots/24-admin-locations.png" width="170"><br><sub>Locations</sub></td><td align="center"><img src="docs/screenshots/25-admin-profil.png" width="170"><br><sub>Profil</sub></td></tr>
</table>

**Mode sombre et lancement**

<table>
<tr><td align="center"><img src="docs/screenshots/26-sombre-accueil.png" width="170"><br><sub>Accueil</sub></td><td align="center"><img src="docs/screenshots/27-sombre-conversation.png" width="170"><br><sub>Conversation</sub></td><td align="center"><img src="docs/screenshots/28-sombre-profil.png" width="170"><br><sub>Profil</sub></td><td align="center"><img src="docs/screenshots/29-lancement.png" width="170"><br><sub>Lancement</sub></td></tr>
</table>

## Architecture

```
app/      Application Android (Java, Volley, Socket.IO client, Mapbox)
server/   API REST + Socket.IO (Express 4, TypeScript, mysql2, zod)
  db/schema.sql   le schéma MySQL (idempotent)
  src/            config, auth, routes, temps réel
  test/           75 tests sur une vraie base MySQL
  demo/           illustrations et `npm run seed:demo` (données de démonstration)
```

L'application parle à l'API en `POST` avec des formulaires (c'est ce que fait Volley) . Les erreurs « métier » (champ manquant, identifiants invalides, e-mail déjà pris…) restent des réponses HTTP 200 `{ "error": "…" }`. Ce qui touche à la sécurité a son vrai code : 401, 403, 429, 413.

## Lancer le serveur

Prérequis : Docker.

```bash
docker compose up --build        # MySQL + serveur sur http://localhost:5000
curl localhost:5000/health       # {"status":"ok"}
```

Sans Docker : `cd server && cp .env.example .env && npm ci && npm run db:schema && npm run dev` (Node 22, MySQL 8 , voir `.env.example`).

Donner le rôle administrateur à un compte (créé d'abord depuis l'application) :

```bash
cd server && npm run promote-admin -- adresse@exemple.fr
```

### Production

Le serveur **refuse de démarrer** en `NODE_ENV=production` sans `JWT_SECRET` (32 caractères au moins, `openssl rand -base64 48`), avec un `DB_PASSWORD` vide ou celui du développement, ou avec `DB_USER=root`. Derrière un reverse proxy, mettre `TRUST_PROXY=true`. Les images déposées sont dans le volume `/data/images`.

## Lancer l'application

Ouvrir le dossier dans Android Studio (JDK 17), ou en ligne de commande :

```bash
./gradlew :app:assembleDebug     # app/build/outputs/apk/debug/app-debug.apk
```

Deux réglages, dans `local.properties` (non versionné , modèle : `local.properties.example`) ou en `-P` :

| Réglage | Défaut | Rôle |
|---|---|---|
| `serverUrl` | `http://10.0.2.2:5000` | adresse du serveur (`10.0.2.2` : la machine hôte, vue depuis l'émulateur) |
| `mapboxToken` | vide | jeton public Mapbox (`pk.…`, https://account.mapbox.com) , sans lui, tout fonctionne sauf la carte |

```properties
serverUrl=http://192.168.1.20:5000
mapboxToken=pk.mon-jeton
```

Le trafic en clair (HTTP) n'est autorisé que par le build debug . Une version release exige HTTPS.

### Données de démonstration

Pour remplir un serveur en marche (comptes, annonces avec photos, réservations, rendez-vous, conversations) :

```bash
cd server && npm run seed:demo
```

Les comptes `admin@getcars.dz` (administrateur), `karim@getcars.dz`, `lina@getcars.dz`, `yanis@getcars.dz` (vendeurs) et `sara@getcars.dz` (acheteuse) ont tous le mot de passe `demo1234`. La commande est rejouable . À ne pas lancer sur un serveur de production.

## Tests

```bash
docker compose -f docker-compose.test.yml up -d --wait   # MySQL de test, port 33306
cd server && npm test                                     # 75 tests
```

Ils couvrent la configuration, l'authentification, les autorisations (qui peut lire, modifier, supprimer quoi), les tentatives d'injection SQL, l'envoi d'images piégées (traversée de répertoire, faux formats, taille) et la messagerie temps réel. La CI (GitHub Actions) les rejoue avec un service MySQL, construit l'image Docker et compile l'application Android.

## Design

L'interface a été entièrement reprise (Material 3, clair et sombre) : palette GetCars plus contrastée, cartes d'annonce avec photo, type, année et prix en DA, pastilles d'état (En attente, Acceptée, Refusée), conversation en bulles, états vides, formulaires en deux colonnes, navigation du bas avec indicateur. L'application est en français . Les libellés sont dans `app/src/main/res/values/strings.xml`.

## Ce qui a changé

La première version du projet fonctionnait mais n'était pas sûre. La réécriture du serveur corrige :

| Avant | Maintenant |
|---|---|
| Requêtes SQL construites par concaténation (injection possible sur la connexion, la recherche, les conversations, les filtres) | Requêtes paramétrées , tables et colonnes cherchables sur liste blanche |
| Aucune authentification : n'importe qui lisait, modifiait ou supprimait les données d'un autre | Jetons JWT , le vendeur, le client et l'administrateur ne touchent qu'à ce qui les concerne |
| Mots de passe en clair dans la base | argon2id , même réponse et même durée pour un compte inconnu ou un mauvais mot de passe |
| Images écrites au chemin envoyé par le client | Noms fermés, format vérifié sur les premiers octets, taille limitée |
| Identité Socket.IO annoncée par le client (`init`) | Identité tirée du jeton , expéditeur d'un message toujours celui du jeton , débit limité |
| Pas de limite de tentatives | Limiteur sur la connexion (échecs), l'inscription et le total |
| Serveur lancé avec `root` et sans mot de passe , clé de carte (TomTom) et adresse IP dans le dépôt | Configuration validée au démarrage , jeton de carte (Mapbox) et adresse hors du dépôt |
| Pas de schéma SQL, pas de tests, pas de CI | `db/schema.sql`, 75 tests, CI, Docker |

> **Ancienne clé TomTom.** La carte est passée de TomTom à Mapbox, mais l'ancienne clé TomTom a été commitée : elle reste dans l'historique git et doit être **révoquée** sur le portail TomTom.

### Règles d'accès

| Ressource | Lecture | Écriture |
|---|---|---|
| Annonces | tout le monde | le vendeur (ou un administrateur) |
| Profil | nom : tout le monde , coordonnées : comptes connectés , âge : le titulaire | le titulaire (ou un administrateur) |
| Réservations, rendez-vous | le client, le vendeur de l'annonce, un administrateur | le client change date et lieu, le vendeur change l'état (`Pending`, `Accepted`, `Rejected`) , une demande naît toujours `Pending` |
| Messages | les deux participants | l'expéditeur, qui est toujours le titulaire du jeton |
| Images d'annonces | tout le monde | le vendeur |
| Avatars | comptes connectés | le titulaire |

## API

Toutes les routes sont en `POST`, sauf `GET /` et `GET /health`. Les routes marquées 🔒 demandent `Authorization: Bearer <jeton>` (rendu par `/signin` et `/signup`).

| Route | |
|---|---|
| `/signup`, `/signin` | créer un compte, se connecter |
| `/user`, 🔒 `/user/update`, 🔒 `/user/remove` | profil |
| `/annonce`, `/annonces`, `/annonces/user` | lire (filtre : `filterObj`, un JSON de colonne → valeur) |
| 🔒 `/annonce/add`, `/annonce/update`, `/annonce/remove` | gérer ses annonces |
| `/search` | `term`, `table` (`annonce`, ou `client` 🔒), `cols` |
| 🔒 `/reservation(s)`, `/reservation/add\|update\|remove` | réservations |
| 🔒 `/rendezvous`, `/rendezvous/all`, `/rendezvous/add\|update\|remove` | rendez-vous |
| 🔒 `/chats`, `/messages`, `/messages/add` | conversations |
| 🔒 `/upload/image`, `/download/image` | images (les avatars demandent un compte) |

Socket.IO : connexion avec `auth: { token }` . L'application émet `message`, reçoit `message-sent` (accusé), `message` (reçu) et `message-error`.

## Licence

MIT - voir [LICENSE](LICENSE).
