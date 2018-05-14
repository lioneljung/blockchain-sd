# blockchain-sd
Simulation de blockchain

## Compilation
* make

## Simulation
* ./test-100.sh <nb_noeud> <nb_participants>
Le nombre maximum autorisé de noeud est de 15
Le nombre maximum autorisé de participants est de 50
Les fichiers sont stocké dans le dossier test/

## Vérification
Comparer les blockchain des noeuds:
* ./diff-blockchain.sh
Vérifier que toutes les opérations des participants sont stockés:
* ./check-operation.sh