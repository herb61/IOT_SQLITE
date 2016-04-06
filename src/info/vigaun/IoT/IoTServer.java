/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.vigaun.IoT;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;
import javax.websocket.Session;
import org.glassfish.tyrus.server.Server;
import org.json.simple.JSONObject;
/**
 *
 * @author Herbert
 */
public class IoTServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        runServer();
    }
 
    public static void runServer() {
        Server server = new Server("localhost", 33334, "/websockets",null, IoTEndpoint.class);
 
        try {
            server.start();
 /*           BufferedReader reader = new BufferedReader(
                    new InputStreamReader(System.in));
            System.out.print("Please press a key to stop the server.");
            reader.readLine();*/
            runMenue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            server.stop();
        }
    } 
    
    
private static void runMenue() throws IOException, InterruptedException{
//session variable
//Session session = IoTEndpoint.session1;

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
   System.exit(1);
   break;
default:
  System.out.println("Falsche Auswahl!");
  Thread.sleep(2000);
  break; 
  }
 }     
}

 private static String createJSON(String value){
        JSONObject obj = new JSONObject();
        obj.put("request", value);
        System.out.print(obj.toString()+"\n");
        return obj.toString();   
    }  
}
