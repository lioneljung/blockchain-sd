#!/bin/sh

######################################################################
# Tester l'établissement d'une communication entre 2 noeuds Block    #
######################################################################

PORT=9000

# lancer 2 noeud block
for i in $(seq 1 2) ; do
    currentPort=$(($PORT + $i - 1))
    # lancer le registre RMI du serveur associé en arrière plan
    rmiregistry $currentPort >&2 &
    # stocker le PID du registre lancé
    registries[$i]=$!
    # lancer le programme Java
    java CoreBin1 noeud$i $currentPort >&2 &
    cores[$i]=$!
done

echo "On a lancé" $i "noeud(s)"

sleep 10

# terminer les registres et noeuds block
for i in $(seq 1 2) ; do
    kill ${registries[i]}
    kill ${cores[i]}
done

exit 0