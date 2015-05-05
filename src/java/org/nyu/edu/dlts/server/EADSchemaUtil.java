/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.server;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;

/**
 *
 * @author nathan
 */
public class EADSchemaUtil {

    private static String schemaPath;

    /**
     * Set the location of the EAD 2002 schema
     *
     * @param path
     */
    public static void setSchemaPath(String path) {
        schemaPath = path;
    }

    /**
     * Method to process the schema and return a list of fields
     *
     * @return
     * @throws Exception
     */
    public static HashMap<String, ArrayList<String>> processSchema() throws Exception {
        HashMap<String, ArrayList<String>> fieldsMap = new HashMap<String, ArrayList<String>>();

        // read the field for EAD 2002
        InputStream is = new FileInputStream(schemaPath);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is));
        
        addSchemaFields("EAD 2002", fieldsMap, schema);
        
        // return the field map now
        return fieldsMap;
    }
    
    /**
     * Method to extract the field names from the schema document
     * 
     * @param fieldsMap
     * @param schema 
     */
    private static void addSchemaFields(String schemaName, HashMap<String, ArrayList<String>> fieldsMap, XmlSchema schema) {
        ArrayList<String> eadFields = new ArrayList<String>();
        Map schemaTypeMap = schema.getSchemaTypes();

        for (Object object : schemaTypeMap.values()) {
            if (object instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType schemaType = (XmlSchemaComplexType) object;
                List<XmlSchemaAttributeOrGroupRef> attributesList = schemaType.getAttributes();
                
                String fieldName = schemaType.getName();
                eadFields.add(fieldName + ", ELEMENT");
                
                if(attributesList != null) {
                    for(XmlSchemaAttributeOrGroupRef attributeOrGroup: attributesList) {
                        if(attributeOrGroup instanceof XmlSchemaAttribute) {
                            XmlSchemaAttribute attribute = (XmlSchemaAttribute)attributeOrGroup;
                            eadFields.add(fieldName + "=>" + attribute.getName() + ", ATTRIBUTE");
                        }
                    }
                }
            }
        }
        
        /*Collections.sort(eadFields);
        
        for(String field: eadFields) {
            System.out.println("Field: " + field);
        }*/
        
        fieldsMap.put(schemaName, eadFields);
    }

    /**
     * Main method for testing without spinning up servlet container
     *
     * @param args
     */
    public static void main(String[] args) {
        // set the index uri and url for ASpace docs
        //String path = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas/AS/index.txt";
        String path = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas/EAD/ead.xsd";

        try {
            setSchemaPath(path);
            processSchema();
        } catch (Exception ex) {
            Logger.getLogger(ASpaceSchemaUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
