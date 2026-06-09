# Guide du backend API

Ce projet est une API REST Spring Boot pour une messagerie. Elle gere les utilisateur-rice-s, l'authentification par token, les conversations, les participant-e-s et les messages.

L'URL de base est:

```text
http://localhost:8080/api/v1
```

La documentation OpenAPI/Swagger est exposee par Springdoc:

```text
http://localhost:8080/api/v1/swagger-ui/index.html
http://localhost:8080/api/v1/v3/api-docs
```

## Vue d'ensemble

Le code est organise en couches:

```text
src/main/java/ch/epai/ict/m295/messaging/backend
├── api
│   ├── controllers     # Routes HTTP: users, tokens, conversations, messages, participants
│   ├── dto             # Objets d'entree/sortie JSON
│   ├── security        # Authentification par Bearer token
│   └── OpenApiConfig   # Configuration Swagger/OpenAPI
├── data                # Acces SQL concret avec JdbcTemplate
├── domain              # Modeles metier, interfaces repository, builders
├── AppConfig.java      # Lecture de configuration applicative
├── CorsConfig.java     # Configuration CORS
├── RepositoryConfig.java
└── MessagingApp.java   # Point d'entree Spring Boot
```

Les migrations SQL sont dans:

```text
src/main/resources/db/migration/mariadb
```

La configuration principale est:

```text
src/main/resources/application.yaml
```

## Authentification

L'API utilise un token envoye dans le header HTTP:

```http
Authorization: Bearer <token>
```

Le filtre qui lit ce header est `api/security/TokenAuthenticationFilter.java`. Il cherche le token en base, recupere l'utilisateur, puis place:

- `principal`: l'utilisateur connecte
- `token`: le token courant
- une autorite Spring Security de type `ROLE_ADMIN`, `ROLE_USER`, etc.

Les permissions globales sont dans `api/security/SecurityConfig.java`:

- Swagger est public.
- `POST /tokens` est public.
- `POST /users` est public.
- `GET /users` demande le role `ADMIN`.
- Le reste demande une authentification.

## Routes principales

### Tokens

Controleur: `api/controllers/TokenController.java`

| Methode | Route | Description | Acces |
| --- | --- | --- | --- |
| `POST` | `/tokens` | Connexion. Cree un token si username/password sont valides. | Public |
| `DELETE` | `/tokens/me` | Deconnexion. Supprime le token courant. | Authentifie |

Corps attendu pour `POST /tokens`:

```json
{
  "username": "sheana@example.com",
  "password": "epai321"
}
```

Retour:

```json
{
  "token": "..."
}
```

### Utilisateurs

Controleur: `api/controllers/UserController.java`

| Methode | Route | Description | Acces |
| --- | --- | --- | --- |
| `GET` | `/users?page=0&size=20` | Liste les utilisateurs avec pagination. | Admin |
| `POST` | `/users` | Cree un utilisateur. | Public |
| `GET` | `/users/{userId}` | Recupere un utilisateur. | Lui-meme ou admin |
| `PATCH` | `/users/{userId}` | Modifie username, displayName ou password. | Lui-meme ou admin |
| `DELETE` | `/users/{userId}` | Supprime un utilisateur et anonymise ses messages. | Lui-meme ou admin, sauf admin qui se supprime lui-meme |
| `GET` | `/users/me` | Recupere l'utilisateur authentifie. | Authentifie |
| `PATCH` | `/users/me` | Modifie l'utilisateur authentifie. | Authentifie |
| `DELETE` | `/users/me` | Supprime l'utilisateur authentifie. | Authentifie |

Le repository SQL correspondant est `data/SqlUserRepository.java`.

Points importants:

- Les mots de passe sont hashes avec `BCryptPasswordEncoder`.
- Changer le mot de passe supprime tous les tokens de l'utilisateur.
- Supprimer un utilisateur appelle aussi `conversationRepository.anonymizeAllUserMessages(userId)`.

### Conversations

Controleur: `api/controllers/ConversationController.java`

| Methode | Route | Description | Acces |
| --- | --- | --- | --- |
| `GET` | `/conversations?page=0&size=20` | Liste les conversations actives de l'utilisateur connecte. | Authentifie |
| `POST` | `/conversations` | Cree une conversation privee ou de groupe. | Authentifie |
| `GET` | `/conversations/{conversationId}` | Recupere le detail d'une conversation. | Participant |

Logique importante:

- Une conversation privee existante entre deux personnes est reutilisee.
- Une conversation de groupe est recreee a chaque appel.
- Les reponses sont en `application/hal+json` avec des liens HATEOAS.
- Les conversations ou le participant est `INACTIVE` ne sont pas listees.

Le repository SQL correspondant est `data/SqlConversationRepository.java`.

### Participants

Controleur: `api/controllers/ParticipantController.java`

Attention: les routes utilisent `/conversation/{conversationId}` au singulier.

| Methode | Route | Description | Acces |
| --- | --- | --- | --- |
| `POST` | `/conversation/{conversationId}/participants` | Ajoute des participants a une conversation. | Owner |
| `PATCH` | `/conversation/{conversationId}/participants/{participantId}` | Modifie role ou statut d'un participant. | Selon regles |

Regles principales:

- Seul un `OWNER` peut ajouter des participants.
- Un participant peut changer son propre statut seulement vers `INACTIVE`.
- Un owner peut modifier le role ou le statut d'un autre participant.
- Le statut accepte pour un owner est `ACTIVE` ou `BLOCKED`.

Modeles lies:

- `domain/Participant.java`
- `api/dto/ParticipantDto.java`
- `api/dto/ParticipantUpdateDto.java`
- `api/dto/ParticipantsAddDto.java`

### Messages

Controleur: `api/controllers/MessageController.java`

Attention: les routes utilisent aussi `/conversation/{conversationId}` au singulier.

| Methode | Route | Description | Acces |
| --- | --- | --- | --- |
| `GET` | `/conversation/{conversationId}/messages?page=0&size=20` | Liste les messages visibles pour l'utilisateur. | Participant |
| `POST` | `/conversation/{conversationId}/messages` | Ajoute un message dans la conversation. | Authentifie |
| `PATCH` | `/conversation/{conversationId}/messages/{messageId}` | Marque un message comme lu ou supprime pour l'utilisateur courant. | Authentifie |

Regles principales:

- `GET` verifie que l'utilisateur est participant.
- Les messages supprimes pour un utilisateur ne sont plus retournes pour cet utilisateur.
- `PATCH` accepte soit `{ "read": true }`, soit `{ "delete": true }`, mais pas les deux.
- La suppression est logique et personnelle: elle change `message_status.deleted`.

Le repository SQL correspondant est `data/SqlConversationRepository.java`.

## Modele de donnees

Les entites SQL sont creees dans `V1__create-db.sql`.

| Table | Role |
| --- | --- |
| `user` | Comptes, mot de passe hashe, role, nom affiche |
| `token` | Tokens d'authentification actifs |
| `conversation` | Conversation privee ou groupe |
| `participant` | Lien utilisateur/conversation, avec role et statut |
| `message` | Message envoye dans une conversation |
| `message_status` | Etat du message par utilisateur: lu/supprime |
| `entity_counter` | Compteurs pour generer les identifiants |

Les objets Java correspondants sont dans `domain`:

- `User`
- `Conversation`
- `Participant`
- `Message`
- `MessageStatus`
- `Token`

Les builders creent ces objets et utilisent souvent `IdGeneratorManager` pour attribuer les IDs.

## Ou travailler selon le besoin

| Tu veux changer... | Va surtout dans... |
| --- | --- |
| Une route HTTP, un statut HTTP, une validation de requete | `api/controllers/*Controller.java` |
| Le JSON recu ou retourne | `api/dto/*.java` |
| Une regle metier simple liee aux conversations | `domain/Conversation.java`, puis controleur concerne |
| Une regle participant/message plus concrete | `ParticipantController.java` ou `MessageController.java` |
| Une requete SQL, pagination, tri, filtre, insertion | `data/Sql*Repository.java` |
| Les tables ou donnees de demo | `src/main/resources/db/migration/mariadb/*.sql` |
| L'authentification ou les droits globaux | `api/security/SecurityConfig.java` et `TokenAuthenticationFilter.java` |
| La creation des repositories | `RepositoryConfig.java` |
| Le chemin de base `/api/v1` ou la DB | `application.yaml` |
| La documentation Swagger | Annotations OpenAPI dans les controleurs |

## Flux typiques

### Connexion

1. Le client appelle `POST /tokens`.
2. `TokenController` verifie username/password avec `UserRepository.validate`.
3. `SqlUserRepository` compare le password avec BCrypt.
4. Un token aleatoire est cree et insere via `SqlTokenRepository`.
5. Le client reutilise ce token dans `Authorization: Bearer ...`.

### Lire les conversations

1. Le client appelle `GET /conversations`.
2. `TokenAuthenticationFilter` met l'utilisateur dans `principal`.
3. `ConversationController` appelle `conversationRepository.getConversationsByUser`.
4. `SqlConversationRepository` filtre les conversations ou le participant n'est pas `INACTIVE`.
5. Le controleur retourne une reponse HAL avec liens vers detail, participants et messages.

### Envoyer un message

1. Le client appelle `POST /conversation/{conversationId}/messages`.
2. `MessageController` verifie que la conversation existe.
3. Il cree un `Message` via `MessageBuilder`.
4. `SqlConversationRepository.createMessage` insere le message et les lignes `message_status` pour les participants actifs.

### Supprimer un message pour soi

1. Le client appelle `PATCH /conversation/{conversationId}/messages/{messageId}` avec `{ "delete": true }`.
2. `MessageController` valide la requete.
3. `SqlConversationRepository.markMessageAsDeletedForUser` met `deleted = TRUE`.
4. Les prochains `GET /messages` ne retournent plus ce message pour cet utilisateur.

## Points a surveiller

- Certaines routes utilisent `/conversations` au pluriel et d'autres `/conversation/{id}` au singulier. C'est volontaire dans le code actuel, mais c'est facile de se tromper.
- Plusieurs controles de securite sont faits dans les controleurs, pas seulement dans `SecurityConfig`.
- `MessageController.createMessages` verifie que la conversation existe, mais ne verifie pas explicitement que l'utilisateur courant est participant avant d'envoyer un message.
- `MessageController.updateMessageStatus` ne verifie pas explicitement que le message appartient a la conversation fournie.
- La pagination calcule le lien `last` avec `totalElements / pageSize`, ce qui peut donner une page trop loin quand `totalElements` est un multiple exact de `pageSize`.
- Les DTOs et les exemples Swagger sont utiles pour comprendre les payloads attendus.

## Ordre conseille pour reprendre le projet

1. Lire `README.md` pour lancer le projet avec Docker.
2. Ouvrir Swagger et tester `POST /users`, `POST /tokens`, puis `GET /users/me`.
3. Lire `SecurityConfig.java` et `TokenAuthenticationFilter.java` pour comprendre l'auth.
4. Lire les controleurs dans cet ordre: `UserController`, `TokenController`, `ConversationController`, `ParticipantController`, `MessageController`.
5. Lire `ConversationRepository.java` puis `SqlConversationRepository.java`, car beaucoup de logique API finit dans cette classe.
6. Lire `V1__create-db.sql` pour comprendre les tables.

