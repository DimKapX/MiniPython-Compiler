# MiniPython Compiler (Static Analysis)
A compiler front-end for a subset of Python, written in Java using SableCC. It performs lexing, parsing, symbol resolution, and type checking on MiniPython source files, reporting errors without executing the code.

How It Works
The compiler runs two AST visitor passes over the parsed source:
SymbolFinderASTVisitor — collects all variable and function definitions into a symbol table, catching undefined identifiers and duplicate function/argument declarations
TypeCheckerASTVisitor — walks the AST a second time to infer and validate types, reporting type mismatches in expressions, function calls, and assignments


How to Run
1. Generate the parser from the grammar (Windows)
sablecc minipython.grammar
On other platforms: java -jar lib/sablecc.jar minipython.grammar
After this, a minipython/ directory will be created containing four subfolders: analysis/, lexer/, node/, and parser/.

2. Compile all Java files

3. Run on a MiniPython source file
java ParserTest <your_file.py>
