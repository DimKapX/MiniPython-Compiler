import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import java.util.List;

import minipython.analysis.DepthFirstAdapter;
import minipython.node.*;

public class TypeCheckerASTVisitor extends DepthFirstAdapter{

    private SymbolTable symbolTable;
    private HashMap<PExpression, TYPE> exptypes;  // The expression nnodes of our ast tree and their corresponding types
    private Stack<FuncSymbol> functionCallStack;  // We can call functions inside other functions :( 
    private TYPE funcCallRetType;   // The return type of the current function 
    private FuncSymbol currFuncDefinition;
    // You can define global variables inside functions :(
    // Evaluating their type is a special case
    // We need to to do this because they may be used after the function definition
    // Having function scopes would make our lives easier

    private int errors;
    private int warnings;

    TypeCheckerASTVisitor(SymbolTable symbolTable){

        this.symbolTable = symbolTable;
        this.exptypes = new HashMap<>();
        this.functionCallStack = new Stack<>();
        this.funcCallRetType = TYPE.NONE;   // We suppose it's none by default, will change only when a return statement (inside a function) appears
        this.currFuncDefinition = null;
        errors = 0;
        warnings = 0;
    }

    @Override
    public void outANumberExpression(ANumberExpression node){

        exptypes.put(node, TYPE.INT);
    }

    @Override
    public void outAStringExpression(AStringExpression node){

        exptypes.put(node, TYPE.STRING);
    }

    @Override
    public void outACharExpression(ACharExpression node){

        exptypes.put(node, TYPE.STRING);
    }

    @Override
    public void outANoneExpression(ANoneExpression node){

        exptypes.put(node, TYPE.NONE);
    }

    @Override
    public void outAFunction(AFunction node){
        this.currFuncDefinition = null;
    }

    @Override
    public void inALesscComparison(ALesscComparison node){
        comparison(node);
    }

    @Override
    public void inAEqLesscComparison(AEqLesscComparison node){
        comparison(node);;
    }

    @Override
    public void inAGreatcComparison(AGreatcComparison node){
        comparison(node);
    }

    @Override
    public void inAEqGreatcComparison(AEqGreatcComparison node){
        comparison(node);
    }

    @Override
    public void inAEqcComparison(AEqcComparison node){
        comparison(node);
    }

    @Override
    public void inANotEqcComparison(ANotEqcComparison node){
        comparison(node);
    }

    @Override
    public void inAIdExpression(AIdExpression node){

        exptypes.put(node, getIdType(node.getId()));
    }

    @Override 
    public void caseAAdditionExpression(AAdditionExpression node){

        inAAdditionExpression(node);
        if(node.getL() != null)
        {
            node.getL().apply(this);
        }
        if(node.getR() != null)
        {
            node.getR().apply(this);
        }

        TYPE left = exptypes.remove(node.getL());
        TYPE right = exptypes.remove(node.getR());
        TYPE prodtype = OperationTable.BinaryOperation(OperationTable.BinOp.ADD, left, right);

        if (prodtype == TYPE.ERROR){

            System.err.printf("[ERROR] \"%s + %s \" (unsupported operands types for '+': '%s' and '%s')\n", node.getL(), node.getR(), left, right);
            errors++;
        }

        exptypes.put(node, prodtype);

        outAAdditionExpression(node);
    }

    @Override
    public void caseASubstractionExpression(ASubstractionExpression node){
       
        inASubstractionExpression(node);
        if(node.getL() != null)
        {
            node.getL().apply(this);
        }
        if(node.getR() != null)
        {
            node.getR().apply(this);
        }

        TYPE left = exptypes.remove(node.getL());
        TYPE right = exptypes.remove(node.getR());
        TYPE prodtype = OperationTable.BinaryOperation(OperationTable.BinOp.SUB, left, right);

        if (prodtype == TYPE.ERROR){
            
            System.err.printf("[ERROR] \"%s - %s \" (unsupported operands types for '-': '%s' and '%s')\n", node.getL(), node.getR(), left, right);
            errors++;
        }
        
        exptypes.put(node, prodtype); 
        
        outASubstractionExpression(node);
    }

    @Override
    public void caseAMultiplicationExpression(AMultiplicationExpression node){
        
        inAMultiplicationExpression(node);
        
        if(node.getL() != null)
        {
            node.getL().apply(this);
        }
        if(node.getR() != null)
        {
            node.getR().apply(this);
        }
        
        TYPE left = exptypes.remove(node.getL());
        TYPE right = exptypes.remove(node.getR());
        TYPE prodtype = OperationTable.BinaryOperation(OperationTable.BinOp.DIV, left, right);

        if (prodtype == TYPE.ERROR){
            
            System.err.printf("[ERROR] \"%s * %s \" (unsupported operands types for '*': '%s' and '%s')\n", node.getL(), node.getR(), left, right);
            errors++;
        }
        
        exptypes.put(node, prodtype); 
        
        outAMultiplicationExpression(node);
    }

    @Override
    public void caseADivisionExpression(ADivisionExpression node){

        inADivisionExpression(node);

        if(node.getL() != null)
        {
            node.getL().apply(this);
        }
        if(node.getR() != null)
        {
            node.getR().apply(this);
        }

        TYPE left = exptypes.remove(node.getL());
        TYPE right = exptypes.remove(node.getR());
        TYPE prodtype = OperationTable.BinaryOperation(OperationTable.BinOp.MULT, left, right);

        if (prodtype == TYPE.ERROR){
            
            System.err.printf("[ERROR] \"%s / %s \" (unsupported operands types for '/': '%s' and '%s')\n", node.getL(), node.getR(), left, right);
            errors++;
        }
        
        exptypes.put(node, prodtype); 

        outADivisionExpression(node);
    }

    @Override
    public void caseAExpExpression(AExpExpression node){

        inAExpExpression(node);

        if(node.getL() != null)
        {
            node.getL().apply(this);
        }
        if(node.getR() != null)
        {
            node.getR().apply(this);
        }

        TYPE left = exptypes.remove(node.getL());
        TYPE right = exptypes.remove(node.getR());
        TYPE prodtype = OperationTable.BinaryOperation(OperationTable.BinOp.EXP, left, right);

        if (prodtype == TYPE.ERROR){
            
            System.err.printf("[ERROR] \"%s ** %s \" (unsupported operands types for '**': '%s' and '%s')\n", node.getL(), node.getR(), left, right);
            errors++;
        }
        
        exptypes.put(node, prodtype);

        outAExpExpression(node);
    }

    @Override
    public void caseAModulusExpression(AModulusExpression node){

        inAModulusExpression(node);

        if(node.getL() != null)
        {
            node.getL().apply(this);
        }
        if(node.getR() != null)
        {
            node.getR().apply(this);
        }

        TYPE left = exptypes.remove(node.getL());
        TYPE right = exptypes.remove(node.getR());
        TYPE prodtype = OperationTable.BinaryOperation(OperationTable.BinOp.EXP, left, right);

        if (prodtype == TYPE.ERROR){
            
            System.err.printf("\"%s % %s \" (unsupported operands types for '%': '%s' and '%s')\n", node.getL(), node.getR(), left, right);
            errors++;
        }
        
        exptypes.put(node, prodtype);
        
        outAModulusExpression(node);
    }
    
    @Override
    public void caseAIncrementExpression(AIncrementExpression node){

        inAIncrementExpression(node);

        if(node.getExpression() != null)
        {
            node.getExpression().apply(this);
        }

        TYPE type = exptypes.remove(node.getExpression());
        TYPE prodType = OperationTable.UnaryOperation(OperationTable.UnOp.INCR, type);

        if (prodType == TYPE.ERROR){

            System.err.printf("[ERROR] \"%s++\" (unsupported operand type for '++': '%s')\n", node.getExpression(), type);
            errors++;
        }

        exptypes.put(node, prodType);

        outAIncrementExpression(node);
    }

    @Override
    public void caseADecrementExpression(ADecrementExpression node){

        inADecrementExpression(node);

        if(node.getExpression() != null)
        {
            node.getExpression().apply(this);
        }

        TYPE type = exptypes.remove(node.getExpression());
        TYPE prodType = OperationTable.UnaryOperation(OperationTable.UnOp.DECR, type);

        if (prodType == TYPE.ERROR){

            System.err.printf("[ERROR] \"%s-- \" (unsupported operand type for '--': '%s')\n", node.getExpression(), type);
            errors++;
        }

        exptypes.put(node, prodType);

        outADecrementExpression(node);
    }

    @Override
    public void caseAAssignStatement(AAssignStatement node){

        if(node.getExpression() != null)
        {
            node.getExpression().apply(this);
        }

        TYPE type = exptypes.remove(node.getExpression());
        
        if (!functionCallStack.isEmpty()){  // We are inside a function call
            
            if (functionCallStack.peek().containsArgument(node.getId().getText())){

                functionCallStack.peek().setArgumentType(node.getId().getText(), type);

            }
        }
        else{
            symbolTable.changeSymbolType(node.getId().getText(), type);
        }
    }

    @Override
    public void caseASubAssignStatement(ASubAssignStatement node){

        if(node.getId() != null)
        {
            node.getId().apply(this);
        }
        if(node.getExpression() != null)
        {
            node.getExpression().apply(this);
        }

        TYPE childexptype = exptypes.remove(node.getExpression());
        TYPE idtype = getIdType(node.getId());
        TYPE exType = TYPE.INT;

        if (idtype != TYPE.INT && idtype != TYPE.UNKOWN){
            exType = TYPE.ERROR;
        }

        if (childexptype != TYPE.INT && childexptype != TYPE.UNKOWN){
            exType = TYPE.ERROR;
        }

        if (exType == TYPE.ERROR){
            System.err.printf("[ERROR] \"%s -= %s \" (unsupported operands types for '-': '%s' and '%s')\n",
                node.getId().getText(), node.getExpression(), idtype, childexptype);
            errors++;   
            return;
        }

        if (!functionCallStack.isEmpty()){  
            
            if (functionCallStack.peek().containsArgument(node.getId().getText())){

                functionCallStack.peek().setArgumentType(node.getId().getText(), childexptype);

            }
        }
        else{
            symbolTable.changeSymbolType(node.getId().getText(), childexptype);
        }
    }

    @Override
    public void caseADivAssignStatement(ADivAssignStatement node){

        if(node.getId() != null)
        {
            node.getId().apply(this);
        }
        if(node.getExpression() != null)
        {
            node.getExpression().apply(this);
        }

        TYPE childexptype = exptypes.remove(node.getExpression());
        TYPE idtype = getIdType(node.getId());
        TYPE exType = TYPE.INT;

        if (idtype != TYPE.INT && idtype != TYPE.UNKOWN){
            exType = TYPE.ERROR;
        }

        if (childexptype != TYPE.INT && childexptype != TYPE.UNKOWN){
            exType = TYPE.ERROR;
        }

        if (exType == TYPE.ERROR){
            System.err.printf("[ERROR] \"%s /= %s \" (unsupported operands types for '/': '%s' and '%s')\n",
                node.getId().getText(), node.getExpression(), idtype, childexptype);
            errors++;   
            return;
        }

        if (!functionCallStack.isEmpty()){  
            
            if (functionCallStack.peek().containsArgument(node.getId().getText())){

                functionCallStack.peek().setArgumentType(node.getId().getText(), childexptype);

            }
        }
        else{
            symbolTable.changeSymbolType(node.getId().getText(), childexptype);
        }
    }

    @Override
    public void caseAAssignArrayStatement(AAssignArrayStatement node){

        if(node.getLEx() != null)
        {
            node.getLEx().apply(this);
        }
        if(node.getREx() != null)
        {
            node.getREx().apply(this);
        }

        TYPE idType = getIdType(node.getId());
        
        if (idType != TYPE.LIST && idType != TYPE.UNKOWN){
            
            System.err.printf("[ERROR] Line %d Col %d identifier '%s' is not of type 'LIST'\n", 
                node.getId().getLine(), node.getId().getPos(), node.getId().getText());
            errors++;
            return;
        }

        TYPE arrexptype = exptypes.remove(node.getLEx());

        if (arrexptype != TYPE.INT && arrexptype != TYPE.NONE){
           
            System.err.printf("[ERROR] Expression inside array brackets '%s[..]' must be of type 'INT'\n", node.getId().getText());
            errors++;
            return;
        }

        exptypes.remove(node.getREx());
    }


    @Override
    public void caseAReturnStatement(AReturnStatement node){

        inAReturnStatement(node);

        if(node.getExpression() != null)
        {
            node.getExpression().apply(this);
        }

        this.funcCallRetType = exptypes.get(node.getExpression());

        outAReturnStatement(node);

    }

    @Override
    public void caseAFuncCallExpression(AFuncCallExpression node){

        funcCall(node);
    }

    @Override
    public void caseAFuncCallStatement(AFuncCallStatement node){

        funcCall(node);
    }

    @Override
    public void caseAFuncUseExpression(AFuncUseExpression node){

        funcCall(node);
    }

    @Override
    public void caseAFunction(AFunction node){

        if (!this.functionCallStack.isEmpty()){  // We called a function
            node.getStatement().apply(this);
            return;
        }

        inAFunction(node);
      
        Object temp[] = node.getArgument().toArray();

        for(int i = 0; i < temp.length; i++)
        {
            ((PArgument) temp[i]).apply(this);
        }
        
        this.currFuncDefinition = new FuncSymbol(this.symbolTable.getFuncSymbol(node.getId().getText(), temp.length));
        if(node.getStatement() != null)
        {
            node.getStatement().apply(this);
        }

        outAFunction(node);
    }

    @Override
    public void caseAArrayExpression(AArrayExpression node){
    
        Object temp[] = node.getExpression().toArray();
        for(int i = 0; i < temp.length; i++)
        {
            ((PExpression) temp[i]).apply(this);
        }

        for (Object obj: temp){
            exptypes.remove((PExpression)obj); // We don't care about the types inside the list
        }

        exptypes.put(node, TYPE.LIST);
    }

    @Override 
    public void caseAArrayItemExpression(AArrayItemExpression node){

        TId id = node.getId();
        TYPE idtype = getIdType(id);

        if (idtype == TYPE.ERROR){
            exptypes.put(node, idtype);
            return;
        }

        if (idtype != TYPE.LIST && idtype!= TYPE.UNKOWN){   // If we can't evaluate the type, we don't ghit the error
            System.err.printf("[ERROR] Line %d Col %d identifier '%s' is not of type 'LIST'\n",
                id.getLine(), id.getPos(), id.getText()); 
            errors++;
            exptypes.put(node, TYPE.ERROR);
            return;
        }

        if(node.getExpression() != null)
        {
            node.getExpression().apply(this);
        }

        TYPE childexptype = exptypes.remove(node.getExpression());

        if (childexptype != TYPE.INT && childexptype != TYPE.UNKOWN){
            System.err.printf("[ERROR] Line %d Col %d expression in '[..]' is not of type 'INT'\n",
                id.getLine(), id.getPos());
            errors++;
            exptypes.put(node, TYPE.ERROR);
            return;
        }

        // This will always hit because we have no way of evaluating smoething like a[2]
        System.out.printf("[WARNING] expression '%s[%s]' cannot be evaluated\n", id.getText(), node.getExpression());
        exptypes.put(node, TYPE.UNKOWN);
    }

    @Override
    public void caseATypeExpression(ATypeExpression node){

        TId id = node.getId();

        // so simple 
        exptypes.put(node, getIdType(id));
    }

    @Override
    public void caseAAsciiExpression(AAsciiExpression node){

        TId id = node.getId();
        TYPE idType = getIdType(id);
        TYPE expType = TYPE.STRING;

        if (idType != TYPE.INT && idType != TYPE.UNKOWN){
            System.err.printf("[ERROR] 'ascii(..)' takes only identifiers of type 'INT'\n");
            errors++;
            expType = TYPE.ERROR;
        }
        exptypes.put(node, expType);
    }

    @Override
    public void caseAOpenExpression(AOpenExpression node){

        if(node.getLex() != null)
        {
            node.getLex().apply(this);
        }
        if(node.getRex() != null)
        {
            node.getRex().apply(this);
        }

        TYPE lType = exptypes.remove(node.getLex());
        TYPE rType = exptypes.remove(node.getRex());
        TYPE exptype;

        if (lType == TYPE.STRING && rType == TYPE.STRING){  // Open expression returns none
            exptype = TYPE.NONE;
        }
        else if ((lType == TYPE.STRING && rType == TYPE.UNKOWN) || (lType == TYPE.UNKOWN && rType == TYPE.STRING)){
            exptype = TYPE.NONE;
        }
        else if (lType == TYPE.UNKOWN && rType == TYPE.UNKOWN){
            exptype = TYPE.NONE;
        }
        else{
            exptype = TYPE.ERROR;
        }

        if ((exptype == TYPE.ERROR)){

            System.err.printf("[ERROR]: 'open(.. , ..)'' takes only expressions of type 'STRING'\n");
            errors++;
            exptype = TYPE.ERROR;
        }

        exptypes.put(node, exptype);
    }

    @Override
    public void caseAMaxExpression(AMaxExpression node){

        Object temp[] = node.getExpression().toArray();
        
        for(int i = 0; i < temp.length; i++)
        {
            ((PExpression) temp[i]).apply(this);
        }

        for (Object obj: temp){

            PExpression valexp = (PExpression)obj;
            TYPE valtype = exptypes.remove(valexp);

            if (valtype != TYPE.INT && valtype != TYPE.UNKOWN){
                
                System.err.printf("[ERROR] 'max' takes only expressions of type 'INT'\n");
                errors++;
                exptypes.put(node, TYPE.ERROR);
                return;
            }
        }

        exptypes.put(node, TYPE.INT);   // returns int
    }

    @Override
    public void caseAMinExpression(AMinExpression node){

        Object temp[] = node.getExpression().toArray();
        
        for(int i = 0; i < temp.length; i++)
        {
            ((PExpression) temp[i]).apply(this);
        }

        for (Object obj: temp){

            PExpression valexp = (PExpression)obj;
            TYPE valtype = exptypes.remove(valexp);

            if (valtype != TYPE.INT && valtype != TYPE.UNKOWN){
                
                System.err.printf("[ERROR] 'min' takes only expressions of type 'INT'\n");
                errors++;
                exptypes.put(node, TYPE.ERROR);
                return;
            }
        }

        exptypes.put(node, TYPE.INT);   // returns int
        
    }

    //@Override
    //public void case A

    private void funcCall(Node node){

        /* Because much of the same code is shared among these nodes */
        /* Only one of those can be non null at the same time */
        AFuncCallStatement funcstatnode = null;
        AFuncCallExpression funcexpnode = null;
        AFuncUseExpression funcusenode = null;
        TId fid;

        Object args[];

        if (node instanceof AFuncCallStatement){
            funcstatnode = (AFuncCallStatement)node;
            inAFuncCallStatement(funcstatnode); 
            fid = funcstatnode.getId();
            args = funcstatnode.getExpression().toArray();

        }
        else if (node instanceof AFuncCallExpression){
            funcexpnode = (AFuncCallExpression)node;
            inAFuncCallExpression(funcexpnode);
            fid = funcexpnode.getId();
            args = funcexpnode.getExpression().toArray();
        }
        else{   // Func use expression
            funcusenode = (AFuncUseExpression)node;
            inAFuncUseExpression(funcusenode);
            fid = funcusenode.getRId();
            args = funcusenode.getExpression().toArray();
        }

        TYPE funccalltype;  // 
       
        if (!symbolTable.containsFuncSymbol(fid.getText(), args.length)){

            System.err.printf("[ERROR] Line %d Col %d function '%s' is undefined\n", fid.getLine(), fid.getPos(), fid.getText());
            funccalltype = TYPE.ERROR;
            errors++;

            if (funcexpnode != null){
                exptypes.put(funcexpnode, funccalltype);
            }

            else if (funcusenode != null){
                exptypes.put(funcusenode, funccalltype);
            }
            return;
        }

        // We want the og symbol defines on the symbol table where the arg list is set
        // If it is not contained in the symbol table. the above error would hit
        FuncSymbol fsym = new FuncSymbol(this.symbolTable.getFuncSymbol(fid.getText(), args.length, 0));

        /* 
        Check if the functon is already contained in the call stack
        In this case we may have get something like infinite recursion
        Lets just throw a warning and say that the type of the function call cannot be evaluated
         */
        if (functionCallStack.contains(fsym)){
            System.out.printf("[WARNING] Line %d Col %d function call'%s' type cannot be evaluated due to circular function call chain\n", 
                fid.getLine(), fid.getPos(), fid.getText());
            warnings++;

            if (funcexpnode != null){
                exptypes.put(funcexpnode, TYPE.UNKOWN);
            }
            else if (funcusenode != null){
                exptypes.put(funcusenode, TYPE.UNKOWN);
            }
            this.funcCallRetType = TYPE.UNKOWN;
            return;
        }
        
        List<PExpression>  newArgumentExpressions = new  ArrayList<PExpression>(symbolTable.getDefaultArgumentExpressions(fsym));

        for (int i = 0; i < args.length; i++){

            newArgumentExpressions.set(i, (PExpression)args[i]);  // Replace parametres with new expressions
        }

        for (int i = 0; i < newArgumentExpressions.size(); i++){

            if (newArgumentExpressions.get(i) == null){     // free argument is missing 

                System.err.printf("[ERROR] Line %d Col %d function call '%s' missing required argument '%s'\n",
                    fid.getLine(), fid.getPos(), fid.getText(), fsym.getArgsNames().get(i));
                
                funccalltype = TYPE.ERROR;
                errors++;

                if (funcexpnode != null){
                    exptypes.put(funcexpnode, funccalltype);
                }

                else if (funcusenode != null){
                    exptypes.put(funcusenode, funccalltype);
                }    
                return;
            }
        }   

        for (int i = 0; i < newArgumentExpressions.size(); i++){

            PExpression exp = newArgumentExpressions.get(i);

            newArgumentExpressions.get(i).apply(this);
            fsym.getArgSymbols().get(i).setType(exptypes.remove(exp));
        }

        /* 
        def add(x, y):
            return x + y

        def foo(x, y):
            z = (x, y)  #z is a global variable

        print z + 10

        z cannot be evaluated in function definition
        
        YAYYYY another edge case 
        */
        for (IdSymbol sym: fsym.getArgSymbols()){
            
            if (sym.getType() == TYPE.UNKOWN){

                System.out.printf("[WARNING] Line %d Col %d function call '%s' cannot be evaluated due to unknown type of argument '%s'\n",
                    fid.getLine(), fid.getPos(), fid.getText(), sym.name());
                warnings++;
                
                if (funcexpnode != null){
                    exptypes.put(funcexpnode, TYPE.UNKOWN);
                }
                else if (funcusenode != null){
                    exptypes.put(funcusenode, TYPE.UNKOWN);
                }    
                return;
            }
        }

        this.functionCallStack.push(fsym);

        symbolTable.getFunctionNode(fsym).apply(this);  // The function call

        // We return from function call
        this.functionCallStack.pop();
        funccalltype = this.funcCallRetType;
        this.funcCallRetType = TYPE.NONE; // Set it to None again


        if (funccalltype == TYPE.UNKOWN){

            System.out.printf("[WARNING]: Line %d Col %d function call '%s' type cannot be evaluated due to previous warnings\n",
                fid.getLine(), fid.getPos(), fid.getText());
            warnings++;
        }

        if (funcexpnode != null){
            exptypes.put(funcexpnode, funccalltype);
        }

        else if (funcusenode != null){
            exptypes.put(funcusenode, funccalltype);
        }
    }

    private void comparison(PComparison node){

        PExpression lchild;
        PExpression rchild;
        TYPE left;
        TYPE right;
        TYPE type;

        if (node instanceof ALesscComparison){

            ALesscComparison lesscomp = (ALesscComparison)node;
            lchild = lesscomp.getL();
            rchild = lesscomp.getR();
            lchild.apply(this);
            rchild.apply(this);
            left = exptypes.remove(lchild);
            right = exptypes.remove(rchild);
            type = OperationTable.Comparison(left, right);
            comparisonDiagnostics("<", lchild, rchild, type, left, right);

        }
        else if (node instanceof AEqLesscComparison){
            
            AEqLesscComparison eqlesscomp = (AEqLesscComparison)node;
            lchild = eqlesscomp.getL();
            rchild = eqlesscomp.getR();
            lchild.apply(this);
            rchild.apply(this);
            left = exptypes.remove(lchild);
            right = exptypes.remove(rchild);
            type = OperationTable.Comparison(left, right);
            comparisonDiagnostics("<=", lchild, rchild, type, left, right);
        }
        else if (node instanceof AGreatcComparison){

            AGreatcComparison greatcomp = (AGreatcComparison)node;
            lchild = greatcomp.getL();
            rchild = greatcomp.getR();
            lchild.apply(this);
            rchild.apply(this);
            left = exptypes.remove(lchild);
            right = exptypes.remove(rchild);
            type = OperationTable.Comparison(left, right);
            comparisonDiagnostics(">", lchild, rchild, type, left, right);

        }
        else if (node instanceof AEqGreatcComparison){

            AEqGreatcComparison eqgreatcomp = (AEqGreatcComparison)node;
            lchild = eqgreatcomp.getL();
            rchild = eqgreatcomp.getR();
            lchild.apply(this);
            rchild.apply(this);
            left = exptypes.remove(lchild);
            right = exptypes.remove(rchild);
            type = OperationTable.Comparison(left, right);
            comparisonDiagnostics(">=", lchild, rchild, type, left, right);
        }
        else if (node instanceof AEqcComparison){

            AEqcComparison eqcomp = (AEqcComparison)node;
            lchild = eqcomp.getL();
            rchild = eqcomp.getR();
            lchild.apply(this);
            rchild.apply(this);
            left = exptypes.remove(lchild);
            right = exptypes.remove(rchild);
            type = OperationTable.Comparison(left, right);
            comparisonDiagnostics("==", lchild, rchild, type, left, right);
        }
        else if (node instanceof ANotEqcComparison){
            
            ANotEqcComparison noteqcomp = (ANotEqcComparison)node;
            lchild = noteqcomp.getL();
            rchild = noteqcomp.getR();
            lchild.apply(this);
            rchild.apply(this);
            left = exptypes.remove(lchild);
            right = exptypes.remove(rchild);
            type = OperationTable.Comparison(left, right);
            comparisonDiagnostics("!=", lchild, rchild, type, left, right);
        }
    }

    /*This code repeats among multiple comparison nodes, we put it into a function to not reapeat */
    private void comparisonDiagnostics(String compsymbol, PExpression lchild, PExpression rChild, TYPE type,
        TYPE ltype, TYPE rType){    

        if (type == TYPE.ERROR){
            System.err.printf("[ERROR] \"%s %s %s \" (invalid comparison types: '%s' and '%s')\n", 
            lchild, compsymbol, rChild, ltype, rType);
            errors++;
        }
        else if (type == TYPE.UNKOWN){
            System.out.printf("[WARNING] \"%s %s %s\" (comparison may not be valid)\n", lchild, compsymbol, rChild);
            warnings++;
        }
    }

    private TYPE getIdType(TId id){
        
        TYPE idexptype = TYPE.UNKOWN;
        FuncSymbol currfunc = null;

        if (!this.functionCallStack.isEmpty())
            currfunc = this.functionCallStack.peek();

        if (symbolTable.containsIdSymbol(id.getText())){
            idexptype = symbolTable.getIdSymbol(id.getText()).getType();
        }

        if (currfunc != null  && currfunc.containsArgument(id.getText())){
            idexptype = currfunc.findArgument(id.getText()).getType();
        }

        if (this.currFuncDefinition != null && currFuncDefinition.containsArgument(id.getText())){
            idexptype = TYPE.UNKOWN;
        }

        if (idexptype == TYPE.ERROR){
            System.err.printf("[ERROR] Line %d Col %d identifier '%s' type is invalid due to previous error\n", id.getLine(), id.getPos(), id.getText());
            errors++;
        }

        return idexptype;

    }

    public int totalErrors(){
        return errors;
    }

    public int totalWarnings(){
        return warnings;
    }

}