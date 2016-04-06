/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.vigaun.IoT;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
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
 * @author Herbert
 */
    @ServerEndpoint("/chat")
public class IoTEndpoint {
    
    /**
     * date for logfile
     */
    static Session session1 = null;
    SimpleDateFormat date=new SimpleDateFormat("HH:mm:ss");
    
     /**
      * static variable session for client handling
      */
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
    
    public static Session getSession(){
        return session1;
    }
    
    
    /**
     * connection with a client
     * @param session current session
     * @throws IOException 
     */
    @OnOpen
    public void onOpen(Session session) throws IOException {
        //session.getBasicRemote().sendText("Server: onOpen");
        sessions.add(session);
        session1=session;
        System.out.println("Server: Connection opened...");
    }
    /**
     * 
     * @param session current session
     * @param message message from the client
     * @return
     * @throws ParseException
     * @throws IOException 
     */
    @OnMessage
    public String onMessage(Session session,String message) throws ParseException, IOException {

        String username = (String) session.getUserProperties().get("username");
        //Wenn kein Attribut (username) gesetzt ist, username in Session eintragen
        if(username == null){
            session.getUserProperties().put("username", message);
            session.getAsyncRemote().sendText("System: sie sind jetzt verbunden als: " + message);
            writeLog(buildString( "User " + message , " ist dem Server verbunden"));
            //build the JSON string and send update all to the client
            session.getAsyncRemote().sendText(createJSON("5"));
        }
        else{
            //writeLog(buildString(username, message));
            decodingJson(message);
        }
        
        //System.out.println("Server: Message received: >"+message+"<");     
        // send echo to client
        return message;
    }

    @OnError
    public void onError(Throwable t) {
        t.printStackTrace();
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("Server: Connection closed...");
    }
    
        /**
     * 
     * @param value
     * @return 
     */
    private String createJSON(String value){
        
        JSONObject obj = new JSONObject();
        obj.put("request", value);
        System.out.print("\n send request: "+obj.toString()+"\n");
        return obj.toString();
        
    }
    /**
     * writes a log file
     * @param s string from sensor
     * @throws IOException 
     */
    private void writeLog(String s) throws IOException{
          FileWriter writer;
          File file;
          file = new File("sensorlog.txt");
          writer = new FileWriter(file ,true);
          writer.write(s);
          writer.write(System.getProperty("line.separator"));
          writer.flush();
          writer.close();         
    }
    
    
    /**
     * Teilstrings verketten Besser wäre JSON-Objekt
     * @param username
     * @param message
     * @return 
     */
    private String buildString (String username, String message){

        return "("+date.format(new Date())+") "  + username+": "+message;
    }
    
    public void decodingJson(String s) throws ParseException, IOException{
        
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(s);
	JSONObject jsonObject = (JSONObject) obj;
        double value =0.00;
        if(obj.toString().contains("temperatur") && obj.toString().contains("feuchte")){
            String allValues ="";
            value = (double)(jsonObject.get("temperatur"));
            allValues = allValues + buildDecodeJSON("temperatur", jsonObject);
            allValues = allValues + buildDecodeJSON("feuchte", jsonObject);
            allValues = allValues + buildDecodeJSON("time", jsonObject);
            allValues = allValues + buildDecodeJSON("druck", jsonObject);
            System.out.println(allValues);
            writeLog(buildString("all values",allValues));
        }
        else if(obj.toString().contains("temperatur")){
            value = (double)(jsonObject.get("temperatur"));
            System.out.println("temperatur: "+value);
            writeLog(buildString("temperatur",Double.toString(value)));
        }
        else if(obj.toString().contains("feuchte")){
            value = (double) jsonObject.get("feuchte");
            System.out.println("feuchte: "+value);
            writeLog(buildString("feuchte",Double.toString(value)));
        }
    } 
    /**
     * update all JSON decoding
     * @param s string in JSON
     * @param j JSON object
     * @return decoded JSON value
     */
    private String buildDecodeJSON(String s, JSONObject j){
      
       Double value = (double)(j.get(s));
       return (s+":"+Double.toString(value)+"; ");
    }
   
    /**
     * send message to all connected clients
     * @param session
     * @param message
     * @param username
     * @throws IOException 
     */
    private void sendAll(Session session ,String message,String username) throws IOException{
        
          for(Session client : sessions){
              client.getBasicRemote().sendText(buildString(username, message));
             }
    }
    //new
    public void sendOne(Session session ,String message,String username)throws IOException{

    // Usually this can be a field rather than a method variable
    Random rand = new Random();

    // nextInt is normally exclusive of the top value,
    // so add 1 to make it inclusive
    int randomNum = rand.nextInt((5) + 1) + 1;
        
        session.getBasicRemote().sendText(Integer.toString(randomNum));
    }
    
}

