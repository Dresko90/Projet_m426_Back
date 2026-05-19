# Projet_m426_Back



Se placer dans le dossier /docket du projet :
Projet_m426_back/message-api-1/docker$

Créer les conteneur grâce au docker-compose.yaml :
docker compose up --build

Vérifier que les conteneurs soient bien créés en ligne de commande ou vérifier sur docker desktop :
ligne de commande : docker ps

Il doit y avoir 2 images qui tournent, message-api-1 et mariadb :
CONTAINER ID   IMAGE                                             COMMAND                  CREATED          STATUS          PORTS                                         NAMES
09dfe07c333f   glcr.epai-ict.ch/m295/java/message-api-1:latest   "java -jar /app/mess…"   54 minutes ago   Up 54 minutes   0.0.0.0:8080->8080/tcp, [::]:8080->8080/tcp   docker-messaging-api-1
5de376744a4d   mariadb:11.7.2                                    "docker-entrypoint.s…"   54 minutes ago   Up 54 minutes   3306/tcp                                      docker-db-1

Si ces deux images sont lancées, le backend est opérationnel