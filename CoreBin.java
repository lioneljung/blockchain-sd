import java.io.PrintWriter;
import java.rmi.*;  
import java.rmi.registry.*;
import java.util.concurrent.TimeUnit;
import java.util.Vector;


/**
 * @brief cette class va permettre d'instancier un Noeud block
 */
public class CoreBin{

    /**
     * @brief instanciation du noeud du réseau
     * @param args[0] contient de pseudo a donner à l'instance du noeud
     * @param args[1] la durée d'execution du noeud
     * @param args[2] le port sur lequel on se connecte
     * @param args[3]+ les voisins de ce noeud
     * La durée de vie d'un noeud est infini. Il faut l'arrêter manuellement
     * (les scripts de tests s'en occupe)
     */
    public static void main(String[] args){
        // vérification des arguments
        if(args.length < 3){
            System.err.println("Usage: CoreBin pseudo duree port voisin..voisin");
            System.exit(-1);
        }
        String pseudo = args[0];
        int duree = Integer.valueOf(args[1]);
        int port = Integer.valueOf(args[2]);
        String url = "rmi://localhost:"+port+"/"+Core.nom;
        
        try{
            // création du noeud
            Core stub = new Core(pseudo);

            // ajout des voisins
            String tmp;
            for(int i = 3; i < args.length; i++){
                tmp = "rmi://localhost:"+args[i]+"/"+Core.nom;
                stub.addVoisin(tmp, url);
                System.out.println("Ajout du voisin "+tmp);
            }

            // Gérer la terminaison du programme (SIGINT notamment)
            Runtime.getRuntime().addShutdownHook(new Terminaison(stub));

            // exécution du serveur dans un thread
            ServeurCore serveur = new ServeurCore(port, stub);
            Thread instanceServeur = new Thread(serveur);
            instanceServeur.start();

            // attendre les autres noeuds
            //TimeUnit.MILLISECONDS.sleep(1000);

            // on fait travailler le noeud
            while(1<2){
                stub.preuveDeTravail();
            }
/*
            // on termine le programme
            Naming.unbind(url);
            instanceServeur.join(1);
            System.exit(0);
*/
        // gestion des exception
        } catch (Exception e){
            System.err.println("Erreur thread principal: "+e.toString());
            e.printStackTrace();
        }
    }

    /**
     * @brief Thread appelé lors de la terminaison du programme
     * il permet d'afficher la blockchain lors de la terminaison
     */
    static class Terminaison extends Thread{
        private Core core;
        Terminaison(Core c){
            core = c;
        }
        public void run(){
            try{
                System.out.println("Terminaison du programme");
                PrintWriter fichier = new PrintWriter("test/blockchain-"+core.getPseudo(), "UTF-8");
                String s = "";
                s += "VOISIN DE CE NOEUD:\n";
                for(int i = 0 ; i < core.getVoisin().size(); i++){
                    s += core.getVoisin().get(i) + "\n";
                }
                s += core.getBlockChain().toString();
                s += "\nOPÉRATIONS EN ATTENTES RESTANTES:\n";
                for(int i = 0; i < core.getOperationAttente().size(); i++){
                    s += core.getOperationAttente().get(i) + "\n";
                }
                fichier.println(s);
                fichier.close();
                System.exit(0);
            } catch (Exception e){
                System.err.println("Erreur fichier: "+e.toString());
            }
        }
    }
}


/**
 * @brief Thread du serveur pour le noeud block
 */ 
class ServeurCore extends Thread{

    private int port;
    Core stub;

    /**
     * constructeur
     */
    public ServeurCore(int po, Core st){
        port = po;
        stub = st;
    }

    /**
     * @brief Cette méthode est appelé lorsque le thread est lancé.
     * On crée le serveur.
     */
    public void run(){
        try{
            Naming.rebind("rmi://localhost:"+port+"/"+Core.nom, stub);
            System.out.println("Serveur  initialisé");
            System.out.println("URL: rmi://localhost:"+port+"/"+Core.nom);
        } catch (Exception e) {
            System.err.println("Erreur init serveurCore: " + e.toString());
            e.printStackTrace();
        }
    }
}