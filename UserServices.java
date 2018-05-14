import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @brief Cette interface décrit les services d'un participant
 */
public interface UserServices extends Remote{

    /**
     * @brief Permet à un noeud block d'augmenter le mérite
     * d'un participant
     * @param i Coefficient d'augmentation
     */
    public void augmenterMerite(int i) throws RemoteException;

}