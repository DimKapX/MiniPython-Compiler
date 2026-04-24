import minipython.node.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class SymbolTable{

    private boolean DEBUG;

    private Map<String, IdSymbol> idsymbols;

    /*The function symbols and the corresponding function who's type has to be evaluated every time it's called*/
    private Map<FuncSymbol, AFunction> funcsymbols;
    private Map<FuncSymbol, List<PExpression>> defaultexp;  /* non default arguments are null, they have no default expression*/

    public SymbolTable(){
        idsymbols = new HashMap<>();
        funcsymbols = new HashMap<>();
        defaultexp = new HashMap<>();
    }

    public void addIdSymbol(String name) {

        idsymbols.put(name, new IdSymbol(name));

        if (DEBUG)
            System.out.println("added id symbol " + name);
    }

    public void addIdSymbol(String name, int line, int col){

        idsymbols.put(name, new IdSymbol(name, line, col));
    
        if (DEBUG)
            System.out.println("added id symbol " + name);
    }

    public IdSymbol getIdSymbol(String name){

        return idsymbols.get(name);
    }

    public void addFuncSymbol(FuncSymbol symb, AFunction node){

        funcsymbols.put(symb, node);
        
        if (DEBUG)
            System.out.println("added func symbol " + symb.name());
    }

    public void changeSymbolType(String symbolName, TYPE newType){

        IdSymbol symbol = idsymbols.get(symbolName);

        if(symbol == null)
            return;

        symbol.setType(newType);

        if (DEBUG)
            System.out.printf("changed id symbol %s to %s\n", symbolName, newType);
    }

    public boolean containsIdSymbol(String name){
        return idsymbols.containsKey(name);
    }

    public boolean containsFuncSymbol(String name, int freeargs, int defargs){

        FuncSymbol fsym = new FuncSymbol(name, freeargs, defargs);
        return this.funcsymbols.containsKey(fsym);
    }

    public boolean containsFuncSymbol(String name, int freeargs){
        return containsFuncSymbol(name, freeargs, 0);
    }

    public FuncSymbol getFuncSymbol(String name, int freeargs, int defargs){

        FuncSymbol fsym = new FuncSymbol(name, freeargs, defargs);

        for (FuncSymbol fsymkey: this.funcsymbols.keySet()){

            if (fsymkey.equals(fsym)){
                return fsymkey;
            }
        }
        return null;
    }

    public FuncSymbol getFuncSymbol(String name, int freeargs){
        return getFuncSymbol(name, freeargs, 0);
    }
 
    public AFunction getFunctionNode(FuncSymbol fsym){

        return this.funcsymbols.get(fsym);
    }
    

    public void addArgumentList(FuncSymbol fsym, List<PExpression> explist){

        defaultexp.put(fsym, explist);

        if(DEBUG){

            List<String> argnames = fsym.getArgsNames();

            System.out.println();
            System.out.println(fsym);
            for (int i = 0; i < argnames.size(); i++){

                System.out.printf("%s: %s\n", argnames.get(i), explist.get(i));
            }
            System.out.println();
        }
    }

    public List<PExpression> getDefaultArgumentExpressions(FuncSymbol fsym){
        return defaultexp.get(fsym);
    }


    public void printTable(){


        if (funcsymbols.isEmpty() && idsymbols.isEmpty()){
            printEmptyTable();
            return;
        }

        System.out.println("--------------------------------------------------------");
        System.out.println("\t\t\tTABLE");
        System.out.println("--------------------------------------------------------");
        System.out.println();
    
        /* Not printed in order of definition  */

        for (FuncSymbol fsym: funcsymbols.keySet()){

            System.out.println(fsym);
        }

        System.out.println();

        for (IdSymbol idsym: idsymbols.values()){

            System.out.println(idsym);
        }

        System.out.println("--------------------------------------------------------");
    }

    private void printEmptyTable(){

        System.out.println("--------------------------------------------------------");
        System.out.println("\t\t\tTABLE");
        System.out.println("--------------------------------------------------------");
        System.out.println();
        System.out.println("\t\t\tEMPTY");
        System.out.println();
        System.out.println("--------------------------------------------------------");
    }

    public void enableDebugMode(){
        this.DEBUG = true;
    }
}