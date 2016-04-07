
/**
 * @author Herbert Pichler
 * @version 1.1
 * @date 06.04.2016
 */
package info.vigaun.IoT;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import javax.websocket.Session;
import org.glassfish.tyrus.server.Server;
import org.json.simple.JSONObject;
//logger
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
/**
 * Diese Klasse  startet den WesocketServer
 */
public class IoTServer {

    static Logger logger = Logger.getRootLogger();
    static String filename = "sensor.log";
    static String pattern = "%d{MM.dd.yyyy\tHH:mm:ss}\t%p\t%m %n";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        /**
         * Fileappender erstellen und dem Logger hinzufügen
         */
        Appender fileAppender = null;
        try {
            fileAppender = new FileAppender(new PatternLayout(pattern),
                    filename, true);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        logger.addAppender(fileAppender);
        logger.info("Server wurde gestartet");
        runServer();

    }
 
    /**
     * Methode startet den Server
     */
    public static void runServer() {
        Server server = new Server("localhost", 33334, "/websockets",null, IoTEndpoint.class);
        try {
            server.start();
            runMenue();
            } 
        catch (Exception e) {
            throw new RuntimeException(e);
            } 
        finally {
            server.stop();
        }
    } 
    
/**
 * Diese Methode zeigt ein einfaches Menü. Alle wichtigen funktionen implementiert
 * @throws IOException falsche Eingabe
 * @throws InterruptedException falsche
 */    
private static void runMenue() throws IOException, InterruptedException{

while(true){
Scanner s = new Scanner(System.in);
int value = 0;
// Display menue
System.out.println("=========================================");
System.out.println("|   MENÜ AUSWAHL                        |");
System.out.println("=========================================");
System.out.println("| Auswahl:                              |");
System.out.println("|        1. Update: Alle Werte          |");
System.out.println("|        2. Update: Temperatur          |");
System.out.println("|        3. Update: Luftfeuchtigkeit    |");
System.out.println("|        4. Exit                        |");
System.out.println("=========================================");
System.out.println("\n Wählen Sie eine Option aus:");

//read an integer
try{
    value = s.nextInt();
}
catch (InputMismatchException e){
    System.err.println("Input error - kein gültiger Ganzzahlenwert");
            }
// Switch construct
switch (value) {
case 1:
  System.out.println("Option 1 ausgewählt: Update: Alle Werte"); 
    if(IoTEndpoint.session1 == null){
        System.out.println("Server nicht verbunden");
        Thread.sleep(2000);
        break;
  }
  else{
    IoTEndpoint.session1.getAsyncRemote().sendText(createJSON("5"));
    Thread.sleep(2000);
    break;
  }
case 2:
  System.out.println("Option 2 ausgewählt: Update: Luftfeuchtigkeit"); 
  System.out.println("Bitte warten");
    if(IoTEndpoint.session1 == null){
        System.out.println("Server nicht verbunden");
        break;
  }
    else{
      IoTEndpoint.session1.getAsyncRemote().sendText(createJSON("3"));
      Thread.sleep(2000);
      break;
  }
case 3:
  System.out.println("Option 3 ausgewählt Update: Temperatur"); 
    if(IoTEndpoint.session1 == null){
        System.out.println("Server nicht verbunden");
        break;
  }
    else{
      IoTEndpoint.session1.getAsyncRemote().sendText(createJSON("1"));
      Thread.sleep(2000);
      break; 
    }
case 4:
   System.out.println("Beenden");
   logger.info("Server wurde beendet");
   System.exit(1);

   break;
default:
  System.out.println("Falsche Auswahl!");
  Thread.sleep(2000);
  break; 
  }
 }     
}

/**
 * Erzeugt den benötigten JSON String
 * @param value Zeichenkette für den Client mögliche Werte 1-5
 * @return JSON String
 */
 private static String createJSON(String value){
        JSONObject obj = new JSONObject();
        obj.put("request", value);
        System.out.print(obj.toString()+"\n");
        return obj.toString();   
    }  
}
