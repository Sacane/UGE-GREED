# Guide pour les trams

## 1. Les 3 types de trames de routage

- LOCAL: 0x00 -> Aucun comportement particulier
- TRANSFERT: 0x01 -> 1 (BYTE) opcode (BYTE) id_src (ID) id_dst (ID) payload (BYTES)
- BROADCAST: 0x02
- TO_ROOT: 0x03

## 1.2 Trame TRANSFERT

La trame transfert est la trame de routage la plus utilisée. Elle permet de transférer des données d'un noeud à un autre. Elle est composée de 4 parties:
 
  * 0x01 byte identifier
  * opcode: 1 octet, permet de définir le type de trame
  * id_src: socketAddress, identifiant du noeud source
  * id_dst: socketAddress, identifiant du noeud destination
  * payload: 1 à 255 octets, données à transmettre

Si l'id_dst est l'id de l'application elle-même, alors le payload est traité par l'application. Sinon, le payload est transmis à l'application de destination selon sa table de routage.

## 1.3 Trame BROADCAST

La trame BROADCAST est constituée de 4 parties : 

  * 0x02 byte identifier
  * opcode: 1 octet, permet de définir le type de trame
  * id_src: socketAddress, identifiant du noeud source
  * id_dst: socketAddress, identifiant du noeud destination
  * payload: 1 à 255 octets, données à transmettre

Quand une application reçoit une trame Broadcast elle transmet à l'identique la trame à tous ses voisins.

## 1.4 Trame TO_ROOT

La trame to_root est constituée :

* 0x03 byte identifier
* opcode: 1 octet, permet de définir le type de trame
* id_src: socketAddress, identifiant du noeud source
* payload: 1 à 255 octets, données à transmettre


## 2. La liste des comportements 

### 2.1 Une application CONNECTED (pas root)

1. envoi de la trame à l'application sur lequel elle s'est connectée : 

CONNECT: LOCAL(0x00) opcode(0x01) id (SOCKETADDRESS)

2. Reception de la trame de réponse : 

- OK: LOCAL(0x00) opcode(0x03) id_mother (SOCKETADDRESS de la mère) id_address (SOCKETADDRESS liste des ids des applications dans la table de mothers)
- KO: LOCAL(0x00) opcode(0x02) id (SOCKETADDRESS)