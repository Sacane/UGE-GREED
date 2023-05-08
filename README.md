# UGE GREED

UGE GREED est un projet effectué dans le contexte de notre année de Master 1 à l'université de Gustave Eiffel pour la matière "Programmation Réseau".

UGE Greed est un protocole permettant d'initier un réseau de calcul. Chaque réseau de calcul est initialisé par un serveur Root qui va
permettre à d'autres serveurs filles de s'y attacher, ce réseau de serveurs va permettre de distribuer les calculs effectués.

## Installation

Pour installer le projet, il vous suffit d'avoir maven d'installer sur votre machine, puis de lancer la commande suivante :

```bash
mvn clean install
```
Assurez-vous bien d'être situé à la racine de ce projet.
Ceci vous génèrera un fichier .jar dans le dossier target.

Vous avez maintenant deux possibilités pour lancer le projet :

```bash
java --enable-preview -jar <path/to/jar> <port>
```
Qui vous permettra d'initialiser une application Root sur le port que vous aurez choisi.

```bash
java --enable-preview -jar <path/to/jar> <port self> <ip target> <port target>
```
Qui vous permettra d'initialiser une application fille sur le port que vous aurez choisi, qui se connectera à l'application dont vous aurez spécifié l'ip et le port.
A noter que l'application courante tentera d'abord de se connecter à l'application parent avant de s'initialiser lui-même en tant que serveur et accepter d'autres connexion.

Lorsque votre serveur est initialisé, vous pouvez attendre ou initialiser vous même d'autres serveurs qui viendront s'ajouter au réseau.
Par la suite vous pourrez envoyer des conjectures à effectuer au réseau du serveur via la commande suivante :

```bash
START <lien vers le JAR> <nom de la classe implémentant l'interface Checker> <debut de la conjecture> <fin de la conjecture>
```

Vous pouvez également Shutdown votre serveur s'il n'est pas root via la commande suivante :

```bash
SHUTDOWN
```

Attention, vous ne pouvez shutdown une application root que si tous les serveurs filles sont déconnectés.

Auteurs:

- RAMAROSON RAKOTOMIHAMINA Johan
- TELLIER Quentin