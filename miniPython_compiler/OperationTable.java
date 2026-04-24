

/* This class is defined to make TypeCheckerASTVisitor class simpler by encaosulating some of it's functionality in type checking */

public class OperationTable{

    public static enum BinOp{
        ADD,
        SUB,
        MULT,
        DIV,
        EXP,
        MOD
    }

    public static enum UnOp{
        INCR,
        DECR
    }

    public static TYPE BinaryOperation(BinOp binop, TYPE left, TYPE right){
        
        if (left == TYPE.ERROR || right == TYPE.ERROR){
            return TYPE.ERROR;
        }

        if (left == TYPE.UNKOWN || right == TYPE.UNKOWN){
            return TYPE.UNKOWN;
        }

        switch (binop) {

            case ADD:   /* Addition is defined only betweeen integers or between strings (string concatenation) */
                
                if (left == TYPE.INT && right == TYPE.INT) 
                    return TYPE.INT;
                
                else if (left == TYPE.STRING && right == TYPE.STRING)   // Addition between strings is defined (concatenation)
                    return TYPE.STRING;
                
                else if (left == TYPE.LIST && right == TYPE.LIST)   // Addition between lists is defined (concatenation)
                    return TYPE.LIST;
                
                else
                    return TYPE.ERROR;
            
            case SUB:   /*Subtraction is only defined between integers */

                if (left == TYPE.INT && right == TYPE.INT)
                    return TYPE.INT;
                
                else
                    return TYPE.ERROR;


            case MULT:  /* Multiplication is defined only between  integers*/

                if (left == TYPE.INT && right == TYPE.INT)
                    return TYPE.INT;
                
                else
                    return TYPE.ERROR;
                
            case DIV:  /* Division is defined only between  integers*/

                if (left == TYPE.INT && right == TYPE.INT)
                    return TYPE.INT;
                
                else
                    return TYPE.ERROR;
                
            case EXP:  /* Exponention is defined only between  integers*/

                if (left == TYPE.INT && right == TYPE.INT)
                    return TYPE.INT;
                
                else
                    return TYPE.ERROR;   
                
            
            case MOD:  /* Modulo is defined only between  integers*/

                if (left == TYPE.INT && right == TYPE.INT)
                    return TYPE.INT;
                
                else
                    return TYPE.ERROR;

            default:
                return TYPE.ERROR;
        }
    }   
    
    public static TYPE UnaryOperation(UnOp unop, TYPE type){

        if (type == TYPE.ERROR)
            return TYPE.ERROR;

        if (type == TYPE.UNKOWN)
            return TYPE.UNKOWN;

        switch (unop) {
            case INCR:
                
                if (type == TYPE.INT)
                    return TYPE.INT;
               
                else
                    return TYPE.ERROR;

            case DECR:

            if (type == TYPE.INT)
                return TYPE.INT;

            else
                return TYPE.ERROR;

            default:
                return TYPE.ERROR;
        }
    }

    public static TYPE Comparison(TYPE left, TYPE right){

        if (left == TYPE.UNKOWN || right == TYPE.UNKOWN)
            return TYPE.UNKOWN;

        if (left == TYPE.INT && right == TYPE.INT)
            return TYPE.BOOL;

        if (left == TYPE.STRING && right == TYPE.STRING)
            return TYPE.BOOL;

        if (left == TYPE.BOOL && right == TYPE.BOOL)
            return TYPE.BOOL;

        return TYPE.ERROR;  // Everything else is invalid e.g:  "Monkey" > 100
    }
} 