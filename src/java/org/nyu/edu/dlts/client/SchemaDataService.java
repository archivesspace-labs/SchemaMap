/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.ArrayList;
import java.util.HashMap;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataField;

/**
 *
 * @author nathan
 */
@RemoteServiceRelativePath("schemadataservice")
public interface SchemaDataService extends RemoteService {
    
    public ArrayList<SchemaData> getSchemaDataAT();
    
    public ArrayList<SchemaData> getSchemaDataEAD();
    
    public ArrayList<SchemaData> getSchemaDataAS();
    
    public HashMap<String, SchemaData> getDataValues(String type);
    
    public String generateMappingDocument();
    
    public String updateSchemaData(String username, SchemaData schemaData);
    
    public String authorize(String username, String password);
}
