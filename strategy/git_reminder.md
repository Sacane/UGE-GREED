# Strategie de notre utilisation de git en équipe


## 1. Avant de travailler

Je récupère la dernière version de la branche main

menu Git -> **Fetch**, puis selectionner la local branche main -> **update**
puis basculer sur sa branche (quentin, johan..) avec checkout puis selectionner main -> 'rebase **my branch** onto main'


## 2 Pendant que je travail

Lorsque je termine une fonction, une fonctionnalité, quelque chose d'explicite et qu'elle compile, je commit avec un message explicite sur ce que j'ai fait.

## 3. Quand je termine de travailler

Je push ma branche sur le serveur avec menu Git -> **Push** -> **Push my branch**

Le git master se chargera de merger ma branche avec la branche main et de résoudre les conflits s'il y en a.

Et ainsi de suite...