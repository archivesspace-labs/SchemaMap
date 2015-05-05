package org.nyu.edu.dlts.server;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A simple class for loading AT hibernate schema files and extracting
 * relevant information from the files
 * 
 * @author Nathan Stevens
 * @date 10/01/2012
 */
public class ATSchemaUtil {

    private static String indexPath;
    private static String path;
    private static String lookListValuesPath;
    private static String noteEtcTypeValuesPath;
    private static String inLineTagsValuesPath;

    /**
     * Method used to set the index URU and also the path
     * 
     * @param indexURI 
     */
    public static String setIndexPath(String filepath) {
        String replaceString = File.separator + "index.txt";
        
        indexPath = filepath;
        path = indexPath.replace(replaceString, "");
        
        lookListValuesPath = path + File.separator + "values" + File.separator + "lookupListValues.xml";
        noteEtcTypeValuesPath = path + File.separator + "values" + File.separator + "NoteEtcTypes.xml";
        inLineTagsValuesPath = path + File.separator + "values" + File.separator + "inLineTags.xml";
        
        // return the path so we can use it to save the documents
        return path + File.separator;
    }

    /**
     * Method to load a text file containing a list of AT Schema objects
     * 
     * @return String containing list of hibernate schema 
     */
    public static HashMap<String, ArrayList<String>> processSchemaIndex() throws Exception {
        // stores the field information
        HashMap<String, ArrayList<String>> fieldsMap = new HashMap<String, ArrayList<String>>();

        String fileContent = FileUtil.readFileContent(indexPath);

        if (fileContent != null) {

            String[] lines = fileContent.split("\\n");

            for (String line : lines) {
                if (line.isEmpty()) {
                    continue;
                }

                String schemaName = line.replace(".hbm.xml", "");
                System.out.println("Schema Name: " + schemaName);

                // get the data fields from the schema files on the server
                String schemaFileURI = path + File.separator + line;

                HashMap<String, ArrayList<String>> currentSchemaFieldsMap = getDataFields(schemaFileURI);

                if (currentSchemaFieldsMap != null) {
                    fieldsMap.putAll(currentSchemaFieldsMap);
                }
            }
        }

        return fieldsMap;
    }

    /**
     * Method to get the data fields by reading content from a file 
     * 
     * @param httpClient
     * @param schemaFileName
     * @return 
     */
    public static HashMap<String, ArrayList<String>> getDataFields(String schemaFileURI) throws Exception {
        // hashmap that hould the information
        HashMap<String, ArrayList<String>> fieldsMap = new HashMap<String, ArrayList<String>>();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(schemaFileURI);
        doc.getDocumentElement().normalize();

        //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

        // get the class 
        NodeList classList = doc.getElementsByTagName("class");

        for (int i = 0; i < classList.getLength(); i++) {
            Element classElement = (Element) classList.item(i);

            String className = removePackageName(classElement.getAttribute("name"));
            
            System.out.println("Class Name is: " + className);

            // array list to hold the field informaion
            ArrayList<String> fieldsList = new ArrayList<String>();

            // get the properties, which are the fields now
            NodeList propertyList = classElement.getElementsByTagName("property");

            for (int j = 0; j < propertyList.getLength(); j++) {
                Element propertyElement = (Element) propertyList.item(j);
                
                Node parentNode = propertyElement.getParentNode();
                String parentName = parentNode.getNodeName();
                
                String propertyName = propertyElement.getAttribute("name");
                String propertyType = propertyElement.getAttribute("type");
                String propertyLength = propertyElement.getAttribute("length");
                
                // if we are a subclass then prepend the subclass name to the propertyName
                if (parentName.contains("subclass")) {
                    NamedNodeMap attributeNodes = parentNode.getAttributes();
                    String subclassName = attributeNodes.getNamedItem("name").getNodeValue();
                    
                    propertyName = removePackageName(subclassName)  + "::" + propertyName;
                }
                
                if (!propertyName.equals("auditInfo")) {
                    String propertyInfo = propertyName + ", " + propertyType.toUpperCase() + " " + propertyLength;
                    fieldsList.add(propertyInfo);
                    System.out.println("Property Info: " + propertyInfo);
                }
                
            }

            // Get the Sets of data now
            NodeList setList = classElement.getElementsByTagName("set");

            for (int j = 0; j < setList.getLength(); j++) {
                Element setElement = (Element) setList.item(j);
                
                Node parentNode = setElement.getParentNode();
                String parentName = parentNode.getNodeName();
                
                String setName = setElement.getAttribute("name");
                String type = "SET";
                
                // if we are a subclass then prepend the subclass name to the propertyName
                if (parentName.contains("subclass")) {
                    NamedNodeMap attributeNodes = parentNode.getAttributes();
                    String subclassName = attributeNodes.getNamedItem("name").getNodeValue();
                    
                    setName = removePackageName(subclassName)  + "::" + setName;
                }

                String setInfo = upperCaseFieldName(setName) + ", " + type;
                fieldsList.add(setInfo);
                System.out.println("Set Info: " + setInfo);
            }

            // Get any many to one, such as the repository
            NodeList manyToOneList = classElement.getElementsByTagName("many-to-one");

            for (int j = 0; j < manyToOneList.getLength(); j++) {
                Element mtoElement = (Element) manyToOneList.item(j);
                
                Node parentNode = mtoElement.getParentNode();
                String parentName = parentNode.getNodeName();

                String mtoName = mtoElement.getAttribute("name");
                String type = "OBJECT";
                
                // if we are a subclass then prepend the subclass name to the propertyName
                if (parentName.contains("subclass")) {
                    NamedNodeMap attributeNodes = parentNode.getAttributes();
                    String subclassName = attributeNodes.getNamedItem("name").getNodeValue();
                    
                    mtoName = removePackageName(subclassName)  + "::" + mtoName;
                }

                String mtoInfo = upperCaseFieldName(mtoName) + ", " + type;
                fieldsList.add(mtoInfo);
                System.out.println("Many To One Info: " + mtoInfo);
            }
            
            // add the properties to the main field list
            fieldsMap.put(fixName(className), fieldsList);
        }

        return fieldsMap;
    }
    
    /**
     * Method to return the relevant data values from the lookup list, and notes etc types
     * XMl files
     * 
     * @return HashMap containing data values keyed by classname->fieldName
     * @throws Exception 
     */
    public static HashMap<String, SchemaData> getDataValues() throws Exception {
        // hashmap that hould the information
        HashMap<String, SchemaData> dataValueMap = new HashMap<String, SchemaData>();
        
        // add lookup list data values
        addLookupListValues(dataValueMap);
        
        // add notes etc type values
        addNotesEtcValues(dataValueMap);
        
        // add the wrap in tag values
        addInLineTagValues(dataValueMap);
        
        return dataValueMap;
    }
    
    /**
     * Method to get the initial data values for the lookup list
     */
    private static void addLookupListValues(HashMap<String, SchemaData> dataValueMap) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(lookListValuesPath);
        doc.getDocumentElement().normalize();

        // get the lookup list items 
        NodeList lookupList = doc.getElementsByTagName("list");

        for (int i = 0; i < lookupList.getLength(); i++) {
            Element lookupElement = (Element) lookupList.item(i);

            String listName = lookupElement.getAttribute("name");

            System.out.println("\nList Name is: " + listName);

            // array list to hold the value store in a field object informaion
            ArrayList<SchemaDataField> fieldsList = new ArrayList<SchemaDataField>();
            
            // see if we dealing with data pairs
            NodeList pairList = lookupElement.getElementsByTagName("pair");
            
            // check to see if we have any data pairs. If we don't just use the
            // values
            if(pairList.getLength() == 0) {
                // get the properties, which are the fields now
                NodeList valuesList = lookupElement.getElementsByTagName("value");

                for (int j = 0; j < valuesList.getLength(); j++) {
                    Element valueElement = (Element) valuesList.item(j);

                    String valueName = valueElement.getTextContent();

                    SchemaDataField schemaDataField = new SchemaDataField(valueName);
                    fieldsList.add(schemaDataField);

                    System.out.println("Value: " + valueName);
                }
            } else {
                for (int j = 0; j < pairList.getLength(); j++) {
                    String dataName = "";
                    
                    Element pairElement = (Element) pairList.item(j);
                    
                    // get the value
                    Element valueElement = (Element) pairElement.getElementsByTagName("value").item(0);
                    String valueName = valueElement.getTextContent();
                    
                    // get the code
                    Element codeElement = (Element) pairElement.getElementsByTagName("code").item(0);
                    if(codeElement != null) {
                        dataName  = valueName + " [" + codeElement.getTextContent() + "]";
                    } else {
                        dataName = valueName;
                    }

                    SchemaDataField schemaDataField = new SchemaDataField(dataName);
                    fieldsList.add(schemaDataField);

                    System.out.println("Data Name: " + dataName);
                }
            }

            // get the class that use this list and add them to the hashmap
            NodeList usageList = lookupElement.getElementsByTagName("usage");
            String key = "";
            
            for (int j = 0; j < usageList.getLength(); j++) {
                Element usageElement = (Element) usageList.item(j);

                String className = usageElement.getAttribute("className");
                className = className.replace("org.archiviststoolkit.model.", "");
                
                String fieldName = usageElement.getAttribute("field");
                
                if(key.isEmpty()) {
                    key = className.trim() + "->" + fieldName.trim();
                } else {
                    key += "/" + className.trim() + "->" + fieldName.trim();
                }
            }
            
            // set the key to list name if it's empty
            if(key.isEmpty()) {
                key = listName;
            }
            
            SchemaData schemaData = new SchemaData(key, SchemaData.AT_VALUE, fieldsList);
            
            dataValueMap.put(key, schemaData);    
        }
    }
    
    /**
     * Method to get the initial data values for the notes ETC type
     * 
     */
    private static void addNotesEtcValues(HashMap<String, SchemaData> dataValueMap) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(noteEtcTypeValuesPath);
        doc.getDocumentElement().normalize();
        
        // get the lookup list of note type 
        NodeList noteList = doc.getElementsByTagName("note");
        
        // array list to hold the value store in a field object informaion
        ArrayList<SchemaDataField> fieldsList = new ArrayList<SchemaDataField>();
        
        for (int i = 0; i < noteList.getLength(); i++) {
            Element noteElement = (Element) noteList.item(i);
            
            // get the properties, which are the fields now
            Element nameElement = (Element)noteElement.getElementsByTagName("name").item(0);
            
            String noteName = nameElement.getTextContent();
            
            SchemaDataField schemaDataField = new SchemaDataField(noteName);
            fieldsList.add(schemaDataField);
            
            System.out.println("Note name: " + noteName);
        }
        
        // add the list to the values hashmap
        String key = "ArchDescriptionRepeatingData->NotesEtcType";
        SchemaData schemaData = new SchemaData(key, SchemaData.AT_VALUE, fieldsList);
        
        dataValueMap.put(key, schemaData);
    }
    
    /**
     * Method to add the initial in line tags
     * 
     * @param dataValueMap
     * @throws Exception 
     */
    private static void addInLineTagValues(HashMap<String, SchemaData> dataValueMap) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        Document doc = dBuilder.parse(inLineTagsValuesPath);
        doc.getDocumentElement().normalize();
        
        // get the lookup list of note type 
        NodeList tagList = doc.getElementsByTagName("tag");
        
        // array list to hold the value store in a field object informaion
        ArrayList<SchemaDataField> fieldsList = new ArrayList<SchemaDataField>();
        
        for (int i = 0; i < tagList.getLength(); i++) {
            Element tagElement = (Element) tagList.item(i);
            
            // get the properties, which are the fields now
            Element nameElement = (Element)tagElement.getElementsByTagName("name").item(0);
            
            String tagName = "<" + nameElement.getTextContent() + "/>";
            
            SchemaDataField schemaDataField = new SchemaDataField(tagName);
            fieldsList.add(schemaDataField);
            
            System.out.println("Tag name: " + tagName);
        }
        
        // add the list to the values hashmap
        String key = "Wrap In Tags";
        SchemaData schemaData = new SchemaData(key, SchemaData.AT_VALUE, fieldsList);
        
        dataValueMap.put(key, schemaData);
    }

    /**
     * Method to return a more meaningful class or field name
     * 
     * @param name 
     */
    private static String fixName(String name) {
        if (name.equals("BasicNames")) {
            return "Names";
        }

        return name;
    }

    /**
     * Method to upper case the first letter in the field names
     * 
     * @param fieldName
     * @return 
     */
    private static String upperCaseFieldName(String fieldName) {
        final StringBuilder result = new StringBuilder(fieldName.length());

        result.append(Character.toUpperCase(fieldName.charAt(0))).append(fieldName.substring(1));

        return result.toString();
    }
    
    /**
     * Method to remove the package name from 
     * @param fullName
     * @return 
     */
    private static String removePackageName(String fullName) {
        int loc = fullName.lastIndexOf(".");
        
        if(loc != -1) {
            String shortName = fullName.substring(loc + 1);
            return shortName;
        } else {
            return fullName;
        }
    }

    /**
     * Main method for testing without spinning up servlet container
     * 
     * @param args 
     */
    public static void main(String[] args) {
        String ip = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas/AT/index.txt";
        String sd = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas";
        
        FileUtil.saveDirectory = sd;
        FileUtil.versionDirectory = sd + "/versions";

        ATSchemaUtil.setIndexPath(ip);

        try {
            //ArrayList<SchemaData> schemaDataMapAT = FileUtil.getSchemaDataList(SchemaData.AT_TYPE);
            
            ArrayList<SchemaData> schemaDataMapAT = new ArrayList<SchemaData>();
            HashMap<String, ArrayList<String>> fieldsMap = ATSchemaUtil.processSchemaIndex();

            // process all the the entries
            for (String schemaName : fieldsMap.keySet()) {
                ArrayList<String> fieldInfo = fieldsMap.get(schemaName);
                Collections.sort(fieldInfo);

                ArrayList<SchemaDataField> schemaDataFields = new ArrayList<SchemaDataField>();

                for (String info : fieldInfo) {
                    System.out.println("Info String >> " + schemaName + " :: " + info);
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
                schemaDataMapAT.add(schemaData);
            }
            
            /*HashMap<String, SchemaData> dataValuesMap = ATSchemaUtils.getDataValues();
            
            DatabaseUtil.setupTestDatabaseInfo();
            DatabaseUtil.getConnection();
            DatabaseUtil.saveDataValues("testId", SchemaData.AT_VALUE, dataValuesMap);
            
            // read back this data
            HashMap<String, SchemaData> dataValuesMap2 = DatabaseUtil.getDataValues(SchemaData.AT_VALUE);
            
            System.out.println("Length of data types: " + dataValuesMap2.size());
            
            for (String key: dataValuesMap2.keySet()) {
                SchemaData schemaData = dataValuesMap2.get(key);
                ArrayList<SchemaDataField> fieldList = schemaData.getFields(); 
                
                System.out.println("\nThe field name: " + key + " values: " + fieldList.size());
            }*/
            
            // try saving it
            //FileUtil.saveSchemaDataList(schemaDataMapAT);

        } catch (Exception ex) {
            Logger.getLogger(ATSchemaUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
