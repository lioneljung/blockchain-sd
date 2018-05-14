import java.io.Serializable;
import java.util.Vector;


/**
 * @brief Décrit la blockchain
 */
public class BlockChain implements Serializable{
 
    private static final long serialVersionUID = 3L;

	/**
     * liste des blocks de la blockchain
     */
    private Vector<Block> blocks;

    
    /**
     * @brief Ajouter un block à la block chain
     * @return Un boolean qui indique si on a ajoué le block ou pas
     */
    public boolean ajouterBlock(Block b){

        /**
         * VERIFIER SI LE BLOCK PEUT ETRE AJOUTER
         */
        
        blocks.add(b);
        System.out.println("BLOCK AJOUTE A LA BLOCKCHAIN:\n"+b.toString()+"\n");
        return true;
    }

    /**
     * constructeur
     */
    public BlockChain(){
        blocks = new Vector<Block>();
    }

    /**
     * getter
     */
    public Vector<Block> getBlocks(){
        return blocks;
    }

    /**
     * @brief Vérifie que la blockchain est valide
     */
    public boolean checkValidite(){
        return true;
    }

    /**
     * @brief Renvoie le hash du dernier block ajouter à la blockchain
     * @return un hash
     */
    int lastHash(){
        return blocks.get(blocks.size()-1).hashCode();
    }

    /**
     * @brief Vérifie si la blockchain contient déjà le bloc avec le hash
     */
    public boolean containBlock(int hash){
        boolean r = false;
        int i = blocks.size()-1;
        while(!r && (i >= 0 )){
            if(blocks.get(i).hashCode() == hash){
                r = true;
            }
            i--;
        }
        return r;
    }


    /**
     * @brief Renvoie le block avec le hash spécifié
     */
    public int getBlockByHash(int hash){
        int k = blocks.size()-1, r = -1;
        while((r == -1) && (k >= 0)){
            if(blocks.get(k).hashCode() == hash){
                r = k;
            }
            k--;
        }
        return r;
    }


    /**
     * @brief Transforme la blockchain en chaine de caractères
     * @return La blockchain présentée en format texte
     */
    public String toString(){
        String s = "\n";
        s += "=======================================\n";
        s += "============== BLOCKCHAIN =============\n";
        s += "=======================================\n";
        for(int i = 0; i < blocks.size(); i++){
            s += "BLOC N°" + i + ":\n";
            s += blocks.get(i).toString();
            s += "---------------------------------------\n";
        }
        s += "=======================================\n";
        return s;
    }

}