import java.util.Date;
import java.io.Serializable;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

/**
 * @brief Décrit une opération
 */
public class Operation implements Serializable{

    private static final long serialVersionUID = 1L;

	/**
     * Montant de la transaction.
     */
    private double montant;
    
    /**
     * Le pseudo de l'auteur de la transaction
     */
    private String auteur;
    
    /**
     * Le pseudo du destinataire de la transaction
     */
    private String destinataire;

    /**
     * La date de la transaction
     */
    private Date date;

    /**
     * Constructeur
     */
    Operation(double m, String a, String d){
        montant = m;
        auteur = a;
        destinataire = d;
        date = new Date();
    }

    /**
     * Cette methode transforme l'operation en une chaine de caractère.
     * @return Une operation synthétisé dans une String
     */
    public String toString(){
        Format formatter = new SimpleDateFormat("dd-MM-yyy,HH:mm:ss");
        String s = formatter.format(date);
        return s+","+String.valueOf(montant)+","+auteur+","+destinataire+";";
    }

    /**
     * Implementation de la fonction hashCode() qui permet de hasher
     * l'objet Operation.
     * @return Un hash sous forme d'un int de 32 bits.
     */
    @Override
    public int hashCode(){
        int hash = 1;
        hash *= 3 + montant;
        hash *= 17 + auteur.hashCode();
        hash *= 17 + destinataire.hashCode();
        hash *= 19  + date.hashCode();
        return hash;
    }


    /**
     * @brief Fonction vérifie si 2 objets sont égaux
     * @param obj l'objet à comparer
     */
    @Override
    public boolean equals(Object obj) {
        if(obj.hashCode() == this.hashCode()){
            return true;
        }
        return false;
    }

}