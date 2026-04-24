import minipython.analysis.DepthFirstAdapter;
import minipython.node.*;

import java.util.ArrayList;
import java.util.List;

/*
Adds the function definitions and id's int the symbol table
*/
public class SymbolFinderASTVisitor extends DepthFirstAdapter{
    
    private SymbolTable symbolTable;
    private FuncSymbol currentFunction;
    private String forLoopIdentifier;
    private int errors = 0;

    public SymbolFinderASTVisitor(SymbolTable symbolTable){
       
        this.symbolTable = symbolTable;
        this.forLoopIdentifier = null;
        this.currentFunction = null;
    }
    
    @Override
    public void inAIdExpression(AIdExpression node){

        checkForDefinition(node.getId());
    }

    @Override 
    public void inATypeExpression(ATypeExpression node){

        checkForDefinition(node.getId());
    }

    @Override
    public void inAAsciiExpression(AAsciiExpression node){

        checkForDefinition(node.getId());
    }

    @Override 
    public void inAForStatement(AForStatement node){

        checkForDefinition(node.getRId());
        this.forLoopIdentifier = node.getLId().getText();
    }

    @Override
    public void inAAssignArrayStatement(AAssignArrayStatement node){

        // a[<expr>] = <expr>
        // a had to be defined as a list identifier earlier
        
        checkForDefinition(node.getId());
    }

    @Override
    public void inADivAssignStatement(ADivAssignStatement node){

        // a /= <expr> 
        // a had to be defined earlier

        checkForDefinition(node.getId());

    }

    @Override
    public void inASubAssignStatement(ASubAssignStatement node){

        // a-= <expr>
        // a had to be defined earlier

        checkForDefinition(node.getId());
    }

    @Override
    public void caseAFunction(AFunction node)
    {
        /* 
            def(x, y, z=a.foo(1, 2)): print(..)
            value a.foo(1, 2) type cannot be evaluated in the first visitor
            so we keep the expressions corresponding to default argument values
            in a list and hopefully evaluate their type later
    
        */
        // We use a list because we want to preserve the position in which those arguments were defined
        List<PExpression> defargnodes = new ArrayList<>();
        
        List<String> argnames = new ArrayList<>();
        Object temp[] = node.getArgument().toArray();
        TId fid = node.getId();
        String fname = fid.getText();
        TId dupargid = null;   // For error printing purposes 
        boolean dupargflag = false;
        boolean funcasdefaultargflag = false;

        int freeargs = 0;
        int defargs = 0;
        
        for (Object obj : temp){

            if (obj instanceof ASimpArgument){
                
                String name = ((ASimpArgument)obj).getId().getText();

                if (argnames.contains(name)){
                    dupargflag = true;
                    dupargid = ((ASimpArgument)obj).getId();
                    break;
                }
                argnames.add(name);
                defargnodes.add(null);  // not a default argument so nothing to evaluate FOR THIS ARGUMENT POSITION later
                freeargs++;
            }
            else if (obj instanceof ADefaultArgument){

                ADefaultArgument defarg = (ADefaultArgument)obj;
                String name = defarg.getId().getText();

                if (argnames.contains(name)){
                    dupargflag = true;
                    dupargid = ((ADefaultArgument)obj).getId();
                    break;
                }

                if (defarg.getExpression() instanceof AFuncUseExpression){
                    funcasdefaultargflag = true;
                }

                argnames.add(name);
                defargnodes.add(defarg.getExpression());  // to evaluate it's type later
                defargs++;
            }
        }

        if (symbolTable.containsFuncSymbol(fid.getText(), freeargs, defargs)){
            
            System.err.printf("[ERROR] Line %d\tCol %d function '%s' has already been defined\n", fid.getLine(), fid.getPos(), fname);
            errors++;
            // node.getStatement().apply(this); // We don't look for more errors inside the function
            // Fix the function name first :)
            return;
        }

        // If dupargflag is set to true then alway dupargid is non null. we added the extra comparison to supress may not be initialized errors
        if (dupargflag && (dupargid != null)){  

            System.err.printf("[ERROR] Line %d\tCol %d duplicate argument '%s' in function definition '%s'\n", 
                dupargid.getLine(), dupargid.getPos(), dupargid.getText(), fname);
            
            errors++;
            return;
        }
        
        FuncSymbol fsym = new FuncSymbol(fname, freeargs, defargs);
        
        /*
        This should not be allowed
        def foo(x, y=id.foo(1,2)):
            return x + y
        */
        // Let's make sure it's not then
        if (funcasdefaultargflag){

            for (PExpression defexp: defargnodes){

                if (defexp instanceof AFuncUseExpression){

                    AFuncUseExpression funcuseexp = (AFuncUseExpression)defexp;
                    
                    if (fsym.equals(new FuncSymbol(funcuseexp.getRId().getText(), funcuseexp.getExpression().size()))){

                        System.err.printf("[ERROR] Line %d Col %d function call as default argument of the same function being defined\n",
                            funcuseexp.getRId().getLine(), funcuseexp.getRId().getPos());
                        errors++;
                        return;
                    }
                } 
            }
        }

        for (String name: argnames){    // Add the names of the arguments for this function
            fsym.addArgument(name);
        }

        symbolTable.addFuncSymbol(fsym, node); // Based on the symbol read the functoion type will be evaluated when called
        symbolTable.addArgumentList(fsym, defargnodes);
        this.currentFunction = fsym;
        
        if(node.getStatement() != null)
        {
            node.getStatement().apply(this);
        }

        outAFunction(node);
    }

    @Override
    public void outAFunction(AFunction node){

        this.currentFunction = null;
    }

    @Override
    public void outAAssignStatement(AAssignStatement node){

        TId id = node.getId();

        if (this.currentFunction == null){  // Not inside a function

            if (!symbolTable.containsIdSymbol(id.getText())){  

                symbolTable.addIdSymbol(id.getText(), id.getLine(), id.getPos());  // First Definition
            }
        }
        else{   

            if (!currentFunction.containsArgument(id.getText())){   // Not a function argument

                symbolTable.addIdSymbol(id.getText(), id.getLine(), id.getPos()); // varaible defined inside function

                // all vairables are global
            }
        }
    }

    @Override
    public void outAForStatement(AForStatement node){
        this.forLoopIdentifier = null;
    }

    private void checkForDefinition(TId id){

        if(symbolTable.containsIdSymbol(id.getText())){
            return;
        }

        if (currentFunction != null && currentFunction.containsArgument(id.getText())){
            return;
        }

        if (this.forLoopIdentifier != null && this.forLoopIdentifier.equals(id.getText())){
            return;
        }

        System.err.printf("[ERROR] Line %d\tCol %d: identifier '%s' is undefined\n", id.getLine(), id.getPos(), id.getText());
        errors++;
    }

    public int totalErrors(){
        return errors;
    }
}