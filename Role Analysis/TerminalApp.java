import java.io.*;

public class TerminalApp{

   // Data model
   private Chario chario;
   private Scanner scanner;
   private Parser parser;

   private boolean roleTest;
   private boolean scopeTest;

   public TerminalApp(String[] args){
     for(int i = 1; i < args.length; i++){
       if(args[i].equals("-r")){
         roleTest = true;
       }
       if(args[i].equals("-s")){
         scopeTest = true;
       }
     }

      FileInputStream stream;
      try{
         stream = new FileInputStream(args[0]);
     }catch(IOException e){
         System.out.println("Error opening file.");
         return;
      }
      chario = new Chario(stream);
      //testChario();
      scanner = new Scanner(chario);
      //testScanner();
      parser = new Parser(chario, scanner, scopeTest, roleTest);
      testParser();
   }

   private void testChario(){
      char ch = chario.getChar();
      while (ch != Chario.EF)
         ch = chario.getChar();
      chario.reportErrors();
   }

   private void testScanner(){
      Token token = scanner.nextToken();
      while (token.code != Token.EOF){
         chario.println(token.toString());
         token = scanner.nextToken();
      }
      chario.reportErrors();
   }

   private void testParser(){
      try{
         parser.parse();
      }
      catch(Exception e){}
      chario.reportErrors();
   }

   public static void main(String[] args){
      new TerminalApp(args);
   }
}
