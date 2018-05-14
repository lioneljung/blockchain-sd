import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Vector;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;

/**
 * @brief Class Core décrit un noeud du réseau (noeud block)
 */
public class Core extends UnicastRemoteObject implements NoeudBlockServices{

    /*******************
     **    DONNÉES    **
     *******************/

    private static final long serialVersionUID = 10L;
    public static final String nom = "core";
    private String pseudo;
    private double points;
    private BlockChain blockchain;

    /**
     * Liste des opérations envoyés aux blocs qui n'ont pas encore été
     * validés dans un nouveau bloc.
     */
    private Vector<Operation> operationAttente;

    /**
     * Liste des blocs orphelins
     */
    private Vector<Block> orphelins;

    /**
     * Voisins du noeud sous forme d'URL RMI:
     * rmi://host:port/core
     */
    private Vector<String> voisins;

    /**
     * Pseudo des participants du noeud sous forme d'URL RMI:
     * rmi://host:port/user
     */
    private Vector<String> participants;


    /**************************************************
     ** METHODES DIVERS (CONSTR, GETTER, SETTER,...) **
     **************************************************/

    /**
     * Constructeur
     */
    public Core(String s) throws RemoteException{
        pseudo = s;
        points = 0.0;
        operationAttente = new Vector<Operation>();
        voisins = new Vector<String>();
        participants = new Vector<String>();
        orphelins = new Vector<Block>();
        /* Recevoir une copie de la blockchain ? */
        blockchain = new BlockChain();
    }


    /**
     * @brief ajouer un nouveau voisin
     */
    public void addVoisin(String voisin, String noeud){
        try{
            voisins.add(voisin);
            NoeudBlockServices stub = (NoeudBlockServices)Naming.lookup(voisin);
            stub.connexion(noeud);
        } catch (Exception e){
            System.err.println("Impossible de se connecter à "+voisin);
            voisins.remove(voisins.lastElement());
            e.printStackTrace();
        }
    }


    /**
     * getters
     */
    public Vector<String> getVoisin(){
        return voisins;
    }
    public Vector<String> getParticipants(){
        return participants;
    }
    public BlockChain getBlockChain(){
        return blockchain;
    }
    public String getPseudo(){
        return pseudo;
    }
    public Vector<Operation> getOperationAttente(){
        return operationAttente;
    }


    /**
     * @brief supprimer les operation d'un block qu'on a en attente
     */
    void suppOpeDejaDansBlock(Block b){
        synchronized (operationAttente){
            for(int i = 0; i < b.getOperationList().size(); i++){
                for(int j = 0; j < operationAttente.size(); j++){
                    if(b.getOperationList().get(i).hashCode() 
                    == operationAttente.get(j).hashCode()){
                        operationAttente.remove(j);
                        j--;
                    }
                }
            }
        }
    }


    /**
     * @brief Créer un nouveau bloc à partir des opérations en attente
     * @return Le block créé
     */
    public Block creerBlock(){
        int prec;
        if(blockchain.getBlocks().isEmpty()){
            prec = 0;
        }
        else{
            prec = blockchain.getBlocks().lastElement().hashCode();
        }
        System.out.println("Création block: "+"prec="+prec);
        return new Block(prec, operationAttente);
    }


    /**
     * @brief Le noeud réalise une preuve de travail.
     * @return un boolean
     */
    public boolean preuveDeTravail(){
        System.out.println("Le noeud "+pseudo+" travaille");
        try{
            // pour l'instant, il ne fait rien pendant 3 à 8s
            Random r = new Random();
            long tempo = Math.round(3000 + (8000 - 3000) * r.nextDouble());
            System.out.println("Travail de "+tempo+" ms");
            TimeUnit.MILLISECONDS.sleep(tempo);
            System.out.println("Travail fournit!");
            // si j'ai des opérations en attentes, je crée un nouveau block
            if(!operationAttente.isEmpty()){
                Block b = creerBlock();
                // vider les operation en attente (= nouveau vecteur)
                operationAttente = new Vector<Operation>();
                // ajouter le bloc a la blockchain local
                blockchain.ajouterBlock(b);
                // distribuer le block
                distribuerBlock(b);
            }
        // gérer les exeption
        } catch (Exception e){
            System.err.println("Erreur preuve de travail: " + e.toString());
            e.printStackTrace();
        }
        return true;
    }


    /*******************************
     ** METHODES DE DISTRIBUTIONS **
     *******************************/

    /**
     * @brief Permet de distribuer un block aux voisins
     * @param b le block à distribuer
     */
    public void distribuerBlock(Block b){
        for(int i = 0; i < voisins.size(); i++){
            try {
                NoeudBlockServices stub = (NoeudBlockServices)Naming.lookup(voisins.get(i));
                stub.transmettreBlock(b);
                System.out.println("\tblock distribué à " + voisins.get(i));
            } catch (Exception e){
                System.err.println("Erreur distribution Block à "+voisins.get(i));
                e.printStackTrace();
            }
        }
    }


    /**
     * @brief Permet de distribuer un nouvel participant
     * @param s le pseudo de l'utilisateur à distribuer
     */
    public void distribuerUser(String s){
        for(int i = 0; i < voisins.size(); i++){
            try {
                NoeudBlockServices stub = (NoeudBlockServices)Naming.lookup(voisins.get(i));
                stub.transmettreUser(s);
                System.out.println("user distribué à " + voisins.get(i));
            } catch (Exception e){
                System.err.println("Erreur distribution User à "+voisins.get(i));
                e.printStackTrace();
            }
        }
    }


    /**
     * @brief Permet de distribuer une opération aux voisins
     * @param ope l'operation à distribuer
     */
    public void distribuerOperation(Operation ope){
        for(int i = 0; i < voisins.size(); i++){
            try {
                NoeudBlockServices stub = (NoeudBlockServices)Naming.lookup(voisins.get(i));
                stub.ajouterOperation(ope);
                System.out.println("operation distribué à " + voisins.get(i));
            } catch (Exception e){
                System.err.println("Erreur distribution Operation à "+voisins.get(i));
                e.printStackTrace();
            }
        }
    }



    /*******************************************************
     ** IMPLEMENTATION DES SERVICES DE L'INTERFACE REMOTE **
     *******************************************************/

    /**
     * @brief permet de transmettre la blockchain à un voisin
     * @param bc la blockchain à transmettre
     */
    public void transmettreBlockChain(BlockChain bc) throws RemoteException{
        System.out.println("transmettre block");
        if(bc.getBlocks().isEmpty()){
            // on a reçu une blockchain vide
            return;
        }
        // vérifier intégrité de la blockchain
        // comparer avec la blockchain actuelle
            // si c'est la meme ne rien changer
            // si différente et plus longue, la prendre
    }


    /**
     * @brief permet de transmettre un block à un voisin
     * @param b le block à transmettre
     */
    public void transmettreBlock(Block b) throws RemoteException{
        System.out.println("\t => appel de transmettre block");
        // vérifier l'intégrité du block
            // sinon le jeter
        // je regarde si j'ai déjà ce block
        if(!blockchain.containBlock(b.hashCode())){
            // selon le block précédent:
            if(b.getHashBlockPrecedent() == 0){
                // c'est un premier bloc d'une chaine
                System.out.println("Debut d'une nouvelle branche");
                if(blockchain.getBlocks().isEmpty()){
                    // on commence la blockchain
                    // supprimer opérations dans liste d'attente
                    suppOpeDejaDansBlock(b);
                    blockchain.ajouterBlock(b);
                    // on envoie le bloc reçu à nos voisins
                    distribuerBlock(b);
                }
                // autre début de blockchain. On l'ignore
            }
            else if(b.getHashBlockPrecedent() == blockchain.lastHash()){
                // le block reçu est le suivant de la blockchain qu'on a
                // supprimer opérations dans liste d'attente
                suppOpeDejaDansBlock(b);
                System.out.println("\n J'ai reçu le block qui suit ! \n");
                blockchain.ajouterBlock(b);
                distribuerBlock(b);
            }
            else if(blockchain.containBlock(b.getHashBlockPrecedent())){
                /** 
                 * On n'a pas encore ce block, mais on a son block précédent.
                 * Dans ce cas, on va comparer la taille du block reçu avec
                 * la taille du bloc suivant qu'on a localement et l'ajouter
                 * ou non en fonction de cela.
                 */
                int tmp = blockchain.getBlockByHash(b.getHashBlockPrecedent());
                if((tmp != -1) &&
                (b.getOperationList().size() > blockchain.getBlocks().get(tmp+1).getOperationList().size())){
                    // le nouveau block contient plus d'opération, on l'ajoute
                    // d'abord on supprime les blocks venant après lui
                    for(int i = tmp+1; i < blockchain.getBlocks().size(); i++){
                        blockchain.getBlocks().remove(tmp);
                    }
                    System.out.println("\n J'ai reçu un block plus gros ! \n");
                    blockchain.ajouterBlock(b);
                    distribuerBlock(b);
                }
            }
        }
        System.out.println("=> Je possède déjà le block: "+b.hashCode());
    }


    /**
     * @brief Permet de transmettre le pseudo d'un participant aux voisins
     * @param b Le pseudo de l'utilisateur
     */
    public void transmettreUser(String s) throws RemoteException{
        System.out.println("transmettreUser: " + s);
        voisins.add(s);
    }


    /**
     * @brief Permet à un participant d'ajouter une operation au noeud
     * @param o L'operation à ajouter
     */
    public void ajouterOperation(Operation ope) throws RemoteException{
        System.out.println("ajouter operation");
        // vérifier l'intégriter de l'operation
            // si intègre, l'ajouter à la liste
            // la jeter sinon
        if(!operationAttente.contains(ope)){
            operationAttente.add(ope);
            distribuerOperation(ope);
        }
        else{
            System.out.println(" => je possède déjà cette operation: "+ope.toString());
        }
    }


    /**
     * @brief Permet à un participant de s'inscrire auprès du noeud
     * @param s L'URL RMI du participant
     * @return Un boolean qui indique si l'inscription est acceptée
     */
    public boolean inscription(String s) throws RemoteException{
        if(!participants.contains(s)){
            participants.add(s);
        }
        System.out.println("Inscription du participant "+s);
        return true;
    }


    /**
     * @brief Permet à un noeud de se connecter à se noeud
     * @param s L'URL du noeud
     */
    public void connexion(String s) throws RemoteException{
        if(!voisins.contains(s)){
            voisins.add(s);
            System.out.println("Nouveau voisin: "+s);
        }
    }

}