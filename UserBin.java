import java.rmi.*;  
import java.rmi.registry.*;
import java.util.Random;
import java.util.Vector;
import java.util.Random;
import java.io.PrintWriter;


/**
 * @brief cette classe va permettre d'instancier un participant
 */
public class UserBin{

    /**
     * @brief instanciation d'un participant
     * @param args[0] contient le pseudo du participant
     * @param args[1] le port sur lequel on se connecte
     * @param args[2] le noeud auquel se connecte le participant
     */
    public static void main(String[] args){
        // vérification des arguments
        if(args.length != 3){
            System.err.println("Usage: UserBin pseudo port noeud");
            System.exit(-1);
        }

        String pseudo = args[0];
        int port = Integer.valueOf(args[1]);
    
        try{
            // création du participant
            // génération d'un montant initial aléatoire
            Random r = new Random();
            double montant = 1 + (100 - 1) * r.nextDouble();
            User stub = new User(pseudo, montant);
            System.out.println("User créé avec "+montant+" points");

            // ajout des noeuds auxquels le participant se connecte
            // on ne gère qu'une seule connexion pour l'instant
            for(int i = 2; i < args.length; i++){
                stub.addNoeud("rmi://localhost:"+args[i]+"/"+Core.nom, "rmi://localhost:"+port+"/"+User.nom);
                System.out.println("Ajout du noeud rmi://localhost:"+args[i]+"/"+Core.nom);   
            }

            // Gérer la terminaison du programme (SIGINT notamment)
            Runtime.getRuntime().addShutdownHook(new TerminaisonBis(stub));
            
            // lancement services (serveur) dans un thread
            ServeurUser serveur = new ServeurUser(port, stub);
            Thread instanceServeur = new Thread(serveur);
            instanceServeur.start();
            
            // l'utilisateur va lancer de manière aléatoire des opérations
            stub.transactionsAleatoires();

            // terminer le programme
            Naming.unbind("rmi://localhost:"+port+"/"+User.nom);
            instanceServeur.join(1);
            System.exit(0);

        // gérer les exceptions
        } catch (Exception e) {
            System.err.println("Erreur UserBin: "+e.toString());
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * @brief Thread appelé lors de la terminaison du programme
     * il permet d'afficher l'historique des operations
     */
    static class TerminaisonBis extends Thread{
        private User user;
        TerminaisonBis(User u){
            user = u;
        }
        public void run(){
            try{
                PrintWriter fichier = new PrintWriter("test/user-"+user.getPseudo(), "UTF-8");
                fichier.println(user.resume());
                fichier.close();
                System.exit(0);
            } catch (Exception e){
                System.err.println("Erreur fichier: "+e.toString());
                e.printStackTrace();
            }
        }
    }
}


/**
 * @brief Thread qui exécute le serveur
 */
class ServeurUser implements Runnable{

    private int port;
    User stub;

    ServeurUser(int po, User st){
        port = po;
        stub = st;
    }

    public void run(){
        try{
            Naming.rebind("rmi://localhost:"+port+"/"+User.nom, stub);
            System.out.println("Serveur user initialise");
            System.out.println("URL: rmi://localhost:"+port+"/"+User.nom);
        } catch (Exception e) {
            System.err.println("Erreur init serveur: " + e.toString());
            e.printStackTrace();
        }
    }
}