/**
 * @author Herbert Pichler
 * IoTEndpoint WebsocketEndpointServer Klasse
 * Diese Klasse beinhaltet alle nötigen Methoden zum Senden und Empfangen der JSON Srings
 */
package info.vigaun.IoT;

import static info.vigaun.IoT.IoTServer.logger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 * Klasse IoTEndpoint
 */
    @ServerEndpoint("/chat")
public class IoTEndpoint {
    
     static Long id;
    /**
     * static session Variable für die anderen Klassen
     */
    static Session session1 = null;
     /**
      * static Set sessions für die Clients. 
      * Alle verbundenen Clients werden mittels dieses Set verwaltet
      */
    public static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());  
    /**
     * Methode wird aufgerufen wenn sich ein Client verbindet
     * @param session aktuelle Session
     * @throws IOException 
     * die Variable session1 wird inizialisiert
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.add(session);
        session1=session;
        System.out.println("Server: Connection opened...");
    }
    /**
     * 
     * @param session aktuelle Session
     * @param message Nachricht (JSON) von einem Client
     * @return Nachricht vom Client
     * @throws ParseException (JSON Parser), 
     * @throws IOException
     */
    @OnMessage
    public String onMessage(Session session,String message) throws ParseException, IOException {
        
        String ID = (String) session.getUserProperties().get("ID");
        /**
         * Wird bei der ersten Verbindung die ID gesetzt
         * ID wird zur identifizierung der unterschiedlichen Sessions verwendet
         * Wenn kein Attribut (uID) gesetzt ist, ID in Session als String eintragen
         */
        if(ID == null){
            
            /**
             * decodingJson zerlegt den JSON String und trägt den Controller in die Datenbank ein
             * @param message kommt vom Controller
             * @param session aktuelle Session
             */
            decodingJson(session,message);
            /**
             * @param ID ist der identifizierende Name des Controllers
             */
            session.getUserProperties().put("ID", Long.toString(id));
            /**
             * @deprecated nachstehenden zwei Methoden sind für den Chatserver
             * werden hier nicht gebraucht
             */
            //session.getAsyncRemote().sendText("System: sie sind jetzt verbunden als: " + message);
            //writeLog(buildString( "User " + message , " ist dem Server verbunden"));
            /**
             * sendet einen JSON String {"request":5} bedeutet Update alle Werte
             */
            session.getAsyncRemote().sendText(createJSON("5"));
        }
        else{
            /**
             * empfangene Nachricht wird decodiert und entsprechende Aktion ausgeführt
             */
            decodingJson(session, message);
        }
           
        /*
         * @deprecated
         * sendet Echo zum Client
        */
        return message;
    }
   /**
    * Methode gibt Fehler des WebSocketServers aus
    * @param t  Fehlerobjekt welches geworfen wird
    */
    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }
    
    /**
     * Methode wird verwendet wenn der Client die Verbindung trennt
     * @param session Session wird aus dem Set gelöscht
     */
    @OnClose
    public void onClose(Session session) {
       try {
             database.updateDatabase(" ","nein",Integer.parseInt((String)session.getUserProperties().get("ID")));
         } 
       catch (SQLException ex) {
             Logger.getLogger(IoTEndpoint.class.getName()).log(Level.SEVERE, null, ex);
         }
        sessions.remove(session);
 
        System.out.println("Server: Connection closed...");
    }
    
    /**
     * Methode erzeugt einen request JSON String ({"request":value}
     * @param value Ganzzahl für das Update
     * mögliche Werte 1:Temperatur innen,  2:Temperatur aussen, 3:Heizuungstemperatur, 
     * 4:Fensterstatus: true/false, 5:Haustür:true/false 
     * true = offen; false geschlossen
     * @return JSON String
     */
    private String createJSON(String value){
        JSONObject obj = new JSONObject();
        obj.put("request", value);
        System.out.print("\n send request: "+obj.toString()+"\n");
        return obj.toString();
    }
        
    /**
     * Decodiert den empfangenen JSON String und schreibt in ein Logfile 
     * @param json empfangene Daten
     * @throws ParseException Fehler beim JSON decoding
     * @throws IOException  falsche Eingabe
     */
    public void decodingJson(Session session,String json) throws ParseException, IOException{
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(json);
	JSONObject jsonObject = (JSONObject) obj;
        double value =0.00;
        /**
         * Controller wird in die Datenbank eingetragen
         */
        if((obj.toString().contains("id")) && (obj.toString().contains("name"))){
            id = (Long) (jsonObject.get("id"));
            String name = (String) (jsonObject.get("name"));
            String ip = (String) (jsonObject.get("ip"));
            String location = (String)(jsonObject.get("location"));
            database.insertController(id.intValue(),name,ip,location);
        }
        /**
         * Fullupdate wird gesendet
         */
        if(obj.toString().contains("temp_in") && obj.toString().contains("temp_out")){
            String allValues ="";
            value = (double)(jsonObject.get("temp_in"));
            allValues = allValues + buildDecodeJSON("Innentemperatur: ","temp_in", jsonObject);
            allValues = allValues + buildDecodeJSON("Aussentemperatur: ","temp_out", jsonObject);
            allValues = allValues + buildDecodeJSON("Heizung: ","heizung", jsonObject);
            allValues = allValues + buildDecodeJSON("Luftdruck: ","luftdruck", jsonObject);
           logger.info("ID " + (String)session.getUserProperties().get("ID")+" "+ allValues);
        }
        else if(obj.toString().contains("temp_in")){
            value = (double)(jsonObject.get("temp_in"));
            logger.info("ID " + (String)session.getUserProperties().get("ID")+" Innentemperatur: "+ Double.toString(value));
        }
        else if(obj.toString().contains("temp_out")){
            value = (double) jsonObject.get("temp_out");
            logger.info("ID " + (String)session.getUserProperties().get("ID")+" Aussentemperatur: "+ Double.toString(value));
        }
        else if(obj.toString().contains("heizung")){
            value = (double) jsonObject.get("heizung");
            logger.info("ID " + (String)session.getUserProperties().get("ID")+" Heizung: "+ Double.toString(value));
        }
    } 
    /**
     * Erstellt einen decotierten String
     * @param s Name des Wertes im JSON
     * @param name Name des Wertes klartext
     * @param j JSON object
     * @return Zeichenkette Wertepaar: (Temperatur:23,12)
     */
    private String buildDecodeJSON(String name,String s, JSONObject j){
       Double value = (double)(j.get(s));
       return (name + Double.toString(value)+"; ");
    }
   
    /**
     * sendet eine Nachricht an alle verbundenen Clients
     * @param message requestnummer für Clients
     * @throws IOException 
     */
    private void sendAll(String message) throws IOException{
          for(Session client : sessions){
              client.getBasicRemote().sendText(createJSON(message));
             }
    }
    
    /**
     * @deprecated wurde für eine Zufallszahl verwendet
     * @param session aktuelle Verbindung
     * @throws IOException falsche Eingabe
     */
    public void sendOne(Session session )throws IOException{
    Random rand = new Random();
    /**
     * erzeugt eine Zusatzzahl zwischen 1 und 5 (1,2,3,4,5)
     */
    int randomNum = rand.nextInt((5) + 1) + 1;
    session.getBasicRemote().sendText(Integer.toString(randomNum));
    }
    
}

