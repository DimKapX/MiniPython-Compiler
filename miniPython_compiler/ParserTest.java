import java.io.*;
import minipython.lexer.Lexer;
import minipython.parser.Parser;
import minipython.node.*;


public class ParserTest{

    public static void main(String[] args){

        try{

            Parser parser =
                new Parser(
                new Lexer(
                new PushbackReader(
                new FileReader(args[0].toString()), 1024)));

            Start ast = parser.parse();

            SymbolTable symbolTable = new SymbolTable();
            SymbolFinderASTVisitor sfastv = new SymbolFinderASTVisitor(symbolTable); // First Visitor

            ast.apply(sfastv);

            if (sfastv.totalErrors() > 0){

                System.out.print("Definition Errors found, program will abort, [CORRECT THE ERRORS BEFORE WE CAN PROCEED TO TYPE CHECKING]\n");
                return;
            }

            TypeCheckerASTVisitor tcastv = new TypeCheckerASTVisitor(symbolTable); // Second Visitor
            
            ast.apply(tcastv);

            if (tcastv.totalErrors() > 0){

                System.out.print("Errors found, program will abort\n");
                return;
            }
            
            symbolTable.printTable();

        }
        catch (Exception exception){
            exception.printStackTrace();
        }
    }
}