
package org.nyu.edu.dlts.server;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A simple utility to clean up json records before running unit test on them
 * 
 * @author nathan
 */
public class RecordTestUtil {
    /**
     * Method called to remove certain fields of a json record before unit testing
     * can be done on it
     * 
     * @param json
     * @return
     * @throws Exception 
     */
    public static void cleanRecord(JSONObject json) throws Exception {
        // clean the audit info for the stop level record
        removeUriAndAuditInfo(json);
        
        // TODO, based on the specific record type do any additional cleanup if needed
        String recordType = json.getString("jsonmodel_type");
    }
    
    /**
     * Method to strip out audit info common to all json records using a recursive call
     */
    private static void removeUriAndAuditInfo(JSONObject json) throws Exception {
        json.remove("uri");
        json.remove("create_time");
        json.remove("last_modified");
        json.remove("lock_version");
        
        // next remove audit information for any sub aspace records
        for(Object key: json.keySet()) {
            Object object1 = json.get((String)key);
            
            if(object1 instanceof JSONObject) {
                JSONObject jo = (JSONObject)object1;
                removeUriAndAuditInfo(jo);
            } else if (object1 instanceof JSONArray) {
                JSONArray ja = (JSONArray)object1;
                
                for(int i = 0; i < ja.length(); i++) {
                    Object object2 = ja.get(i);
                    
                    if(object2 instanceof JSONObject) {
                        JSONObject jo = (JSONObject)object2;
                        removeUriAndAuditInfo(jo);
                    }
                }
            }
        }
    }
}
