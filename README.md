# Lecteur Local — lecteur de musique hors-ligne pour Android

Application Android native (Kotlin + Jetpack Compose + Media3) de lecture de
musique **stockée en local** sur le téléphone et la carte microSD. Conçue pour
Android 12 (testée comme cible Samsung Galaxy S10e), compatible Android 8.0+.

> ⚠️ **À lire en premier.** Ce dépôt contient le **code source complet** de
> l'application. Il n'a **pas** été compilé : aucun outil Android n'était
> disponible pour produire l'APK directement. Deux façons d'obtenir l'APK sont
> décrites plus bas — la plus simple (**GitHub Actions**) ne demande **aucun
> logiciel** sur votre PC. Comme l'application n'a pas pu être testée sur un
> appareil, un petit travail de correction dans Android Studio peut être
> nécessaire (voir la fin du document).

---

## Fonctionnalités

- **Mise en page façon Spotify** : barre de navigation en bas, contenu au-dessus.
- **Accueil** : dernières écoutes.
- **Dossiers** : parcourt téléphone + microSD, n'affiche que les dossiers
  contenant de l'audio, et dans ceux-ci uniquement les fichiers audio.
- **Playlists** enregistrées en local (base Room).
- **Recherche** : barre en haut, résultats en dessous, sur tout le stockage.
- **Menu contextuel** sur chaque titre : ajouter à une playlist, créer une
  playlist à partir du titre, ajouter à la file de lecture.
- **Sélection multiple** : ajouter plusieurs titres à une playlist / à la file.
- **Édition de playlist** : retirer un titre ou une sélection.
- **Page de lecture** : artiste, titre, repeat ; affiche en grand l'image
  `jpg`/`png` présente dans le dossier du titre, si elle existe.
- **Lecture en arrière-plan**, écran éteint (service Media3 + MediaSession).
- **Détail de playlist** : « Tout lire », « Lecture aléatoire », « Repeat all »,
  et choix d'une image de couverture (`jpg`/`png`).
- **Formats** : mp3, m4a, wav, flac, ogg… (codecs gérés par ExoPlayer/Android).
- **Lignes de titre** fines, multi-lignes : image du dossier à gauche,
  métadonnées à droite ; **aucun emplacement vide** si pas d'image.
- **Bouton avatar rond** en haut à gauche → réglages.
- **Réglages** : scan manuel, personnalisation de l'affichage, mode
  sombre/clair, lecture/affichage des métadonnées.
- Permissions minimales et conformes (`READ_MEDIA_AUDIO`/`READ_MEDIA_IMAGES`
  sur Android 13+, `READ_EXTERNAL_STORAGE` plafonné en dessous), pas d'accès
  réseau.

---

## Option A (recommandée) — Obtenir l'APK via GitHub Actions, sans rien installer

C'est la réponse à « un site pour transformer les sources en APK » : GitHub
compile pour vous, gratuitement, dans le cloud.

1. Créez un compte sur **https://github.com** (gratuit).
2. Cliquez sur **New repository**, donnez-lui un nom (ex. `lecteur-local`),
   laissez-le **Public**, puis **Create repository**.
3. Sur la page du dépôt vide, cliquez **uploading an existing file**, puis
   glissez-y **tout le contenu** de ce projet (décompressez le `.zip` d'abord,
   et conservez l'arborescence, notamment le dossier `.github`). Validez avec
   **Commit changes**.
4. Le build démarre tout seul. Ouvrez l'onglet **Actions** : une exécution
   « Build APK » apparaît (point orange = en cours, ✓ vert = terminé).
   *(Si rien ne démarre : onglet Actions → « Build APK » → « Run workflow ».)*
5. Quand c'est terminé (✓), cliquez sur l'exécution, descendez à
   **Artifacts**, et téléchargez **`app-debug-apk`**. Vous obtenez un `.zip`
   contenant `app-debug.apk`.

### Installer l'APK sur le Galaxy S10e
1. Copiez `app-debug.apk` sur le téléphone (câble USB, ou téléchargement direct).
2. Ouvrez-le avec l'explorateur de fichiers ; Android proposera d'autoriser
   « installer des applications inconnues » pour cette source → acceptez.
3. Installez, lancez **Lecteur Local**, accordez l'accès aux fichiers audio,
   puis allez dans **Réglages → Scanner** pour détecter vos morceaux.

> L'« APK de debug » est parfaitement utilisable au quotidien. L'« APK de
> release » produit en option est **non signé** et ne s'installe pas tel quel ;
> ignorez-le pour un usage personnel.

---

## Option B — Compiler soi-même avec Android Studio

1. Installez **Android Studio** (https://developer.android.com/studio).
2. **File → Open** et sélectionnez le dossier du projet.
3. Laissez Gradle se synchroniser (téléchargement des dépendances).
4. **Build → Build App Bundle(s) / APK(s) → Build APK(s)**.
5. L'APK se trouve dans `app/build/outputs/apk/debug/`.

C'est aussi l'outil à utiliser pour corriger d'éventuelles erreurs (voir plus bas).

---

## Pile technique

Kotlin 2.0, Jetpack Compose, Media3/ExoPlayer 1.4 (lecture + arrière-plan),
Room (playlists/historique), DataStore (réglages), Coil (images), Navigation
Compose. `minSdk 26`, `targetSdk 34`.

Arborescence : `app/src/main/java/com/example/localmusic/` (code), `…/res`
(ressources), `…/AndroidManifest.xml` (permissions et services).

---

## En cas d'erreur de compilation

Le code est complet mais n'a pas pu être compilé ni testé sur un appareil.
Si le build échoue :

- **Sur GitHub Actions** : ouvrez l'exécution en échec, dépliez l'étape
  « Compiler l'APK de debug » et lisez le message d'erreur (fichier + ligne).
- **Sur Android Studio** : les erreurs s'affichent dans l'onglet **Build** ;
  un clic mène directement à la ligne fautive. C'est l'environnement le plus
  pratique pour corriger.

La plupart des corrections éventuelles sont mineures (import manquant, nom à
ajuster). N'hésitez pas à me communiquer le message d'erreur exact : je vous
indiquerai la correction.

---

*Identifiant d'application : `com.example.localmusic`. Aucune donnée n'est
envoyée sur Internet ; tout reste sur l'appareil.*
