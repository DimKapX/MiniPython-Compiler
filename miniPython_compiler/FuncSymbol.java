import java.util.List;
import java.util.ArrayList;

public class FuncSymbol{

    private String name;
    private int freeargs; //simple arg?
    private int defaultargs; //default arg
    private List<IdSymbol> args; 

    public FuncSymbol(String name, int freeargs, int defaultargs){
        this.name = name;
        this.freeargs = freeargs;
        this.defaultargs = defaultargs;
        this.args = new ArrayList<>();
    }

    public FuncSymbol(String name, int freeargs){
        this(name, freeargs, 0);
    }

    public FuncSymbol(FuncSymbol other){
        this.name = other.name;
        this.freeargs = other.freeargs;
        this.defaultargs = other.defaultargs;
        this.args = new ArrayList<IdSymbol>(other.args); // We want to copy the symbols 
    }

    public String name(){
        return this.name;
    }

    public int defaultargs(){
        return this.defaultargs;
    }

    public int freeargs(){
        return this.freeargs;
    }

    public void addArgument(String name){

        args.add(new IdSymbol(name));
    }

    public void setArgumentType(String argName, TYPE newType){

        for (IdSymbol arg: args){

            if (arg.name().equals(argName)){

                arg.setType(newType);
            }
        }
    }

    public boolean containsArgument(String name){

        for (IdSymbol symbol: args){

            if (symbol.name().equals(name)){
                return true;
            }
        }

        return false;
    }

    public IdSymbol findArgument(String name){

        for (IdSymbol symbol: args){

            if (symbol.name().equals(name))
                return symbol;
        }
        return null;
    }

    public int getArgumentPos(String name){

        for (int i = 0; i < args.size(); i++){

            if (args.get(i).name().equals(name)){
                return i;   //  We start from pos 0 and not 1 !!
            }
        }
        return -1;  // Not an argument
    }

    public List<String> getArgsNames(){
        
        List<String> argnames = new ArrayList<>();

        for (IdSymbol arg: args){
            argnames.add(arg.name());
        }

        return argnames;
    }

    public List<IdSymbol> getArgSymbols(){
        return this.args;
    }

    @Override
    public boolean equals(Object obj){
        //--
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (! (obj instanceof FuncSymbol))
            return false;

        FuncSymbol oth = (FuncSymbol)obj;

        // CHECK THE NUMBER OF ARGUMENTS BETWEEN FUNCTIONS WITH THE SAME NAME 

        int totalvar = defaultargs + freeargs;
        int othtotalvar = oth.defaultargs + oth.freeargs;
        
        return this.name.equals(oth.name) && ( totalvar == othtotalvar || totalvar == oth.freeargs || othtotalvar == freeargs);
    }

    @Override
    public int hashCode(){
        
        return this.name.hashCode();
    }

    @Override
    public String toString(){

        String str = new String();

        str += String.format("%s(", this.name);

        for (int i = 0; i < args.size(); i++){

            if (i < args.size() - 1)
                str += String.format("%s, ", args.get(i).name());

            else {
                str += String.format("%s", args.get(i).name());
            }

        }
        str += ')';
        
        return str;
    }

}