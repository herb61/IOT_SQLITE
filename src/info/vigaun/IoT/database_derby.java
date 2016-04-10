/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.vigaun.IoT;

import static info.vigaun.IoT.IoTServer.logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Herbert
 */
public class database_derby {
    
    public static void insertController(int id,String name,String ip, String location) {
        try {
            Connection conn = connectToDatabase();
            Statement s = conn.createStatement();             
            /**
             * Abfrage und Test ob die ID schon vorhanden ist
             * Nach Neustart wird die ID des Controller wieder abgefragt.
             * Die ID des Controllers darf aber nur einmal vorhanden sein. Sollte dieser eingetragen sein, dann wird der Vorgang abgebrochen.
             */
            s.execute("SELECT CONTROLLER_ID from SENSOR.CONTROLLER");
            ResultSet as = s.getResultSet();
            while (as.next()) {
                /**
                 * Überprüft ob der Controller schon registriert ist.
                 * Wenn Ja, dann wird ein Update der IP und Onlinestatus gemacht
                 */
                if(as.getInt("CONTROLLER_ID") == id){
                    updateDatabase(ip,"ja", id);
                    return; 
                }
            }
            /**
             * Vorbereitung des Insert. Muss bei Variablen sein
             */
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO SENSOR.CONTROLLER "
                                                 + "(controller_id, name,location,ip, online) "
                                                 + "VALUES (?, ?, ?,?,?)");
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3,location);
            pstmt.setString(4,ip);
            pstmt.setString(5,"ja");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.info("Fehler: "+e.toString());
        }
        
    }
    /**
     * Updates die datenbank bei neuverbindung des Controllers
     * @param conn aktive Datenbankverbindung
     * @param ip neue IP-Adresse
     * @throws SQLException 
     */
    public static void updateDatabase( String ip, String active, int id) throws SQLException{
    Connection conn = connectToDatabase();
    Statement s = conn.createStatement(); 
    String updateTableSQL = "UPDATE SENSOR.CONTROLLER"
                        + " SET IP = ?,ONLINE = ?"
                        + "WHERE CONTROLLER_ID = ?";
    PreparedStatement pstupd;
        try {
            pstupd = conn.prepareStatement(updateTableSQL);
            pstupd.setString(1,ip);
            pstupd.setString(2,active);
            pstupd.setInt(3,id);
            pstupd.executeUpdate();
        } 
        catch (SQLException e) {
            logger.info("Fehler: "+e.toString());
        }
    }
   /**
    * Fügt die vorhandenen Sensoren in die Datenbank ein.
    * Es wird geprüft ob schon Einträge vorhanden sind.
    * @param sensor Name des Sensors
    * @param c_id   Controller_ID
    * @param g_id   Group_ID (Sensor)
    * @throws SQLException 
    */
   public static void insertGroups(String sensor, int c_id,int g_id) throws SQLException{
       Connection conn = connectToDatabase();
       Statement s = conn.createStatement();
       String insertTable = "INSERT INTO SENSOR.GROUPS"
             + "(GROUP_ID,NAME,CONTROLLER_ID) VALUES"
             + "(?,?,?)";
       
       PreparedStatement pstupd;
       
        s.execute("SELECT GROUP_ID from SENSOR.GROUPS");
        ResultSet as = s.getResultSet();
            while (as.next()) {
                /**
                 * Überprüft ob die Sensoren schon registriert ist.
                 * Wenn Ja, dann wird ein Update gemacht
                 */
                if(as.getInt("GROUP_ID") == g_id){
                    insertTable = "UPDATE SENSOR.GROUPS "
                            + "SET GROUP_ID = ?,NAME = ?,CONTROLLER_ID = ? "
                            + "WHERE GROUP_ID = ?";
            try {
                pstupd = conn.prepareStatement(insertTable);
                pstupd.setInt(1,g_id);
                pstupd.setString(2,sensor);
                pstupd.setInt(3,c_id);
                pstupd.setInt(4,g_id);
                pstupd.executeUpdate();
                return;
            } 
            catch (SQLException e) {
                logger.info("Fehler: "+e.toString());
            }
          }
        }
       
        try {
            pstupd = conn.prepareStatement(insertTable);
            pstupd.setInt(1,g_id);
            pstupd.setString(2,sensor);
            pstupd.setInt(3,c_id);
            pstupd.executeUpdate();
        } 
        catch (SQLException e) {
            logger.info("Fehler: "+e.toString());
        }
       
   }
    
   public static void  insertValues(double values,int group_id) throws SQLException{
       
       Date date = new Date();
       SimpleDateFormat da = new SimpleDateFormat("dd.MM.yyyy");
       SimpleDateFormat ti = new SimpleDateFormat("hh:mm:ss");      
       Connection conn = connectToDatabase();
       Statement s = conn.createStatement();
       String insertTable = "INSERT INTO SENSOR.MESSUREVALUES"
               + "(DATE,TIME,VALUE,GROUP_ID) VALUES"
               + "(?,?,?,?)";
       PreparedStatement pstupd;
        try {
            pstupd = conn.prepareStatement(insertTable);
            pstupd.setString(1,da.format(date));
            pstupd.setString(2,ti.format(date));
            pstupd.setDouble(3,values);
            pstupd.setInt(4,group_id);
            pstupd.executeUpdate();
        } 
        catch (SQLException e) {
           logger.info("Fehler: "+e.toString());
        }  
   }
   /**
    * Holt den aktuellen Wert des gewälten Sensors aus der Datenbank
    * @param group_id Sensor Gruppe
    * @throws SQLException Fehler bei der Abfrage
    */
   public static void getFromDataBase(int group_id) throws SQLException{
       
       Connection conn = connectToDatabase();
       Statement s = conn.createStatement();
       String queryTable = "SELECT NAME,DATE,TIME,VALUE from Sensor.MESSUREVALUES m join Sensor.GROUPS g "
               + "ON m.GROUP_ID = g.GROUP_ID "
               + "WHERE m.GROUP_ID = "+group_id
               + "ORDER by VALUES_ID "
               + "DESC FETCH FIRST ROW ONLY";
       ResultSet rs = s.executeQuery(queryTable);
      while (rs.next()) {
                String name = rs.getString("name");
                Date date = rs.getDate("date");
                Date time = rs.getTime("time");
                double value = rs.getDouble("value");
           System.out.println(name + "  " + date+"   "+time+"   "+value);
      }
       
   }
   
   
   
    /**
     * Herstellung einer Datenbankverbindung
     * @return Connection Variable
     */
    public static Connection connectToDatabase(){
     
        Connection conn = null;
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Derby driver not found.");
        }
        try {
            /**
             * Aufbau der Datenbankverbindung
             */
            String dbURL2 = "jdbc:derby://localhost/sensor;create=true";
            String user = "root";
            String password = "root";
            conn = DriverManager.getConnection(dbURL2, user, password);
           } 
        catch (SQLException e) {
            logger.info("Fehler: "+e.toString());
        }
      return conn;
    }
    
    
    
    
}
