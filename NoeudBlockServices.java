import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @brief Cette interface décrit les services d'un noeud block.
 */
public interface NoeudBlockServices extends Remote{

    /**
     * @brief Transmettre la blockchain a un noeud
     * @param bc La blockchain à transmettre
     */
    public void transmettreBlockChain(BlockChain bc)
        throws RemoteException;
    
    /**
     * @brief Transmettre un block aux voisins
     * @param b Le block a transmettre
     */
    public void transmettreBlock(Block b)
        throws RemoteException;

    /**
     * @brief Transmettre un utilisateur
     * @param s Le pseudo de l'utilisateur
     */
    public void transmettreUser(String s)
        throws RemoteException;

    /**
     * @brief Ajouter une operation à la liste des opérations
     * @param o L'operation à ajouter
     */
    public void ajouterOperation(Operation o)
        throws RemoteException;

    /**
     * @brief Permet à un utilisateur de s'inscrire
     * @param s Le pseudo de l'utilisateur qui s'inscrit
     */
    public boolean inscription(String s)
        throws RemoteException;

    /**
     * @brief Permet à un noeud de se connecter à se noeud
     * @param s L'URL du noeud
     */
    public void connexion(String s) 
        throws RemoteException;

}