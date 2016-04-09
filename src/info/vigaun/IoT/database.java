/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.vigaun.IoT;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Herbert
 */
public class database {
    
    public static void insertController(int id,String name,String ip, String location) {
        try {
            Connection conn = connectToDatabase();
            Statement s = conn.createStatement();             
            /**
             * Abfrage und Test ob die ID schon vorhanden ist
             * Nach Neustart wird die ID des Controller wieder abgefragt.
             * Die ID des Controllers darf aber nur einmal vorhanden sein. Sollte dieser eingetragen sein, dann wird der Vorgang abgebrochen.
             */
            s.execute("SELECT CONTROLLER_ID from DERBYTEST.CONTROLLER");
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
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO DERBYTEST.CONTROLLER (controller_id, name,location,ip, online) VALUES (?, ?, ?,?,?)");
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3,location);
            pstmt.setString(4,ip);
            pstmt.setString(5,"ja");
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
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
    String updateTableSQL = "UPDATE DERBYTEST.CONTROLLER SET IP = ?,ONLINE = ? " + " WHERE CONTROLLER_ID = ?";
    PreparedStatement pstupd;
        try {
            pstupd = conn.prepareStatement(updateTableSQL);
            pstupd.setString(1,ip);
            pstupd.setString(2,active);
            pstupd.setInt(3,id);
            pstupd.executeUpdate();
        } 
        catch (SQLException ex) {
            Logger.getLogger(database.class.getName()).log(Level.SEVERE, null, ex);
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
        catch (SQLException ex) {
            ex.printStackTrace();
        }
      return conn;
    }
    
    
    
    
}
