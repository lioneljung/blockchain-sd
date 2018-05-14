#!/bin/sh

#######################################################################
# Ce script permet de faire une simulation complète de la blockchain  #
# en indiquant le nombre de noeuds block et le nombre de participants #
# qui interagiront avec la chaine de blocs.                           #
#######################################################################

# le port sur lequel on lance le premier noeud; on lancera les noeuds 
# (et participants) suivants en incrémentant ce port
PORT=9000


# fonction usage: affiche l'usage du script
usage(){
    echo
    echo "Usage: $0 <nb_noeud> <nb_participants> <duree>"
    echo
    echo "--------------------------------------"
    echo "NOTE:"
    echo "<nb_noeud> comprit entre 1 et 15"
    echo "<nb_participants> comprit entre 1 et 50"
    echo "--------------------------------------"
    echo "NOTE: si la simulation ne fonctionne pas, vérifier que la plage"
    echo "de ports entre 9000 et 9000 + <nb_noeud> + <nb_participants>"
    echo "soit disponible. Sinon, modifier la variable PORT dans ce script."
    echo "--------------------------------------"
    exit -1
}


echo "====================="
echo "SIMULATION BLOCKCHAIN"
echo "====================="


# tester le nombre d'arguments
if [ $# -ne 2 ] ; then
    usage
fi
# tester la validité des arguments
if [ $1 -lt 1 ] || [ $1 -gt 15 ] ; then
    echo $1 " doit etre comprit entre 1 et 15"
    usage
fi
if [ $2 -lt 1 ] || [ $2 -gt 50 ] ; then
    echo $2 " doit etre comprit entre 1 et 50"
    usage
fi 


#
# Ici ajouter rmic Core et rmic User si besoin (obsolète)
#


# Nettoyage processus avant de commencer
pkill rmiregistry
rm -rf test
mkdir test
mkdir test/log


# créer <nb_noeud> de noeuds block
# CoreBin <pseudo> <port> <voisin>..<voisin>
for i in $(seq 0 $(($1-1))) ; do
    currentPort=$(($PORT + $i))
    # lancer le registre RMI du serveur associé en arrière plan
    rmiregistry $currentPort >&2 &
    echo "Démarrage rmiregistry $currentPort..."
    sleep 0.1 # laisser le registry démarrer
    # stocker le PID du registre lancé
    registries_core[$i]=$!
    # lancer le programme Java
    #### On détermine aléatoirement des voisins de ce noeud
    if [ $i -gt 0 ] ; then
        nbVoisin=`shuf -i 1-$i -n 1`
        voisin=""
        for k in $(seq 0 $nbVoisin) ; do
            p=`shuf -i 0-$i -n 1`
            p=$((9000+$p))
            `echo $voisin | grep $p > /dev/null`
            # on regarde si on n'a pas déjà ajouté ce port
            if [ $? -eq 1 ] && [ $(($p-9000)) -ne $(($i)) ] ; then
                voisin="$voisin $p"
            fi
        done
    fi
    ####
    # durée de vie du noeud 2 * le nombre de participants
    java CoreBin noeud$i $(($2*2)) $currentPort $voisin > test/log/core$i.log &
    cores[$i]=$!
    echo "Noeud créé - port = $currentPort - voisins = $voisin"
    sleep 0.5 # laisser le temps de démarrer
done
echo "On a lancé $(($i+1)) noeud(s)"


# laisser le temps aux noeuds de bien démarrer
sleep 1


# créer <nb_participants> de participants
# UserBin <pseudo> <port_user> <port_noeud_auquel_se_connecte>
for j in $(seq 1 $2) ; do
    currentPort=$(($PORT + $i + $j))
    # lancer le registre RMI du serveur associé en arrière plan
    rmiregistry $currentPort >&2 &
    echo "Démarrage rmiregistry $currentPort..."
    sleep 0.1
    # stocker le PID du registre lancé
    registries_user[$j]=$!
    # lancer le programme Java
    # On détermine aléatoirement sur quel noeud on va se connecter
    noeud=`shuf -i 0-$(($i)) -n 1`
    java UserBin user$j $currentPort $(($noeud+9000)) > test/log/user$j.log &
    users[$j]=$!
    echo "Participant créé - port = $currentPort - connecté au noeud = $(($noeud+9000))"
    # on attend 0.5s à 3s avant de lancer le prochain participant
    tempo=`shuf -i 500-3000 -n 1`
    sleep $(($tempo/1000))
done
echo "On a lancé $j participant(s)"


# on attend le temps de générer et distribuer les dernières infos
echo
echo "On attend 20 secondes..."
echo
sleep 20


# terminer les registres et noeuds block (s'il faut)
echo "Terminaison des noeuds block..."
for i in $(seq 0 $(($1-1))) ; do
    kill ${cores[i]}
    sleep 0.2
    kill ${registries_core[i]}
done


# terminer les participants
echo "Terminaison des participants..."
for i in $(seq 1 $2) ; do
    kill ${users[i]}
    sleep 0.2
    kill ${registries_user[i]}
done

sleep 0.2


# terminer vraiment les programmes (au cas où)
# (les rmiregistry se terminent toujours sans problème)
for i in $(seq 0 $(($1-1))) ; do
    kill -9 ${cores[i]}
done
for i in $(seq 1 $2) ; do
    kill -9 ${users[i]}
done


# tester si les fichiers des noeuds sont tous pareil:
# cela signifie que la blockchain a été bien distribué

echo
echo "La simulation est terminée. Veuillez vérifier les fichier générés dans le dossier test/ qui a été créé."
echo 
echo "=========================================================="
echo "NOTE: la configuration du réseau de noeud peut laisser des noeuds isolés"
echo "=========================================================="
echo

exit 0