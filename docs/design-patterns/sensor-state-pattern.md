\# Design Pattern State — Sensor / Capteur



\## Contexte



Dans l’architecture UrbanHub, les capteurs sont à l’origine des mesures envoyées vers la passerelle IoT puis vers le service d’ingestion.



Un capteur peut avoir plusieurs états :



\- ACTIVE

\- INACTIVE

\- FAULTY



Le comportement du capteur dépend de son état.



\## Problème



Sans design pattern, la classe `Sensor` pourrait contenir plusieurs conditions :



```java

if (status.equals("ACTIVE")) { ... }

if (status.equals("FAULTY")) { ... }

Cela rendrait la classe moins lisible, moins extensible et moins conforme aux principes POO.

## Solution

Le design pattern State permet d’isoler le comportement lié à chaque état dans une classe dédiée.

## Classes

Sensor
SensorState
ActiveSensorState
InactiveSensorState
FaultySensorState

## Responsabilités


### Sensor

- Encapsule l’identité du capteur.
- Expose les comportements métier.
- Délègue le comportement dépendant de l’état à `SensorState`.

### SensorState

- Définit le contrat commun des états.

### ActiveSensorState

- Autorise l’envoi de mesures.

### InactiveSensorState

- Interdit l’envoi de mesures.

### FaultySensorState

- Interdit l’envoi de mesures car le capteur est en panne.

---

## Principes POO respectés

### Encapsulation

Les attributs du capteur sont privés et uniquement exposés par des méthodes contrôlées.

### Single Responsibility Principle

Chaque état possède sa propre responsabilité.

### Open/Closed Principle

Il est possible d’ajouter un nouvel état sans modifier massivement la classe `Sensor`.

### KISS

La logique est simple et lisible.

---

## Tests

Les tests vérifient :

- qu’un capteur actif peut envoyer une mesure ;
- qu’un capteur inactif ne peut pas envoyer une mesure ;
- qu’un capteur défaillant ne peut pas envoyer une mesure ;
- que les transitions d’état fonctionnent ;
- que les données obligatoires sont validées.

