# Projet_m426_Back

Se placer dans le dossier /docker du projet :
Projet_m426_back/message-api-1/docker$

Créer les conteneurs grâce au docker-compose.yaml :
docker compose up --build

Vérifier que les conteneurs soient bien créés en ligne de commande ou vérifier sur docker desktop :
ligne de commande : docker ps

Il doit y avoir 2 images qui tournent, message-api-1 et mariadb :

CONTAINER ID   IMAGE                                             COMMAND                  CREATED          STATUS          PORTS                                         NAMES
09dfe07c333f   glcr.epai-ict.ch/m295/java/message-api-1:latest   "java -jar /app/mess…"   54 minutes ago   Up 54 minutes   0.0.0.0:8080->8080/tcp, [::]:8080->8080/tcp   docker-messaging-api-1
5de376744a4d   mariadb:11.7.2                                    "docker-entrypoint.s…"   54 minutes ago   Up 54 minutes   3306/tcp                                      docker-db-1

Si ces deux images sont lancées, le backend est opérationnel

Routes disponibles :

    Utilisateurs :

    GET     http://localhost:8080/users
    POST http://localhost:8080/users
    GET http://localhost:8080/users/{userId}
    PATCH http://localhost:8080/users/{userId}
    DELETE http://localhost:8080/users/{userId}
    GET http://localhost:8080/users/me
    PATCH http://localhost:8080/users/me
    DELETE http://localhost:8080/users/me


    Conversations :

    GET     http://localhost:8080/conversations
    POST    http://localhost:8080/conversations
    GET     http://localhost:8080/conversations/{conversationId}


    Messages : 

    GET     http://localhost:8080/conversation/{conversationId}/messages
    POST    http://localhost:8080/conversation/{conversationId}/messages
    PATCH   http://localhost:8080/conversation/{conversationId}/messages/{messageId}


    Participants : 

    POST    http://localhost:8080/conversation/{conversationId}/participants
    PATCH   http://localhost:8080/conversation/{conversationId}/participants/{participantId}


    Authentification / tokens :

    POST    http://localhost:8080/tokens
    DELETE  http://localhost:8080/tokens/me