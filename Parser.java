// Scope analysis partially completed

import java.util.*;

public class Parser extends Object{

   private Chario chario;
   private Scanner scanner;
   private Token token;
   private SymbolTable table;

   private Set<Integer> addingOperator,
                        multiplyingOperator,
                        relationalOperator,
                        basicDeclarationHandles,
                        statementHandles;

   public Parser(Chario c, Scanner s){
      chario = c;
      scanner = s;
      initHandles();
      initTable();
      token = scanner.nextToken();
   }

   public void reset(){
      scanner.reset();
      initTable();
      token = scanner.nextToken();
   }

   private void initHandles(){
      addingOperator = new HashSet<Integer>();
      addingOperator.add(Token.PLUS);
      addingOperator.add(Token.MINUS);
      multiplyingOperator = new HashSet<Integer>();
      multiplyingOperator.add(Token.MUL);
      multiplyingOperator.add(Token.DIV);
      multiplyingOperator.add(Token.MOD);
      relationalOperator = new HashSet<Integer>();
      relationalOperator.add(Token.EQ);
      relationalOperator.add(Token.NE);
      relationalOperator.add(Token.LE);
      relationalOperator.add(Token.GE);
      relationalOperator.add(Token.LT);
      relationalOperator.add(Token.GT);
      basicDeclarationHandles = new HashSet<Integer>();
      basicDeclarationHandles.add(Token.TYPE);
      basicDeclarationHandles.add(Token.ID);
      basicDeclarationHandles.add(Token.PROC);
      statementHandles = new HashSet<Integer>();
      statementHandles.add(Token.EXIT);
      statementHandles.add(Token.ID);
      statementHandles.add(Token.IF);
      statementHandles.add(Token.LOOP);
      statementHandles.add(Token.NULL);
      statementHandles.add(Token.WHILE);
   }

   private void accept(int expected, String errorMessage){
      if (token.code != expected)
         fatalError(errorMessage);
      token = scanner.nextToken();
   }

   private void fatalError(String errorMessage){
      chario.putError(errorMessage);
      throw new RuntimeException("Fatal error");
   }

   /*
   Three new routines for scope analysis.
   */

   private void initTable(){
      table = new SymbolTable(chario);
      table.enterScope();
      table.enterSymbol("BOOLEAN");
      table.enterSymbol("CHAR");
      table.enterSymbol("INTEGER");
      table.enterSymbol("TRUE");
      table.enterSymbol("FALSE");
   }

   private SymbolEntry enterId(){
      SymbolEntry entry = null;
      if (token.code == Token.ID)
         entry = table.enterSymbol(token.string);
      else
         fatalError("identifier expected");
      token = scanner.nextToken();
      return entry;
   }

   private SymbolEntry findId(){
      SymbolEntry entry = null;
      if (token.code == Token.ID)
         entry = table.findSymbol(token.string);
      else
         fatalError("identifier expected");
      token = scanner.nextToken();
      return entry;
   }

   public void parse(){
      subprogramBody();
      accept(Token.EOF, "extra symbols after logical end of program");
      table.exitScope();
   }

   /*
   subprogramBody =
         subprogramSpecification "is"
         declarativePart
         "begin" sequenceOfStatements
         "end" [ <procedure>identifier ] ";"
   */
   private void subprogramBody(){
      subprogramSpecification();
      accept(Token.IS, "'is' expected");
      declarativePart();
      accept(Token.BEGIN, "'begin' expected");
      sequenceOfStatements();
      accept(Token.END, "'end' expected");
      table.exitScope();
      if (token.code == Token.ID)
         findId();
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   subprogramSpecification = "procedure" identifier [ formalPart ]
   */
   private void subprogramSpecification(){
      accept(Token.PROC, "'procedure' expected");
      enterId();
      table.enterScope();
      if (token.code == Token.L_PAR)
         formalPart();
   }

   /*
   formalPart = "(" parameterSpecification { ";" parameterSpecification } ")"
   */
   private void formalPart(){
     accept(Token.L_PAR, "'(' expected");
     parameterSpecification();
     while (token.code == Token.SEMI)
     {
        token = scanner.nextToken();
        parameterSpecification();
     }
     accept(Token.R_PAR,"')' expected");

   }
   /*
   parameterSpecification = identifierList ":" mode <type>name
   */
   private void parameterSpecification(){
      identifierList();
      accept(Token.COLON, "':' expected");
      if (token.code == Token.IN)
         token = scanner.nextToken();
      if (token.code == Token.OUT)
         token = scanner.nextToken();
      findId();
   }

   /*
   declarativePart = { basicDeclaration }
   */
   private void declarativePart(){
      while (basicDeclarationHandles.contains(token.code))
         basicDeclaration();
   }

   /*
   basicDeclaration = objectDeclaration | numberDeclaration
                    | typeDeclaration | subprogramBody
   */
   private void basicDeclaration(){
      switch (token.code){
         case Token.ID:
            numberOrObjectDeclaration();
            break;
         case Token.TYPE:
            typeDeclaration();
            break;
         case Token.PROC:
            subprogramBody();
            break;
         default: fatalError("error in declaration part");
      }
   }

   /*
   objectDeclaration =
         identifierList ":" typeDefinition ";"

   numberDeclaration =
         identifierList ":" "constant" ":=" <static>expression ";"
   */
   private void numberOrObjectDeclaration(){
      identifierList();
      accept(Token.COLON, "':' expected");
      if (token.code == Token.CONST){
         token = scanner.nextToken();
         accept(Token.GETS, "':=' expected");
         expression();
      }
      else
         typeDefinition();
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   typeDeclaration = "type" identifier "is" typeDefinition ";"
   */
   private void typeDeclaration() {
      accept(Token.TYPE, "TYPE expected");
      enterId();
      accept(Token.IS, "IS expected");
      typeDefinition();
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   typeDefinition = enumerationTypeDefinition | arrayTypeDefinition
                  | range | <type>name
   */
   private void typeDefinition(){
     switch (token.code){
       case Token.L_PAR:
          enumerationTypeDefinition();
          break;
       case Token.ARRAY:
          arrayTypeDefinition();
          break;
       case Token.RANGE:
          range();
          break;
       case Token.ID:
          findId();
          break;
       default:
          fatalError("error in type definition part");
     }
   }

   /*
   enumerationTypeDefinition = "(" identifierList ")"
   */
   private void enumerationTypeDefinition(){
     accept(Token.L_PAR, "L_PAR expected");
     identifierList();
     accept(Token.R_PAR, "R_PAR expected");
   }
   /*
   arrayTypeDefinition = "array" "(" index ")" "of" <type>name
   */
   private void arrayTypeDefinition(){
     accept(Token.ARRAY, "'array' expected");
     accept(Token.L_PAR, "'(' expected");
     index();
     while(token.code == Token.COMMA){
       token = scanner.nextToken();
       index();
     }
     accept(Token.R_PAR, "')' expected");
     accept(Token.OF, "'of' expected");
     findId();
   }

   /*
   index = range | <type>name
   */
   private void index(){
     switch (token.code){
       case Token.RANGE:
          range();
          break;
       case Token.ID:
          findId();
          break;
       default: fatalError("error in index part");
     }
   }

   /*
   range = "range " simpleExpression ".." simpleExpression
   */
   private void range(){
     accept(Token.RANGE, "'Range' expected");
     simpleExpression();
     accept(Token.THRU, "'..' expected");
     simpleExpression();
   }
   /*
   identifier { "," identifer }
   */
   private void identifierList(){
     enterId();
     while(token.code == Token.COMMA){
       token = scanner.nextToken();
       enterId();
     }
   }

   /*
   sequenceOfStatements = statement { statement }
   */
   private void sequenceOfStatements(){
      statement();
      while (statementHandles.contains(token.code))
         statement();
   }

   /*
   statement = simpleStatement | compoundStatement

   simpleStatement = nullStatement | assignmentStatement
                   | procedureCallStatement | exitStatement

   compoundStatement = ifStatement | loopStatement
   */
   private void statement(){
      switch (token.code){
         case Token.ID:
            assignmentOrCallStatement();
            break;
         case Token.EXIT:
            exitStatement();
            break;
         case Token.IF:
            ifStatement();
            break;
         case Token.NULL:
            nullStatement();
            break;
         case Token.WHILE:
         case Token.LOOP:
            loopStatement();
            break;
         default: fatalError("error in statement");
      }
   }
   /*
   nullStatement = "null" ";"
   */
   private void nullStatement(){
     accept(Token.NULL, "'null' expected");
     accept(Token.NULL, "semicolon expected");
   }
   /*
   loopStatement =
         [ iterationScheme ] "loop" sequenceOfStatements "end" "loop" ";"

   iterationScheme = "while" condition
   */
   private void loopStatement(){
      if (token.code == Token.WHILE){
        token = scanner.nextToken();
        condition();
      }
      accept(Token.LOOP, "LOOP expected");
      sequenceOfStatements();
      accept(Token.END, "END expected");
      accept(Token.LOOP, "LOOP expected");
      accept(Token.SEMI, "SEMI expected");
   }
   /*
   ifStatement =
         "if" condition "then" sequenceOfStatements
         { "elsif" condition "then" sequenceOfStatements }
         [ "else" sequenceOfStatements ]
         "end" "if" ";"
   */
   private void ifStatement(){
     accept(Token.IF, "if expected");
     condition();
     accept(Token.THEN, "then expected");
     sequenceOfStatements();
     while(token.code == Token.ELSIF){
       token = scanner.nextToken();
       condition();
       accept(Token.THEN, "then expected");
       sequenceOfStatements();
     }
     if(token.code == Token.ELSE){
       token = scanner.nextToken();
       sequenceOfStatements();
     }
     accept(Token.END, "end expected");
     accept(Token.IF, "if expected");
     accept(Token.SEMI, "';' expected");
   }
   /*
   exitStatement = "exit" [ "when" condition ] ";"
   */
   private void exitStatement(){
      accept(Token.EXIT, "EXIT expected");
      if(token.code == Token.WHEN){
        token = scanner.nextToken();
        condition();
      }
      accept(Token.SEMI, "semicolon expected");
   }
   /*
   assignmentStatement = <variable>name ":=" expression ";"

   procedureCallStatement = <procedure>name [ actualParameterPart ] ";"
   */
   private void assignmentOrCallStatement(){
      name();
      if (token.code == Token.GETS){
         token = scanner.nextToken();
         expression();
      }
      accept(Token.SEMI, "semicolon expected");
   }

   /*
   condition = <boolean>expression
   */
   private void condition(){
      expression();
   }

   /*
   expression = relation { "and" relation } | { "or" relation }
   */
   private void expression(){
      relation();
      if (token.code == Token.AND)
         while (token.code == Token.AND){
            token = scanner.nextToken();
            relation();
         }
      else if (token.code == Token.OR)
         while (token.code == Token.OR){
            token = scanner.nextToken();
            relation();
         }
   }

   /*
   relation = simpleExpression [ relationalOperator simpleExpression ]
   */
   private void relation(){
     simpleExpression();
     if(relationalOperator.contains(token.code)){
       token = scanner.nextToken();
       simpleExpression();
     }
   }
   /*
  simpleExpression =
         [ unaryAddingOperator ] term { binaryAddingOperator term }
   */
   private void simpleExpression(){
      if (addingOperator.contains(token.code))
         token = scanner.nextToken();
      term();
      while (addingOperator.contains(token.code)){
         token = scanner.nextToken();
         term();
      }
   }

   /*
   term = factor { multiplyingOperator factor }
   */
   private void term(){
      factor();
      while(multiplyingOperator.contains(token.code)){
        token = scanner.nextToken();
        factor();
      }
   }
   /*
   factor = primary [ "**" primary ] | "not" primary
   */
   private void factor(){
     if(token.code == Token.NOT){
       token = scanner.nextToken();
       primary();
     } else {
       primary();
       if(token.code == Token.EXPO){
         token = scanner.nextToken();
         primary();
       }
     }
   }
   /*
   primary = numericLiteral | name | "(" expression ")"
   */
   void primary(){
      switch (token.code){
         case Token.INT:
         case Token.CHAR:
            token = scanner.nextToken();
            break;
         case Token.ID:
            name();
            break;
         case Token.L_PAR:
            token = scanner.nextToken();
            expression();
            accept(Token.R_PAR, "')' expected");
            break;
         default: fatalError("error in primary");
      }
   }

   /*
   name = identifier [ indexedComponent ]
   */
   private void name(){
      findId();
      if (token.code == Token.L_PAR)
         indexedComponent();
   }

   /*
   indexedComponent = "(" expression  { "," expression } ")"
   */
   private void indexedComponent(){
     accept(Token.L_PAR, "L_PAR expected");
     expression();
     while(token.code == Token.COMMA){
       token = scanner.nextToken();
       expression();
     }
     accept(Token.R_PAR, "R_PAR expected");
   }
 }
