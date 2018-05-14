import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;
import java.rmi.Naming;
import java.util.Random;
import java.util.Vector;

public class User extends UnicastRemoteObject implements UserServices{

	private static final long serialVersionUID = 20L;

    /**************
     ** ATTRIBUT **
     **************/

    public static final String nom = "user";
    private String pseudo;
    private double points;
    private int merite;
    private String clePrive;
    public String clePublique;
    private BlockChain blockchain;

    /**
     * Les noeuds auxquels le participant se connecte 
     * sous forme d'URL RMI
     */
    private Vector<String> noeuds;

    /**
     * Historique des transactions effectués par ce noeuds
     */
    public Vector<Operation> archive;


    /******************
     **   METHODES   **
     ******************/

    /**
     * constructeur
     */
    public User(String s, double p) throws RemoteException{
        pseudo = s;
        points = p;
        merite = 0;
        // generer cle prive
        clePrive = "";
        // generer cle publique
        clePublique = "";
        archive = new Vector<Operation>();
        
        /**
         *  SE CONNECTER AU RESEAU BLOCKCHAIN
         *  (fait dans UserBin pour l'instant)
         */
        noeuds = new Vector<String>();

        /** 
         * DEMANDER BLOCKCHAIN AU RESEAU
         */
    }

    /**
     * getter
     */
    public String getPseudo(){
        return pseudo;
    } 

    /**
     * @brief Implémentation du service augmenterMerite
     * @param i de combien on augmente le mérite
     */
    public void augmenterMerite(int i) throws RemoteException{
        merite += i;
    }

    /**
     * @brief Permet au participant d'effectuer une transaction et de la distribuer
     * @param montant Le montant de la transaction
     * @param dest Le pseudo du destinataire
     */
    private void effectuerTransaction(double montant, String dest){
        Operation operation = new Operation(montant, pseudo, dest);
        for(int i = 0; i < noeuds.size(); i++){
            try{
                NoeudBlockServices stub = (NoeudBlockServices)Naming.lookup(noeuds.get(i));
                stub.ajouterOperation(operation);
                System.out.print("Transaction de "+montant);
                System.out.println(" envoyé à "+noeuds.get(i));
                // on sauvegarde cette transaction (pour la simulation)
                archive.add(operation);
            } catch (Exception e){
                System.err.println("Erreur transaction: "+e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * @brief ajouter un noeuds à la liste
     * @param noeud l'url RMI du noeud à ajouter
     * @param user URL RMI du participant
     */
    public void addNoeud(String noeud, String user){
        try{
            NoeudBlockServices stub = (NoeudBlockServices)Naming.lookup(noeud);
            if(stub.inscription(user)){
                noeuds.add(noeud);
            }
            else{
                System.out.println("Le noeud "+noeud+" a refusé l'inscription");
            }
        } catch (Exception e){
            System.err.println("Erreur inscription à "+noeud);
            e.printStackTrace();
        }
    }

    /**
     * @brief Cette fonction simule la création de transactions à intervalle
     * aléatoire.
     */
    public void transactionsAleatoires(){
        System.out.println("Transactions aléatoires");
        while(points > 0.1){
            // demander la blockchain pour vérifier notre solde
            /**
             * 
             */
            Random r = new Random();
            double montant = 0.5 + (points - 0.5) * r.nextDouble();
            effectuerTransaction(montant, "dest");
            points -= montant;
            // attente aléatoire avant la prochaine transaction
            Random s = new Random();
            long attente = Math.round(300 + (1000 - 300) * s.nextDouble());
            try{
                TimeUnit.MILLISECONDS.sleep(attente);
            } catch (Exception e) {
                System.err.println("transaction TimeUnit erreur");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        System.out.println("Fin transactions aléatoires");
    }

    /**
     * @brief écrit un résumé de ce qu'a fait ce participant
     */
    public String resume(){
        String r = "";
        r += pseudo + " connecté à " + noeuds.firstElement() + "\n"; 
        for(int i = 0; i < archive.size(); i++){
            r += archive.get(i).toString();
            r += "\n";
        }
        return r;
    }

}