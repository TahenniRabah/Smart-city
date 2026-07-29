\## GL-01 - Clé générique dans un rapport de test



| Attribut | Valeur |

|---|---|

| Règle | `generic-api-key` |

| Fichier | `Test Results - IngestionControllerValidationTest.xml` |

| Qualification | Faux positif |

| Contexte | Rapport de test généré contenant une valeur réservée aux tests automatisés |

| Secret opérationnel | Non |

| Action corrective | Retrait du rapport généré du dépôt |

| Prévention | Ajout du fichier au `.gitignore` |

| Traitement historique | Fingerprint ajouté à `.gitleaksignore` |

| Risque résiduel | Faible |



La valeur détectée est une donnée fictive utilisée uniquement dans les tests

automatisés. Elle ne donne accès à aucun environnement UrbanHub.



L’exclusion est limitée au fingerprint historique exact. Les futures détections

de la règle `generic-api-key` restent bloquantes. positif | Révocation, suppression ou exclusion justifiée | Ouvert/Fermé |

