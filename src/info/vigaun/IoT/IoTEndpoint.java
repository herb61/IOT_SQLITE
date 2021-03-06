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
    
     static int id;
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
    public String onMessage(Session session,String message) throws ParseException, IOException, SQLException {
        
        String ID = (String) session.getUserProperties().get("ID");
        /**
         * Wird bei der ersten Verbindung die ID gesetzt
         * ID wird zur identifizierung der unterschiedlichen Sessions verwendet
         * Wenn kein Attribut (uID) gesetzt ist, ID in Session als String eintragen
         */
        if(ID == null){
            /**
             * checkTyp zerlegt exrahiert den Telegrammtyp
             * typ 0: erste Verbindung
             * typ 1: werte vom Controller empfangen
             * @param message kommt vom Controller
             * @param session aktuelle Session
             */
            checkTyp(session,message);
            /**
             * @param ID ist der identifizierende Name des Controllers
             */
            session.getUserProperties().put("ID", Integer.toString(id));
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
       if(session != null){
        try {
             database.updateDatabase(" ","nein",Integer.parseInt((String)session.getUserProperties().get("ID")));
         } 
       catch (SQLException ex) {
             Logger.getLogger(IoTEndpoint.class.getName()).log(Level.SEVERE, null, ex);
         }
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
     * @param session
     * @param json empfangene Daten
     * @throws ParseException Fehler beim JSON decoding
     * @throws IOException  falsche Eingabe
     * @throws java.sql.SQLException fehler beim Datenbankeintrag der Sensorenwerte
     */
    public void decodingJson(Session session,String json) throws ParseException, IOException, SQLException{

        int group_id = getID(json,"group");
        
        switch(group_id){
            
            case 1:
               sendValuesDatabase(json,IoTServer.sensorname[group_id],group_id);
            break;
            case 2:
               sendValuesDatabase(json,IoTServer.sensorname[group_id],group_id);
            break;
            case 3:
                sendValuesDatabase(json,IoTServer.sensorname[group_id],group_id);
            break;
            case 4:
                sendValuesDatabase(json,IoTServer.sensorname[group_id],group_id);
            break;
            case 5:
                sendValuesDatabase(json,IoTServer.sensorname[group_id -4],(group_id-4));
                sendValuesDatabase(json,IoTServer.sensorname[group_id -3],(group_id-3));
                sendValuesDatabase(json,IoTServer.sensorname[group_id -2],(group_id-2));
                sendValuesDatabase(json,IoTServer.sensorname[group_id -1],(group_id-1));
         }
    } 
    /**
     * Erstellt einen decotierten String
     * @deprecated Wird aktuell nicht verwendet
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
    
    /**
     * Die Methode entscheidet über den Telegrammtyp TYP: welche operationen ausgeführt werden
     * typ 0: erste Verbindung; typ 1: eintrag in Datenbank; typ 2: holen aus Datenbank
     * @param session aktuelle Session des Clients
     * @param json Nachricht die vom Controller oder vom einem Client kommt
     * @throws ParseException Fehler im JSON
     * @throws IOException  Falscher Variablentyp
     * @throws SQLException delegierter Fehler Datenbankeintrag
     */
    public void checkTyp(Session session, String json) throws ParseException, IOException, SQLException{
        
        int typ = getID(json,"typ");
        
        switch(typ){
            
            case 0:
                decodeTyp0(json);
                break;
            case 1:
                decodingJson(session,json);
                break;
            case 2:
                break;
                
        }
    }
    /**
     * Fügt in die Datenbank Controllername, IP, Ort, Status und verfügbare Sensoren ein
     * @param json JSON vom Controller
     * @throws ParseException 
     * @throws java.sql.SQLException 
     */
    public void decodeTyp0(String json) throws ParseException, SQLException{
            //Controller regisitrieren
            id = getID(json,"id");
            String name = getName(json,"name");
            String ip = getName(json,"ip");
            String location =getName(json,"location");
            database.insertController(id,name,ip,location);
        //Sensorgruppen eintragen  
        for(int i= 1;i<5;i++){
            database.insertGroups(getName(json,"sensor_"+i),id,i);
          }
    }
    
    /**
     * Bereitet die JSON Zeichenkette für den Datenbankeintrag vor
     * @param json Zeichenkette vom Controller
     * @param name Name des Sensors
     * @param group_id ID des Sensors
     * @throws SQLException delegierter Fehler beim Datenbankeintrag
     * @throws ParseException JSON Fehler
     */
    private void sendValuesDatabase(String json, String name, int group_id) throws SQLException, ParseException{
//        JSONParser parser = new JSONParser();
//        Object obj = parser.parse(json);
//	JSONObject jsonObject = (JSONObject) obj;
        double value = 0.00;
        value = getDouble(json,name);
        logger.info("ID " + getID(json,"id")+ getDouble(json,name) + Double.toString(value) );
        database.insertValues(value,group_id);
    }
    /**
     * Holt aus dem JSON den angeforderten Wert
     * @param json Nachricht vom Controller oder Client
     * @param name Name des Parameters im JSON
     * @return integer
     * @throws ParseException Fehler im JSON
     */
    private int getID(String json, String name) throws ParseException{
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONObject jsonObject = (JSONObject) obj;
            Long group_id = (Long)(jsonObject.get(name));
        return group_id.intValue();
    }
    /**
     * Holt aus dem JSON den Angeforderten Wert
     * @param json Nachricht vom Controller oder Client
     * @param name Name des Parameters im JSON
     * @return String
     * @throws ParseException fehler im JSON
     */
    private String getName(String json, String name) throws ParseException{
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONObject jsonObject = (JSONObject) obj;
            String name_1 = (String)(jsonObject.get(name));
        return name_1;
    }
    
        /**
     * Holt aus dem JSON den Angeforderten Wert
     * @param json Nachricht vom Controller oder Client
     * @param name Name des Parameters im JSON
     * @return double
     * @throws ParseException fehler im JSON
     */
    private double getDouble(String json, String name) throws ParseException{
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONObject jsonObject = (JSONObject) obj;
            double value = (double)(jsonObject.get(name));
        return value;
    }
}

