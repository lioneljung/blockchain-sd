#!/bin/sh


#######################################################
# Ce script regarde si toutes les opérations de chaque
# participants ont bien été ajouté dans la blockchain
# de chaque noeuds.
#

NOEUDS=`ls test/ | grep blockchain-noeud`
USER=`ls test/ | grep user-user`

for i in $USER ; do
    echo "----------------------------------"
    echo "VÉRIFICATION PARTICIPANT $i:"
    OPES=`cat test/$i | grep [0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9],*;`
    for j in $NOEUDS ; do
        for k in $OPES ; do
            #echo $k
            TMP=`cat test/$j | grep $k`
            if [ $? -eq 1 ] ; then
                echo " => $j ne contient pas l'opération $k"
            fi
            TMP=`cat test/$j | grep $k | wc -l`
            if [ $TMP -gt 1 ] ; then
                echo " => /!\ opération $k présente $TMP fois dans $j"
            fi
        done
    done
done