#!/bin/sh


######################################################
# Ce script détermine si les blockchains des noeuds à 
# la fin de la simulation ont des différences.
#

FILES=`ls test/ | grep blockchain-noeud`

for i in $FILES ; do
    echo "--------------------------"
    echo "VERIFICATION FICHIER $i..."
    echo
    TMP=`echo $FILES | sed 's/$i//'`
    for j in $TMP ; do
        RESULT=`diff test/$i test/$j | grep [0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9],*;`
        # si grep retourne quelque chose, on a détécté une différence
        if [ $? -eq 0 ] ; then
            echo " => La blockchain du noeud $j est différente!"
            ##echo $RESULT
        fi
    done
done