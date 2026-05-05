\# Air Quality Service



Microservice UrbanHub responsable du calcul du niveau d'alerte qualité de l'air.



\## Règle métier



Le service calcule un niveau d'alerte à partir d'une mesure de pollution.



\### NO2



\- valeur < 100 : NORMAL

\- valeur >= 100 et < 200 : WARNING

\- valeur >= 200 : CRITICAL



\### PM10



\- valeur < 50 : NORMAL

\- valeur >= 50 et < 80 : WARNING

\- valeur >= 80 : CRITICAL



\## Lancer les tests



```bash

mvn test

