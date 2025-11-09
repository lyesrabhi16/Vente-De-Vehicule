# Vente‑De‑Vehicule
Mini‑projet de fin de cycle Licence

## Description
Ce projet consiste en une application de gestion de vente de véhicules. Le but est de permettre à un utilisateur ou un vendeur de :
- Consulter la liste des véhicules disponibles à la vente
- Ajouter, modifier, ou supprimer un véhicule
- Gérer les informations relatives à chaque véhicule (marque, modèle, année, prix, etc.)
- Interface graphique et backend intégrés

L’application est construite en **Java / TypeScript / Kotlin** et utilise **Gradle** comme système de build.

## Fonctionnalités
- Affichage de la liste des véhicules
- Ajout d’un nouveau véhicule avec ses caractéristiques
- Modification et suppression des véhicules existants
- Recherche et filtrage selon les critères (marque, prix, année)
- Interface simple et ergonomique

## Technologies utilisées
- **Langages :** Java (~84.3 %), TypeScript (~14.1 %), Kotlin (~1.6 %)
- **Build Tool :** Gradle
- **Architecture :** Client / Serveur
- **Base de données :** (à préciser)
- **Frameworks/UI :** (ex. JavaFX, Swing, Spring Boot, React, Angular – à compléter selon le projet)

## Installation et exécution
1. Cloner le dépôt :
   ```bash
   git clone https://github.com/lyesrabhi16/Vente-De-Vehicule.git
   cd Vente-De-Vehicule
   ```
2. Construire le projet :
   ```bash
   ./gradlew build
   ```
3. Lancer le serveur :
   ```bash
   cd server
   ./gradlew run
   ```
4. Lancer le client :
   ```bash
   cd ../app
   ./gradlew run
   ```

> ⚠️ Selon la configuration, il peut être nécessaire d’ajouter des étapes supplémentaires (configuration de la base de données, variables d’environnement, etc.)

## Structure du projet
```
Vente-De-Vehicule/
├── app/                # partie cliente (UI)
├── server/             # partie serveur/backend
├── gradle/
│   └── wrapper/
├── build.gradle        # configuration globale
├── settings.gradle
├── gradlew
├── gradlew.bat
└── .gitignore
```

## Contribution
Ce projet est un mini-projet universitaire :  
- Vous pouvez le forker et l’adapter à vos besoins
- Issues et pull requests bienvenues

## Licence
Licence à définir (MIT, GPL, etc.). Si non spécifiée, usage académique uniquement.
