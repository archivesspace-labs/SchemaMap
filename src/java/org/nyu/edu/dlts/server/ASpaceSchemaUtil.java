package org.nyu.edu.dlts.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * A simple class for loading AT hibernate schema files and extracting relevant
 * information from the files
 *
 * @author Nathan Stevens
 * @date 10/01/2012
 */
public class ASpaceSchemaUtil {

    private static String indexPath;
    private static String subSchemaPath;
    private static String schemaPath;

    // use to store fields for abstract classes
    static private HashMap<String, ArrayList<String>> abstractFieldsMap = new HashMap<String, ArrayList<String>>();

    /**
     * Method used to set the index URU and also the path
     *
     * @param path1 Path of index file
     * @param path2 path of schema files
     */
    public static void setIndexPath(String path1, String path2) {
        indexPath = path1;
        schemaPath = path2;
    }

    /**
     * Method used to set the sub schema path which points to a file holding sub
     * schemas which are not current exported by the document generating tool
     *
     * @param path
     */
    public static void setSubSchemaPath(String path) {
        subSchemaPath = path;
    }

    /**
     * Method to load a text file containing a list of AS Schema objects
     *
     * @return String containing list of hibernate schema
     */
    public static HashMap<String, ArrayList<String>> processSchemaIndex() throws Exception {
        // stores the field information
        HashMap<String, ArrayList<String>> fieldsMap = new HashMap<String, ArrayList<String>>();

        /* initialize the http client for reading the schema file
         HttpClient httpClient = new DefaultHttpClient();

         HttpGet httpget = new HttpGet(indexURI);
         HttpResponse response = httpClient.execute(httpget);

         HttpEntity entity = response.getEntity();*/
        
        String fileContent = FileUtil.readFileContent(indexPath);

        if (fileContent != null) {

            String[] lines = fileContent.split("\\n");

            for (String line : lines) {
                if (line.isEmpty()) {
                    continue;
                }

                String schemaName = line.replace(".rb", "");
                System.out.println("Schema Name: " + schemaName);

                // get the data fields from the schema files on the server
                String schemaFileURI = schemaPath + "/" + line;

                HashMap<String, ArrayList<String>> currentSchemaFieldsMap = getDataFields(schemaName, schemaFileURI);

                if (currentSchemaFieldsMap != null) {
                    fieldsMap.putAll(currentSchemaFieldsMap);
                }
            }
        }

        // now read in any sub schemas
        addSubSchemaDataField(fieldsMap);

        return fieldsMap;
    }

    /**
     * Method to get the data fields by reading content from a file
     *
     * @param schemaName
     * @param schemaFileURI
     * @return
     */
    public static HashMap<String, ArrayList<String>> getDataFields(String schemaName, String schemaFileURI) throws Exception {
        // hashmap that hould the information
        HashMap<String, ArrayList<String>> fieldsMap = new HashMap<String, ArrayList<String>>();

        // boolean that specifies that this is an abstract schema
        boolean isAbstract = false;

        // if this schema is an obstract schemma the store it in the abstract field list
        if (schemaName.contains("abstract_")) {
            fieldsMap = abstractFieldsMap;
            isAbstract = true;
        }

        ArrayList<String> fieldMetaData = parseSchemaFile(schemaFileURI);

        if (fieldMetaData != null && !fieldMetaData.isEmpty()) { // make we have actual data
            ArrayList<String> fieldsList = new ArrayList<String>();

            for (String fieldInfo : fieldMetaData) {
                String[] sa = fieldInfo.split("\\t");
                String propertyName = sa[1];
                String propertyType = sa[0];
                String propertyRequired = sa[2];

                if (!propertyName.equals("uri")) {
                    String propertyInfo = propertyName + "\t" + propertyType.toUpperCase() + "\t" + propertyRequired;
                    fieldsList.add(propertyInfo);
                    System.out.println("Property Info: " + propertyInfo);
                }
            }

            fieldsMap.put(schemaName, fieldsList);

            // if we are not an abstract schema, see if there is an abstract schema to add the fields
            if (!isAbstract) {
                // check to see if it as an abstract schema
                String[] sa = schemaName.split("_");
                String abstractSchemaName = "abstract_" + sa[0];

                // some schemas and abstract names don't match 
                // so manually correct them
                if (schemaName.equalsIgnoreCase("resource")
                        || schemaName.equalsIgnoreCase("archival_object")
                        || schemaName.equalsIgnoreCase("digital_object")
                        || schemaName.equalsIgnoreCase("digital_object_component")) {

                    abstractSchemaName = "abstract_archival_object";
                }

                ArrayList<String> alist = abstractFieldsMap.get(abstractSchemaName);

                if (alist != null && !schemaName.equals("agent_contact")) {
                    fieldsList.addAll(alist);
                    System.out.println("Added abstract fields to " + schemaName);
                }
            } else {
                return null; // we don't want to add abstract schemas
            }
        } else {
            String message = "\nError reading schema file: " + schemaFileURI + "\n";
            Logger.getLogger(ASpaceSchemaUtil.class.getName()).log(Level.SEVERE, message);
            return null;
        }

        return fieldsMap;
    }

    /**
     * Method to add fields from sub schemas
     *
     * @param fieldsMap
     */
    private static void addSubSchemaDataField(HashMap<String, ArrayList<String>> fieldsMap) throws Exception {
        String fileContent = FileUtil.readFileContent(subSchemaPath);

        if (fileContent != null) {
            String[] lines = fileContent.split("\\n");
            ArrayList<String> fieldsList = null;
            String schemaName = "Unknown";
            String[] sa;

            for (String line : lines) {
                if (line.isEmpty()) {
                    continue;
                }

                if (line.contains("SubSchema:")) {
                    sa = line.split("\\t");
                    schemaName = sa[1];
                    fieldsList = new ArrayList<String>();
                } else if (line.contains("END")) {
                    fieldsMap.put(schemaName, fieldsList);
                } else {
                    sa = line.split("\\t");

                    String propertyName = sa[1];
                    String propertyType = sa[0];

                    String propertyInfo = propertyName + ", " + propertyType.toUpperCase();
                    fieldsList.add(propertyInfo);
                    System.out.println("Sub Schema Property Info: " + propertyInfo);
                }
            }
        }
    }

    /**
     * Method to get the data fields by reading content from the schema file. The format
     * is based on the legacy format from http://archivesspace.github.io/archivesspace/doc/
     *
     * @param schemaFileURI
     * @return
     */
    public static ArrayList<String> parseSchemaFile(String schemaFileURI) throws Exception {
        ArrayList<String> propertyList = new ArrayList<String>();

        /*HttpGet httpget = new HttpGet(schemaFileURI);
         HttpResponse response = httpClient.execute(httpget);
        
         HttpEntity entity = response.getEntity();
         String fileContent = EntityUtils.toString(entity);*/
        try {
            String fileContent = FileUtil.readFileContent(schemaFileURI);

            // first skip any comments
            int startIndex = fileContent.indexOf("{");
            int endIndex = fileContent.lastIndexOf("}");
            fileContent = fileContent.substring(startIndex, endIndex + 1);

            // do some replacement to turn the rubyrized schema into a valid JSON object
            fileContent = fileContent.replaceAll("=>", " : ");
            fileContent = fileContent.replaceAll(":schema", "\"schema\"");

            JSONObject schemaJS = new JSONObject(fileContent).getJSONObject("schema");
            JSONObject propertiesJS = schemaJS.getJSONObject("properties");

            for (Object key : propertiesJS.keySet()) {
                String propertyName = key.toString();

                // skip any properties which we don't want
                if (propertyName.equals("tree") || propertyName.equals("_resolved")) {
                    continue;
                }

                JSONObject propertyJS = propertiesJS.getJSONObject(propertyName);

                String propertyType = propertyJS.get("type").toString();
                String propertyInfo = "";
                String propertyRequired = "FALSE";

                // depending on the type field find addition information if needed
                if (propertyType.equals("string")) {
                    if (propertyJS.has("maxLength")) {
                        propertyInfo = " (max length: " + propertyJS.get("maxLength") + ")";
                    } else if(propertyJS.has("dynamic_enum")) {
                        propertyInfo = " (enum: " + propertyJS.get("dynamic_enum") + ")";
                    }
                } else if (propertyType.equals("array")) {
                    propertyInfo = getArrayInfo(propertyJS.getJSONObject("items"));
                }
                
                if (isRequired(propertyJS)) {
                    propertyRequired = "TRUE";
                }
                
                String fullPropertyInfo = propertyType + propertyInfo + "\t" + propertyName + "\t" + propertyRequired;
                propertyList.add(fullPropertyInfo);
            }
        } catch (Exception e) {
            String message = "\nError reading schema file: " + schemaFileURI + "\n";
            Logger.getLogger(ASpaceSchemaUtil.class.getName()).log(Level.SEVERE, message);
            return null;
            //e.printStackTrace();
            //System.exit(-1);
        }

        return propertyList;
    }

    /**
     * Method to check to see if a field is required
     *
     * @param fieldJS
     * @return
     */
    private static boolean isRequired(JSONObject fieldJS) {
        if (fieldJS.has("required")) {
            return fieldJS.getBoolean("required");
        } else if (fieldJS.has("ifmissing")) {
            return fieldJS.getString("ifmissing").equals("error");
        } else {
            return false;
        }
    }

    /**
     * Method to extract information about the items that go into the array
     *
     * @param itemsJS
     * @return
     */
    private static String getArrayInfo(JSONObject itemsJS) {
        String fieldInfo = "";
        Object type = itemsJS.get("type");

        if (type instanceof String) {
            fieldInfo = " (Linked Objects)";
        } else {
            fieldInfo = " (" + type.toString().replace("\"type\":", "") + ")";
        }

        return fieldInfo;
    }

    /**
     * Main method for testing without spinning up servlet container
     *
     * @param args
     */
    public static void main(String[] args) {
        // set the index uri and url for ASpace docs
        //String path = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas/AS/index.txt";
        String path = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas/AS/index.txt";
        String path2 = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas/AS/v1.2.0";
        setIndexPath(path, path2);

        path = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas/AS/sub_schemas.txt";
        setSubSchemaPath(path);

        try {
            processSchemaIndex();
        } catch (Exception ex) {
            Logger.getLogger(ASpaceSchemaUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
