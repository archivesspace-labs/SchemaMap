/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.nyu.edu.dlts.client.SchemaDataService;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataField;

/**
 *
 * @author nathan
 */
public class SchemaDataServiceImpl extends RemoteServiceServlet implements SchemaDataService {
    // Array List for storing field information
    private ArrayList<SchemaData> schemaDataListAT = null;
    
    private ArrayList<SchemaData> schemaDataListEAD = null;
    
    private ArrayList<SchemaData> schemaDataListAS = null;
    
    // the base location for the schema code 
    private String schemaCodeBaseUrl = "";
    
    // Hashmap for storing the mapping information for the fields 
    private HashMap<String, String> mappingInfo = new HashMap<String, String>();
    
    // Hashmap for storing the data values that need to be mapped
    private HashMap<String, SchemaData> dataValueMapAT = new HashMap<String, SchemaData>();
    
    // Holds user login information
    private HashMap<String, String> userInfo = new HashMap<String, String>();
    
    private boolean upgrade = false;
    
    // variable needed for saving the mapping document
    private String indexPathAT = "";
        
    // used to generate the url to generated mapping documents
    private String baseURL = "";
    /**
     * Setup the url for accessing the schema list
     */
    @Override
    public void init() {
        // set the URI for accessing the schema index
        ServletContext context = getServletConfig().getServletContext();
        
        try {
            // see whether to upgrade the schema
            String answer = getServletConfig().getInitParameter("schemaListAT.upgrade");
            if(answer.equalsIgnoreCase("yes")) {
                upgrade = true;
            }
            
            // get and set the database connection information
            String hosts = getServletConfig().getInitParameter("database.hosts");
            String port = getServletConfig().getInitParameter("database.port");
            String username = getServletConfig().getInitParameter("database.username");
            String password = getServletConfig().getInitParameter("database.password");
            String database = getServletConfig().getInitParameter("database.name");
            
            DatabaseUtil.database = database;
            DatabaseUtil.hosts = hosts;
            DatabaseUtil.port = port;
            DatabaseUtil.username = username;
            DatabaseUtil.password = password;
            
            // open the database connection and load the user
            //DatabaseUtil.setupTestDatabaseInfo();
            DatabaseUtil.getConnection();
            
            userInfo = DatabaseUtil.getUserLoginInfo();
            
            String saveDirectory = context.getRealPath("/schemas");
            FileUtil.saveDirectory = saveDirectory;
            
            String versionDirectory = context.getRealPath("/schemas/versions");
            FileUtil.versionDirectory = versionDirectory;
            
            // set the index uri for AT
            String indexPath = context.getRealPath("/schemas/AT/index.txt");
            indexPathAT = ATSchemaUtil.setIndexPath(indexPath);
            
            // set the index uri for EAD schema file
            String schemaPath = context.getRealPath("/schemas/EAD/ead.xsd");
            EADSchemaUtil.setSchemaPath(schemaPath);
            
            // set the index uri and url for ASpace docs
            indexPath = context.getRealPath("/schemas/AS/index.txt");
            schemaPath = context.getRealPath("/schemas/AS/v1.2.0");
            ASpaceSchemaUtil.setIndexPath(indexPath, schemaPath);
            
            String subSchemaPath = context.getRealPath("/schemas/AS/sub_schemas.txt");
            ASpaceSchemaUtil.setSubSchemaPath(subSchemaPath);
            
            // set the url root for ASpace schema code
            schemaCodeBaseUrl = "http://archivesspace.github.io/archivesspace/doc";
            
            // load the stored AT and Schema data which has edited notes
            //ArrayList<SchemaData> savedSchemaDataList = FileUtil.getSchemaDataList(SchemaData.AT_TYPE);
            ArrayList<SchemaData> savedSchemaDataList = DatabaseUtil.getSchemaDataList(SchemaData.AT_TYPE);
            
            if(savedSchemaDataList != null) {
                if(!upgrade) {
                    schemaDataListAT = savedSchemaDataList;
                } else {
                    getSchemaDataAT();
                    copySavedValuesToSchemaDataList(savedSchemaDataList);
                    DatabaseUtil.saveSchemaDataList("admin_upgrade", schemaDataListAT);
                    upgrade = false;
                }
            } 
        } catch (Exception ex) {
            Logger.getLogger(SchemaDataServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Load schema fields by calling the utils method
     * 
     * @return HashMap containing FieldList
     */
    public ArrayList<SchemaData> getSchemaDataAT() {
        try {
            if(schemaDataListAT == null) {
                schemaDataListAT = new ArrayList<SchemaData>();
                
                HashMap<String, ArrayList<String>> fieldsMap = ATSchemaUtil.processSchemaIndex();
                
                // process all the the entries
                for(String schemaName: fieldsMap.keySet()) {
                    ArrayList<String> fieldInfo = fieldsMap.get(schemaName);
                    Collections.sort(fieldInfo);
                    
                    ArrayList<SchemaDataField> schemaDataFields = getSchemaDataFields(schemaName, fieldInfo);
                    SchemaData schemaData = new SchemaData(schemaName, schemaDataFields);
                    schemaData.setType(SchemaData.AT_TYPE);
                    
                    schemaDataListAT.add(schemaData);
                }
                
                // save the list now
                if(!upgrade) {
                    DatabaseUtil.saveSchemaDataList("admin", schemaDataListAT);
                }
            }
            
            return schemaDataListAT;
        } catch (Exception ex) {
            Logger.getLogger(SchemaDataServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            
            return null;
        }
    }
    
    /**
     * Load schema fields by calling the utils method
     * 
     * @return HashMap containing FieldList
     */
    public ArrayList<SchemaData> getSchemaDataEAD() {
        try {
            if(schemaDataListEAD == null) {
                schemaDataListEAD = new ArrayList<SchemaData>();
                
                HashMap<String, ArrayList<String>> fieldsMap = EADSchemaUtil.processSchema();
                
                // process all the the entries
                for(String schemaName: fieldsMap.keySet()) {
                    ArrayList<String> fieldInfo = fieldsMap.get(schemaName);
                    Collections.sort(fieldInfo);
                    
                    ArrayList<SchemaDataField> schemaDataFields = getSchemaDataFields(schemaName, fieldInfo);
                    SchemaData schemaData = new SchemaData(schemaName, schemaDataFields);
                    schemaData.setType(SchemaData.EAD_TYPE);
                    
                    schemaDataListEAD.add(schemaData);
                }
                
                // save the list now
                if(!upgrade) {
                    DatabaseUtil.saveSchemaDataList("admin", schemaDataListEAD);
                }
            }
            
            return schemaDataListEAD;
        } catch (Exception ex) {
            Logger.getLogger(SchemaDataServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            
            return null;
        }
    }
    
    /**
     * Load schema fields by calling the utils method
     * 
     * @return HashMap containing FieldList
     */
    public ArrayList<SchemaData> getSchemaDataAS() {
        try {
            // untill the data model stablilizes the set this to null so it reloads all the time
            schemaDataListAS = null;
            
            if(schemaDataListAS == null) {
                schemaDataListAS = new ArrayList<SchemaData>();
                
                HashMap<String, ArrayList<String>> fieldsMap = ASpaceSchemaUtil.processSchemaIndex();
                
                // process all the the entries
                for(String schemaName: fieldsMap.keySet()) {
                    ArrayList<String> fieldInfo = fieldsMap.get(schemaName);
                    Collections.sort(fieldInfo);
                    
                    ArrayList<SchemaDataField> schemaDataFields = getSchemaDataFields(schemaName, fieldInfo);
                    SchemaData schemaData = new SchemaData(schemaName, schemaDataFields);
                    schemaData.setType(SchemaData.AS_TYPE);
                    
                    // set the url for the schema code
                    String url = schemaCodeBaseUrl + "/" + schemaName + "_schema.html";
                    schemaData.setUrl(url);
                    
                    schemaDataListAS.add(schemaData);
                }
                
                // save the list now TODO -- Uncomment this when data model is stable 
                // DatabaseUtil.saveSchemaDataList("admin", schemaDataListAS);
            }
            
            return schemaDataListAS;
        } catch (Exception ex) {
            Logger.getLogger(SchemaDataServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            
            return null;
        }
    }
    
    /**
     * Method to generate an array list of schema data field objects  
     * 
     * @param fieldInfo
     * @return 
     */
    private ArrayList<SchemaDataField> getSchemaDataFields(String schemaName, ArrayList<String> fieldInfo) {
        ArrayList<SchemaDataField> schemaDataFields = new ArrayList<SchemaDataField>();
        
        for(String info: fieldInfo) {
            String name;
            String type;
            String mappedTo = ""; 
            
            String[] sa = info.split("\\s*\t\\s*", 3);
            
            name = sa[0];
            
            if(sa.length == 2) {
                type = sa[1];
            } else if(sa.length == 3) {
                type = sa[1];
                mappedTo = sa[2];
            } else {
                type = "UNKNOWN";
            }
            
            SchemaDataField schemaDataField = new SchemaDataField(name, type);
            schemaDataField.setMappedTo(mappedTo);
            schemaDataFields.add(schemaDataField);
        }
        
        return schemaDataFields;
    }
    
    /**
     * Method to update information about a schemaData including mapping information
     * 
     * @param schemaData
     * @return 
     */
    public synchronized String updateSchemaData(String userId, SchemaData schemaData) {
        try {
            if(schemaData.getType().equals(SchemaData.AT_TYPE)) {
                updateSchemaDataList(userId, schemaData, schemaDataListAT);
            } else if(schemaData.getType().equals(SchemaData.AT_VALUE)) {
                updateDataValues(userId, schemaData);
            } else {
                return "Cannot Update -- " + schemaData.getName();
            }
        
            return "Updated -- " + schemaData.getName();
        } catch(Exception ex) {
            Logger.getLogger(SchemaDataServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return "Failed Update -- " + schemaData.getName();
        }
    }
    
    /**
     * Method to find and replace the stored schemaData with the one from the client
     * 
     * @param schemaData
     * @param schemaDataListAT 
     */
    private void updateSchemaDataList(String username, SchemaData schemaData, ArrayList<SchemaData> schemaDataList) throws Exception {
        for(int i = 0; i < schemaDataList.size(); i++) {
            SchemaData sd = schemaDataList.get(i);
            
            if(sd.getId() == schemaData.getId()) {
                schemaDataList.set(i, schemaData);
                break;
            }
        }
        
        // now save the schema data to an xml file
        //FileUtil.saveSchemaDataList(schemaDataList);
        DatabaseUtil.saveSchemaDataList(username, schemaDataList);
    }
    
    /**
     * Method to get Hashmap containing a list of values
     * 
     * @param type  This is not used for now
     * @return 
     */
    public HashMap<String, SchemaData> getDataValues(String type) {
        try {
            // try reading this from the database
            dataValueMapAT = DatabaseUtil.getDataValues(type);
            
            System.out.println("Return the data value for: " + type);
            
            if(dataValueMapAT == null) {
                dataValueMapAT = ATSchemaUtil.getDataValues();
                
                // save it to the database now
                DatabaseUtil.saveDataValues("admin", type, dataValueMapAT);
            }
            
            return dataValueMapAT;
        } catch (Exception ex) {
            Logger.getLogger(SchemaDataServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    /**
     * Method to update the HashMap containing a list of AT values
     * 
     * @param username
     * @param type
     * @param dataValuesMap
     * @return 
     */
    public String updateDataValues(String username, SchemaData schemaData) {
        String message = "sucess -- " + username;

        try {
            if (schemaData.getType().equals(SchemaData.AT_VALUE)) {
                dataValueMapAT.put(schemaData.getName(), schemaData);
                
                DatabaseUtil.saveDataValues(username, SchemaData.AT_VALUE, dataValueMapAT);
            } else {
                message = "Failed to update -- Unknown data values ...";
            }
        } catch (Exception e) {
            message = "Failed to update -- can't save to the database ...";
        }

        return message;
    }
    
    /**
     * Method to authenticate users
     * 
     * @param username
     * @param password
     * @return 
     */
    public String authorize(String username, String password) {
        String message = "Login failed ...";
        
        if(userInfo.containsKey(username)) {
            if(password.equals(userInfo.get(username))) {
                message = "authorized -- " + username;
            }
        }
        
        return message;
    }
    
    /**
     * Method to upgrade the schema data list. Only works for AT now
     * 
     * @param savedSchemaDataList 
     */
    private void copySavedValuesToSchemaDataList(ArrayList<SchemaData> savedSchemaDataList) {
        // first lets makes finding things really fast by using hash maps
        HashMap<String, SchemaDataField> schemaDataFieldMap = new HashMap<String, SchemaDataField>();
        HashMap<String, SchemaData> schemaDataInfoMap = new HashMap<String, SchemaData>();
        
        for(SchemaData schemaData: schemaDataListAT) {
            String name = schemaData.getName();
            
            String key = name;
            schemaDataInfoMap.put(key, schemaData);
            
            for(SchemaDataField schemaDataField: schemaData.getFields()) {
                String dataFieldName = schemaDataField.getName();
                
                if(dataFieldName.contains("::")) {
                    String[] sa = dataFieldName.split("::");
                    dataFieldName = sa[1];
                }
                
                key = name + " -> " + dataFieldName;
                schemaDataFieldMap.put(key, schemaDataField);
            }
        }
        
        // now copy the relevant values
        for(SchemaData schemaData: savedSchemaDataList) {
            String name = schemaData.getName();
            
            String key = name;
            SchemaData updatedSchemaData = schemaDataInfoMap.get(key);
            
            if(updatedSchemaData != null) {
                updatedSchemaData.setNote(schemaData.getNote());
            
                for(SchemaDataField schemaDataField: schemaData.getFields()) {
                    key = name + " -> " + schemaDataField.getName();
                    SchemaDataField updatedSchemaDataField = schemaDataFieldMap.get(key);
                    
                    if(updatedSchemaDataField != null) {
                        updatedSchemaDataField.setMappedTo(schemaDataField.getMappedTo());
                        updatedSchemaDataField.setNote(schemaDataField.getNote());
                    }
                }
            }
        }
        
        System.out.println("Finished updated Schema Data List ...");
    }
    
    /**
     * Method to generate the mapping document as an xml file
     * @return 
     */
    public String generateMappingDocument() {
        // if schema datalist is emp
        if(schemaDataListAT == null || schemaDataListAT.size() == 0) {
            return "Nothing to process ...";
        }
        
        baseURL = getThreadLocalRequest().getServerName() + ":" +
            getThreadLocalRequest().getServerPort();
        
        // if we running in tomcat under https replace the 8443 with 8083
        baseURL = baseURL.replace("8443", "8083/SchemaMap");
        
        // generate the AT mapping documents now
        try {
            MappingDocumentGenerator mappingDocumentGenerator = new MappingDocumentGenerator();
            String mappingDocumentName = mappingDocumentGenerator.generateATMappingDocument(indexPathAT, schemaDataListAT);
            
            String documentUrl = "http://" + baseURL + "/MappingDocumentPosterServlet";
            
            return documentUrl;
        } catch (Exception ex) {
            Logger.getLogger(SchemaDataServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return "Excpetion occured ...";
        }
    }

}
