# Documentation personnelle du projet backend

Objectif: comprendre le dossier `Projet_m426_Back` et etre capable d'expliquer ou se trouve chaque partie importante du backend.

## 1. Presentation generale du projet

A completer:

- Nom du projet:
- Type de projet:
- Langage principal:
- Framework utilise:
- Base de donnees:
- Role du backend:

Exemple de phrase a reformuler:

> Ce projet est un backend Java Spring Boot qui expose une API REST pour une application de messagerie.

## 2. Structure globale du dossier

```text
Projet_m426_Back
├── src/                 # Code source Java et ressources Spring
├── data/                # Scripts SQL copies ou utilises pour la DB
├── docker/              # Configuration Docker et base de donnees
├── pom.xml              # Configuration Maven et dependances
├── README.md            # Instructions de lancement
├── Dockerfile           # Image Docker de l'application
├── reset-db.sh          # Script pour remettre la DB a zero
├── mvnw / mvnw.cmd      # Wrapper Maven
└── API_BACKEND_GUIDE.md # Guide genere precedemment, a utiliser comme aide
```

A completer:

- A quoi sert `src` ?
- A quoi sert `docker` ?
- A quoi sert `pom.xml` ?
- A quoi sert `README.md` ?

## 3. Dossier `src/main/java`

Chemin:

```text
src/main/java/ch/epai/ict/m295/messaging/backend
```

C'est le coeur de l'application Java.

```text
backend
├── api/                 # Partie HTTP/API
├── data/                # Acces a la base de donnees
├── domain/              # Objets metier et interfaces
├── AppConfig.java       # Configuration application
├── CorsConfig.java      # Configuration CORS
├── RepositoryConfig.java
└── MessagingApp.java    # Classe qui demarre Spring Boot
```

## 4. Dossier `api`

Le dossier `api` contient ce qui est expose au client HTTP.

```text
api
├── controllers/         # Les routes REST
├── dto/                 # Les objets JSON d'entree/sortie
├── security/            # Securite et authentification
└── OpenApiConfig.java   # Configuration Swagger/OpenAPI
```

### `api/controllers`

A completer avec tes mots:

- `UserController.java`:
- `TokenController.java`:
- `ConversationController.java`:
- `ParticipantController.java`:
- `MessageController.java`:

Question utile:

> Si je veux ajouter ou modifier une route HTTP, dans quel dossier je vais ?

Reponse:

```text
api/controllers
```

### `api/dto`

Les DTO sont les objets qui representent les donnees JSON recues ou envoyees par l'API.

A completer:

- Un DTO de creation sert a:
- Un DTO de response sert a:
- Exemple de DTO que j'ai compris:

Exemples de fichiers:

- `UserCreateDto.java`
- `UserResponseDto.java`
- `ConversationCreateDto.java`
- `MessageCreateDto.java`
- `MessageResponseDto.java`
- `ParticipantUpdateDto.java`

### `api/security`

A completer:

- `SecurityConfig.java` sert a:
- `TokenAuthenticationFilter.java` sert a:

Phrase aide:

> La securite verifie le token envoye dans le header `Authorization: Bearer ...` et retrouve l'utilisateur connecte.

## 5. Dossier `domain`

Le dossier `domain` contient les objets importants du metier.

Objets principaux:

- `User`
- `Conversation`
- `Participant`
- `Message`
- `MessageStatus`
- `Token`

Interfaces importantes:

- `UserRepository`
- `ConversationRepository`
- `TokenRepository`

A completer:

- `User` represente:
- `Conversation` represente:
- `Participant` represente:
- `Message` represente:
- `MessageStatus` represente:

Question utile:

> Quelle est la difference entre `domain/UserRepository.java` et `data/SqlUserRepository.java` ?

Ma reponse:

```text
A completer...
```

Indice:

`UserRepository` dit ce qu'on peut faire. `SqlUserRepository` explique comment on le fait avec SQL.

## 6. Dossier `data`

Le dossier `data` contient les classes qui parlent vraiment avec la base de donnees.

Fichiers importants:

- `SqlUserRepository.java`
- `SqlConversationRepository.java`
- `SqlTokenRepository.java`
- `SqlIdGenerator.java`
- `UserRowMapper.java`

A completer:

- `SqlUserRepository.java`:
- `SqlConversationRepository.java`:
- `SqlTokenRepository.java`:
- `SqlIdGenerator.java`:

Question utile:

> Si je veux changer une requete SQL, je vais ou ?

Reponse:

```text
src/main/java/.../backend/data
```

## 7. Dossier `src/main/resources`

```text
src/main/resources
├── application.yaml
└── db/migration/mariadb
    ├── V1__create-db.sql
    └── R__instert-demo-data.sql
```

A completer:

- `application.yaml` contient:
- `V1__create-db.sql` sert a:
- `R__instert-demo-data.sql` sert a:

Tables importantes:

- `user`
- `token`
- `conversation`
- `participant`
- `message`
- `message_status`
- `entity_counter`

## 8. Dossier `docker`

Le dossier `docker` sert a lancer le projet avec Docker.

A completer:

- `docker-compose.yaml` lance:
- `docker/data/1_create_db.sql` sert a:

Commande vue dans le README:

```bash
docker compose up --build
```

## 9. Fichiers racine importants

### `pom.xml`

A completer:

- Maven sert a:
- Les dependances servent a:
- Spring Boot est configure ici:

### `Dockerfile`

A completer:

- Le Dockerfile sert a:

### `README.md`

A completer:

- Le README explique:

### `reset-db.sh`

A completer:

- Ce script sert probablement a:

## 10. Les routes de l'API

### Utilisateurs

- `GET /users`
- `POST /users`
- `GET /users/{userId}`
- `PATCH /users/{userId}`
- `DELETE /users/{userId}`
- `GET /users/me`
- `PATCH /users/me`
- `DELETE /users/me`

A completer:

- Les routes users servent a:
- Le controller correspondant est:

### Tokens

- `POST /tokens`
- `DELETE /tokens/me`

A completer:

- Ces routes servent a:
- Le controller correspondant est:

### Conversations

- `GET /conversations`
- `POST /conversations`
- `GET /conversations/{conversationId}`

A completer:

- Ces routes servent a:
- Le controller correspondant est:

### Participants

- `POST /conversation/{conversationId}/participants`
- `PATCH /conversation/{conversationId}/participants/{participantId}`

A completer:

- Ces routes servent a:
- Le controller correspondant est:

### Messages

- `GET /conversation/{conversationId}/messages`
- `POST /conversation/{conversationId}/messages`
- `PATCH /conversation/{conversationId}/messages/{messageId}`

A completer:

- Ces routes servent a:
- Le controller correspondant est:

## 11. Ou travailler selon le changement demande

Complete ce tableau avec tes mots:

| Si je dois... | Je vais dans... | Pourquoi |
| --- | --- | --- |
| Ajouter une route API | `api/controllers` |  |
| Changer le JSON d'une requete | `api/dto` |  |
| Changer une requete SQL | `data` |  |
| Changer une table DB | `resources/db/migration` |  |
| Changer l'authentification | `api/security` |  |
| Changer une regle metier | `domain` ou controller concerne |  |
| Changer le port/base path/DB | `application.yaml` |  |

## 12. Resume personnel

A ecrire avec tes mots:

> Quand une requete HTTP arrive, elle passe d'abord par la securite, puis par un controller. Le controller utilise des DTO pour recevoir/envoyer du JSON. Ensuite il appelle un repository du domaine, qui est implemente dans le dossier data avec des requetes SQL.

Ma version:

```text
A completer...
```

