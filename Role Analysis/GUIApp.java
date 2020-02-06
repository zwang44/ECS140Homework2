import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class GUIApp extends JFrame{

   // Widgets for the view
   private JLabel sourceLabel, listingLabel;
   private JTextArea sourceArea, listingArea;
   private JButton charioButton, scannerButton, parserButton;
   private JMenuItem newMI, openMI, saveSourceMI, saveListingMI, quitMI;

   // Data model
   private Chario chario;
   private Scanner scanner;
   private Parser parser;

   public GUIApp(){
      setTitle("TinyAda Parser");
      sourceLabel = new JLabel("Source Program");
      listingLabel = new JLabel("Program Listing");
      sourceArea = new JTextArea();
      listingArea = new JTextArea();
      listingArea.setEditable(false);
      charioButton = new JButton("Test Chario");
      scannerButton = new JButton("Test Scanner");
      parserButton = new JButton("Test Parser");
      JPanel labelPanel = new JPanel(new GridLayout(1, 2));
      labelPanel.add(sourceLabel);
      labelPanel.add(listingLabel);
      JPanel textPanel = new JPanel(new GridLayout(1, 2));
      textPanel.add(new JScrollPane(sourceArea));
      textPanel.add(new JScrollPane(listingArea));
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(charioButton);
      buttonPanel.add(scannerButton);
      buttonPanel.add(parserButton);
      Container container = getContentPane();
      container.add(labelPanel, BorderLayout.NORTH);
      container.add(textPanel, BorderLayout.CENTER);
      container.add(buttonPanel, BorderLayout.SOUTH);
      charioButton.addActionListener(new CharioListener());
      scannerButton.addActionListener(new ScannerListener());
      parserButton.addActionListener(new ParserListener());
      newMI = new JMenuItem("New");
      openMI = new JMenuItem("Open");
      saveSourceMI = new JMenuItem("Save source");
      saveListingMI = new JMenuItem("Save listing");
      quitMI = new JMenuItem("Quit TinyAda parser");
      JMenu fileMenu = new JMenu("File");
      fileMenu.add(newMI);
      fileMenu.add(openMI);
      fileMenu.addSeparator();
      fileMenu.add(saveSourceMI);     
      fileMenu.add(saveListingMI);     
      fileMenu.addSeparator();
      fileMenu.add(quitMI);     
      JMenuBar bar = new JMenuBar();
      bar.add(fileMenu);
      setJMenuBar(bar);
      newMI.addActionListener(new NewListener());
      openMI.addActionListener(new OpenListener());
      saveSourceMI.addActionListener(new SaveSourceListener());
      saveListingMI.addActionListener(new SaveListingListener());
      quitMI.addActionListener(new QuitListener());
      chario = new Chario(sourceArea, listingArea);
      scanner = new Scanner(chario);
      parser = new Parser(chario, scanner);
   }

   private class CharioListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
         testChario();
      }
   }

   private class ScannerListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
         testScanner();
      }
   }

   private class ParserListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
         testParser();
      }
   }

   private class NewListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
         sourceArea.setText("");
         listingArea.setText("");
      }
   }

   private class OpenListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
         listingArea.setText("");
         chario.openFile();
      }
   }
      
   private class SaveSourceListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
         chario.saveFile(sourceArea);
      }
   }

   private class SaveListingListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
         chario.saveFile(listingArea);
      }
   }

   private class QuitListener implements ActionListener{
      public void actionPerformed(ActionEvent e){
         System.exit(0);
      }
   }

   private void testChario(){
      chario.reset();
      char ch = chario.getChar();
      while (ch != Chario.EF)
         ch = chario.getChar();
      chario.reportErrors();
   }

   private void testScanner(){
      Token token;
      scanner.reset();
      token = scanner.nextToken();
      while (token.code != Token.EOF){
         chario.println(token.toString());
         token = scanner.nextToken();
      }
      chario.reportErrors();
   }

   private void testParser(){
      parser.reset();
      try{
         parser.parse();
      }
      catch(Exception e){}
      chario.reportErrors();
   }

   public static void main(String args[]){
      JFrame frm = new GUIApp();
      frm.setSize(600, 500);
      frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frm.setVisible(true);
   }
}