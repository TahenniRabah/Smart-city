\# Atelier 2 — Micro-boucle XP sur Air Quality Service



\## 1. Contexte



Dans le cadre du projet Smart City UrbanHub, nous avons choisi le microservice `air-quality-service`.



Ce microservice est responsable du calcul du niveau d'alerte qualité de l'air à partir d'une mesure de pollution.



Règle implémentée :



\- NO2 < 100 : NORMAL

\- NO2 >= 100 et < 200 : WARNING

\- NO2 >= 200 : CRITICAL

\- PM10 < 50 : NORMAL

\- PM10 >= 50 et < 80 : WARNING

\- PM10 >= 80 : CRITICAL



\## 2. Test d'acceptation



User story :



En tant qu'agent environnement, je veux que le microservice Air Quality calcule un niveau d'alerte à partir d'une mesure de pollution afin d'identifier rapidement les épisodes de pollution.



Critère d'acceptation :



```gherkin

Feature: Calcul du niveau d'alerte qualité de l'air



Scenario: Détection d'une alerte critique NO2

\&#x20; Given une mesure NO2 de valeur 220 dans la zone ZFE-1

\&#x20; When le microservice Air Quality analyse la mesure

\&#x20; Then le niveau d'alerte retourné est CRITICAL


