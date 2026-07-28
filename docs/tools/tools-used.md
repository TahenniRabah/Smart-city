## Profils Spring Boot

Chaque microservice utilise deux niveaux de configuration :

* `application.yml` pour la configuration commune et locale ;
* `application-prod.yml` pour l’exécution conteneurisée.

Le profil est activé dans Docker Compose :

```yaml
environment:
  SPRING\_PROFILES\_ACTIVE: prod
  KAFKA\_BOOTSTRAP\_SERVERS: kafka:9092

