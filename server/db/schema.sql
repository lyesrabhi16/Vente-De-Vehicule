-- Le schéma de la base MySQL (8.x). Il n'était pas dans le dépôt : il est déduit des requêtes de l'ancien serveur et des modèles de l'application.
-- Idempotent : rejouable sans rien casser (npm run db:schema).

CREATE TABLE IF NOT EXISTS client (
  idClient      INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nomClient     VARCHAR(100)  NOT NULL,
  prenomClient  VARCHAR(100)  NOT NULL,
  ageClient     TINYINT UNSIGNED NOT NULL,
  email         VARCHAR(255)  NOT NULL,
  numTel        VARCHAR(30)   NOT NULL,
  password      VARCHAR(255)  NOT NULL,           -- hash argon2id, jamais le mot de passe
  role          ENUM('client', 'admin') NOT NULL DEFAULT 'client',
  createdAt     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idClient),
  UNIQUE KEY uq_client_email (email),
  UNIQUE KEY uq_client_numtel (numTel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS annonce (
  idAnnonce            INT UNSIGNED NOT NULL AUTO_INCREMENT,
  idClient             INT UNSIGNED NOT NULL,      -- le vendeur
  titre                VARCHAR(150)  NOT NULL,
  description          TEXT          NOT NULL,
  typeVehicule         VARCHAR(50)   NOT NULL,
  marqueVehicule       VARCHAR(50)   NOT NULL,
  modeleVehicule       VARCHAR(50)   NOT NULL,
  couleurVehicule      VARCHAR(30)   NOT NULL,
  transmissionVehicule VARCHAR(30)   NOT NULL,
  kilometrageVehicule  INT UNSIGNED  NOT NULL,
  anneeVehicule        SMALLINT UNSIGNED NOT NULL,
  moteurVehicule       VARCHAR(50)   NOT NULL,
  energieVehicule      VARCHAR(30)   NOT NULL,
  prixVehicule         DECIMAL(12, 2) NOT NULL,
  createdAt            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idAnnonce),
  KEY ix_annonce_client (idClient),
  CONSTRAINT fk_annonce_client FOREIGN KEY (idClient) REFERENCES client (idClient) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS message (
  idMessage         INT UNSIGNED NOT NULL AUTO_INCREMENT,
  idClient_sender   INT UNSIGNED NOT NULL,
  idClient_reciever INT UNSIGNED NOT NULL,
  contenuMessage    TEXT         NOT NULL,
  etatMessage       VARCHAR(20)  NOT NULL DEFAULT 'sent',   -- sent | received
  date              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (idMessage),
  KEY ix_message_sender (idClient_sender),
  KEY ix_message_reciever (idClient_reciever),
  CONSTRAINT fk_message_sender   FOREIGN KEY (idClient_sender)   REFERENCES client (idClient) ON DELETE CASCADE,
  CONSTRAINT fk_message_reciever FOREIGN KEY (idClient_reciever) REFERENCES client (idClient) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- une demande de réservation (location) d'un client pour une annonce ; le vendeur l'accepte ou la refuse
CREATE TABLE IF NOT EXISTS reservation (
  idClient        INT UNSIGNED NOT NULL,
  idAnnonce       INT UNSIGNED NOT NULL,
  dateDebut       DATETIME     NOT NULL,
  dateFin         DATETIME     NOT NULL,
  lieuReservation VARCHAR(200) NOT NULL,
  etatReservation VARCHAR(20)  NOT NULL DEFAULT 'Pending',  -- Pending | Accepted | Rejected
  PRIMARY KEY (idClient, idAnnonce),
  KEY ix_reservation_annonce (idAnnonce),
  CONSTRAINT fk_reservation_client  FOREIGN KEY (idClient)  REFERENCES client (idClient)   ON DELETE CASCADE,
  CONSTRAINT fk_reservation_annonce FOREIGN KEY (idAnnonce) REFERENCES annonce (idAnnonce) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- une demande de rendez-vous (voir le véhicule) d'un client pour une annonce
CREATE TABLE IF NOT EXISTS rendezvous (
  idClient       INT UNSIGNED NOT NULL,
  idAnnonce      INT UNSIGNED NOT NULL,
  dateRendezVous DATETIME     NOT NULL,
  lieuRendezVous VARCHAR(200) NOT NULL,
  etatRendezVous VARCHAR(20)  NOT NULL DEFAULT 'Pending',
  PRIMARY KEY (idClient, idAnnonce),
  KEY ix_rendezvous_annonce (idAnnonce),
  CONSTRAINT fk_rendezvous_client  FOREIGN KEY (idClient)  REFERENCES client (idClient)   ON DELETE CASCADE,
  CONSTRAINT fk_rendezvous_annonce FOREIGN KEY (idAnnonce) REFERENCES annonce (idAnnonce) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
