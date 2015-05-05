package org.nyu.edu.dlts.server;

import com.thoughtworks.xstream.XStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataField;

/**
 * Simple class for connecting to the mysql database and getting the 
 * schema data
 * 
 * @author nathan
 */
public class DatabaseUtil {
    // The user name and password for connecting to the mysql database

    public static String database;
    public static String username;
    public static String password;
    public static String hosts;
    public static String port;
    
    // the database connection object
    private static Connection conn;

    /**
     * Method to return the list of users for authentication
     * @return 
     */
    public static HashMap<String, String> getUserLoginInfo() {
        HashMap<String, String> userInfo = new HashMap<String, String>();

        String query = "SELECT * FROM " + database + ".Users";

        try {
            getConnection();
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String userId = rs.getString("userid");
                String password = rs.getString("password");
                userInfo.put(userId, password);
            }
            
            closeConnection();
        } catch (Exception ex) {
            Logger.getLogger(DatabaseUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Number of users " + userInfo.size());

        return userInfo;
    }

    /**
     * Method to read the schema data list from the database. The last row
     * in the table is returned. The other entries for backup purposes
     * 
     * @param schemaDataType
     * @return
     * @throws Exception 
     */
    public static ArrayList<SchemaData> getSchemaDataList(String schemaDataType) {
        ArrayList<SchemaData> schemaDataList = null;

        try {
            getConnection();
            
            // get the file name to save to
            String query = "SELECT * FROM " + database
                    + ".SchemaDataList WHERE schemaDataType = '" + schemaDataType + "' ORDER BY schemaDataListId DESC LIMIT 1";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                String xmlContent = rs.getString("schemaDataList");

                // convert object from xml then return the object
                XStream xstream = new XStream();

                schemaDataList = (ArrayList<SchemaData>) xstream.fromXML(xmlContent);
            }

            closeConnection();
        } catch (Exception ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return schemaDataList;
    }

    /**
     * Method to save the schema data object as xml to the database
     * 
     * @param schemaData 
     */
    public static void saveSchemaDataList(String userId, ArrayList<SchemaData> schemaDataList) throws Exception {
        getConnection();
        
        // get the file name to save to
        String insertSQL = "INSERT INTO " + database
                + ".SchemaDataList (schemaDataType, schemaDataList, modifyDate, modifyBy) "
                + "VALUES(?,?,?,?)";

        // convert object to xml then save to file
        XStream xstream = new XStream();

        String xmlContent = xstream.toXML(schemaDataList);

        // get the schema data type
        String schemaDataType = schemaDataList.get(0).getType();

        // get the Time Stamp now
        Timestamp modifyDate = new Timestamp(new Date().getTime());

        // save the schemadata list to the database
        PreparedStatement preparedStatement = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

        preparedStatement.setString(1, schemaDataType);
        preparedStatement.setString(2, xmlContent);
        preparedStatement.setTimestamp(3, modifyDate);
        preparedStatement.setString(4, userId);

        preparedStatement.executeUpdate();
        
        closeConnection();
    }
    
    /**
     * Method to read the schema data values from the database. The last row
     * in the table is returned. The other entries for backup purposes
     * 
     * @param schemaDataType
     * @return
     * @throws Exception 
     */
    public static HashMap<String, SchemaData> getDataValues(String schemaDataType) {
        HashMap<String, SchemaData> dataValueMap = null;

        try {
            getConnection();
            
            // get the file name to save to
            String query = "SELECT * FROM " + database
                    + ".SchemaDataList WHERE schemaDataType = '" + schemaDataType + "' ORDER BY schemaDataListId DESC LIMIT 1";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                String xmlContent = rs.getString("schemaDataList");

                // convert object from xml then return the object
                XStream xstream = new XStream();

                dataValueMap = (HashMap<String, SchemaData>) xstream.fromXML(xmlContent);
            }
            
            closeConnection();
        } catch (Exception ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return dataValueMap;
    }

    /**
     * Method to save the schema data object as xml to the database
     * 
     * @param schemaData 
     */
    public static void saveDataValues(String userId, String schemaDataType, HashMap<String, SchemaData> dataValueMap) throws Exception {
        getConnection();
        
        // get the file name to save to
        String insertSQL = "INSERT INTO " + database
                + ".SchemaDataList (schemaDataType, schemaDataList, modifyDate, modifyBy) "
                + "VALUES(?,?,?,?)";

        // convert object to xml then save to file
        XStream xstream = new XStream();

        String xmlContent = xstream.toXML(dataValueMap);

        // get the Time Stamp now
        Timestamp modifyDate = new Timestamp(new Date().getTime());

        // save the schemadata list to the database
        PreparedStatement preparedStatement = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);

        preparedStatement.setString(1, schemaDataType);
        preparedStatement.setString(2, xmlContent);
        preparedStatement.setTimestamp(3, modifyDate);
        preparedStatement.setString(4, userId);

        preparedStatement.executeUpdate();
        
        closeConnection();
    }
    
    /**
     * Method to establish a connection to the Variations mysql database
     * 
     * @return
     * @throws SQLException 
     */
    public static Connection getConnection() throws Exception {
        // check to see if the connection is not null or closed 
        if (conn != null && !conn.isClosed()) {
            return conn;
        }

        Class.forName("com.mysql.jdbc.Driver");

        Properties connectionProps = new Properties();
        connectionProps.put("user", username);
        connectionProps.put("password", password);

        conn = DriverManager.getConnection(
                "jdbc:mysql://" + hosts + ":" + port + "/",
                connectionProps);

        System.out.println("Connected to database ...");

        return conn;
    }
    
    /**
     * Method to close the database connection
     */
    public static void closeConnection() throws Exception {
        if(conn != null) {
            conn.close();
        }
    }
    
    /**
     * A convenient method for setting the test database
     * connection information in one place
     */
    public static void setupTestDatabaseInfo() {
        // set the location information
        DatabaseUtil.database = "SchemaMap";
        DatabaseUtil.hosts = "localhost";
        DatabaseUtil.port = "3306";
        DatabaseUtil.username = "nathan";
        DatabaseUtil.password = "none";
    }

    /**
     * Main method for testing the saving to database
     * 
     * @param args 
     */
    public static void main(String[] args) {
        try {
            // setup connection info
            setupTestDatabaseInfo();
            
            // get the users
            getUserLoginInfo();
            
            // get any saved schemaDataList
            ArrayList<SchemaData> savedList = getSchemaDataList(SchemaData.AT_TYPE);

            // get and try saving schemadata list to the database
            String ip = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas/AT/index.txt";
            ATSchemaUtil.setIndexPath(ip);

            ArrayList<SchemaData> schemaDataMapList = new ArrayList<SchemaData>();
            HashMap<String, ArrayList<String>> fieldsMap = ATSchemaUtil.processSchemaIndex();

            // process all the the entries
            for (String schemaName : fieldsMap.keySet()) {
                ArrayList<String> fieldInfo = fieldsMap.get(schemaName);
                Collections.sort(fieldInfo);

                ArrayList<SchemaDataField> schemaDataFields = new ArrayList<SchemaDataField>();

                for (String info : fieldInfo) {
                    String[] sa = info.split("\\s*,\\s*");
                    if(sa.length == 2) {
                        String name = sa[0];
                        String type = sa[1];

                        SchemaDataField schemaDataField = new SchemaDataField(name, type);
                        schemaDataFields.add(schemaDataField);
                    }
                }

                SchemaData schemaData = new SchemaData(schemaName, schemaDataFields);
                schemaData.setType(SchemaData.AT_TYPE);
                schemaDataMapList.add(schemaData);
            }

            saveSchemaDataList("nathan", schemaDataMapList);
        } catch (Exception ex) {
            Logger.getLogger(DatabaseUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
