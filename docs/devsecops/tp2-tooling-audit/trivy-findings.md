# Résultats Trivy filesystem

## Commande
Le dépôt UrbanHub a été analysé avec Trivy filesystem afin de détecter les vulnérabilités connues dans les dépendances des quatre microservices.

## Résumé

| Indicateur | Valeur |
|---|---:|
| Nombre total de findings HIGH/CRITICAL | 26 |
| CRITICAL | 6 |
| HIGH | 20 |
| Findings disposant d’un correctif | 26 |
| Services concernés | air-quality-service, alerting-service |

## Qualification

| ID | CVE | Service | Dépendance | Version | Version corrigée | Sévérité | Qualification | Action proposée |
|---|---|---|---|---|---|---|---|---|
| TRIVY-01 | CVE-2026-41293 | air-quality-service, alerting-service | org.apache.tomcat.embed:tomcat-embed-core | 11.0.21 | 11.0.22 | CRITICAL | Vulnérabilité Tomcat permettant une validation incorrecte des en-têtes HTTP/2 pouvant être exploitée à distance | Mettre à jour Tomcat vers 11.0.22 |
| TRIVY-02 | CVE-2026-43512 | air-quality-service, alerting-service | org.apache.tomcat.embed:tomcat-embed-core | 11.0.21 | 11.0.22 | CRITICAL | Contournement d'authentification Digest dans Tomcat | Mettre à jour Tomcat vers 11.0.22 |
| TRIVY-03 | CVE-2026-43515 | air-quality-service, alerting-service | org.apache.tomcat.embed:tomcat-embed-core | 11.0.21 | 11.0.22 | CRITICAL | Contournement de contrôles d'autorisation | Mettre à jour Tomcat vers 11.0.22 |
| TRIVY-04 | CVE-2026-41284 | air-quality-service, alerting-service | org.apache.tomcat.embed:tomcat-embed-core | 11.0.21 | 11.0.22 | HIGH | Risque de déni de service par consommation excessive de ressources | Mettre à jour Tomcat vers 11.0.22 |
| TRIVY-05 | CVE-2026-41731 | air-quality-service, alerting-service | org.springframework.kafka:spring-kafka | 4.0.5 | 4.0.6 | HIGH | Exécution de code via désérialisation non sécurisée | Mettre à jour Spring Kafka vers 4.0.6 |
| TRIVY-06 | CVE-2026-42498 | air-quality-service, alerting-service | org.apache.tomcat.embed:tomcat-embed-core | 11.0.21 | 11.0.22 | HIGH | Divulgation d'informations pendant l'authentification WebSocket | Mettre à jour Tomcat vers 11.0.22 |
| TRIVY-07 | CVE-2026-43513 | air-quality-service, alerting-service | org.apache.tomcat.embed:tomcat-embed-core | 11.0.21 | 11.0.22 | HIGH | Gestion incorrecte de la casse dans LockOutRealm | Mettre à jour Tomcat vers 11.0.22 |
| TRIVY-08 | CVE-2026-54512 | air-quality-service, alerting-service | jackson-databind | 2.21.2 / 3.1.2 | 2.21.4 / 3.1.4 | HIGH | Exécution de code arbitraire via contournement de PolymorphicTypeValidator | Mettre à jour Jackson |
| TRIVY-09 | CVE-2026-54513 | air-quality-service, alerting-service | jackson-databind | 2.21.2 / 3.1.2 | 2.21.4 / 3.1.4 | HIGH | Contournement de mécanismes de sécurité de Jackson pouvant mener à une exécution de code | Mettre à jour Jackson |
| TRIVY-10 | GHSA-r7wm-3cxj-wff9 | air-quality-service, alerting-service | jackson-core | 2.21.2 / 3.1.2 | 2.21.4 / 3.1.4 | HIGH | Contournement de limite de taille de nombres dans le parseur asynchrone | Mettre à jour Jackson |