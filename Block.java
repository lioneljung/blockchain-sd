import java.io.Serializable;
import java.util.Vector;
import java.util.Date;

/**
 * @brief Décrit un bloc 
 */
public class Block implements Serializable{

    private static final long serialVersionUID = 2L;

	/**
     * Le hash du block précédent. Cela va permettre de chainer les
     * bloks. Pour le premier block, on met 0.
     */
    private int hashBlockPrecedent;

    /**
     * Hash du bloc pour vérifier l'intégrité du bloc.
     */
    private int sommeControle;

    /**
     * La liste des operation contenu dans le block.
     */
    private Vector<Operation> operationList;

    /**
     * Datage de la création du block
     */
    Date date;

    /**
     * constructeur
     */
    public Block(int prec, Vector<Operation> ope){
        hashBlockPrecedent = prec;
        operationList = ope;
        date = new Date();
    }

    /**
     * @return Le hash du block precedent
     */
    public int getHashBlockPrecedent() {
        return hashBlockPrecedent;
    }

    /**
     * @return the operationList
     */
    public Vector<Operation> getOperationList() {
        return operationList;
    }

    /**
     * Transformer la liste d'operation en une chaine de caracteres
     * @return La chaine obtenue
     */
    public String operationListToString(){
        String result = "";
        for(int i = 0; i < operationList.size(); i++){
            result += operationList.elementAt(i).toString();
            result += "\n";
        }
        return result;
    }

    /**
     * Transformer le block en une string
     * @return le block synthetisé en string
     */
    public String toString(){
        return "Bloc précédent: "
               +String.valueOf(hashBlockPrecedent)
               +"\n"
               +operationListToString()
               +"Hash de ce bloc: "
               +String.valueOf(this.hashCode())
               +"\n";
    }

    /**
     * @brief Permet de hasher un block
     * @return Le hash du block
     */
    @Override
    public int hashCode(){
        int hash = 1;
        for(int i = 0; i < operationList.size(); i++){
            hash = hash * 5 + operationList.get(i).hashCode();
        }
        /** 
         * le premier bloc d'une branche/blockchain possède un hash
         * du block précédent qui est nulle. On n'en tient donc pas
         * compte sinon notre hash est aussi nul
         */
        if(hashBlockPrecedent != 0){
            hash = hash * 17 + hashBlockPrecedent;
        }
        return hash;
    }

}