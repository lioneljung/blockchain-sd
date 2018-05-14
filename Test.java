import java.util.Vector;

/**
 * @brief Classe de test
 */
public class Test{

    public static void main(String args[]){

        System.out.println("TESTS");
        BlockChain bc = new BlockChain();
        Vector<Operation> opes = new Vector<Operation>();
        opes.add(new Operation(10, "abc", "def"));
        opes.elementAt(0).toString();
        opes.add(new Operation(12.5, "xyz", "abc"));
        opes.add(new Operation(0.123, "def", "xyz"));
        opes.add(new Operation(2.43, "iop", "lmn"));
        opes.add(new Operation(3.43, "def", "iop"));
        opes.add(new Operation(100.2, "xyz", "abc"));
        bc.ajouterBlock(new Block(0, opes));
        System.out.println(bc.toString());
    
    }

}