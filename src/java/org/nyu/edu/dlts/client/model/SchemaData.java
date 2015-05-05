/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.client.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author nathan
 */
public class SchemaData implements Serializable {
    public static final String AT_TYPE = "AT";
    public static final String AT_VALUE = "AT VALUE";
    public static final String EAD_TYPE = "EAD";
    public static final String AS_TYPE = "AS";
    
    private int id;
    private String name = "";
    private ArrayList<SchemaDataField> fields;
    private String note;
    private String type; // used to set either ASpace or AT
    private String url; // URL for schema object. Used by ASpace
    private static int COUNTER = 0;

    // default constructor
    public SchemaData() {
        this.id = Integer.valueOf(COUNTER++);
        this.name = "Test Name";
        this.note = "none";
        this.fields = new ArrayList<SchemaDataField>();
    }

    /**
     * Constructor that is normally used
     *
     * @param name
     * @param fields
     */
    public SchemaData(String name, ArrayList<SchemaDataField> fields) {
        this.id = Integer.valueOf(COUNTER++);
        this.name = name;
        this.note = "none";
        this.fields = fields;
    }
    
    /**
     * Constructor that used when the type of schema needs to be specified
     * 
     * @param name
     * @param type
     * @param fields 
     */
    public SchemaData(String name, String type, ArrayList<SchemaDataField> fields) {
        this.id = Integer.valueOf(COUNTER++);
        this.name = name;
        this.type = type;
        this.note = "";
        this.fields = fields;
    }
    
    public ArrayList<SchemaDataField> getFields() {
        return fields;
    }

    public void setFields(ArrayList<SchemaDataField> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
