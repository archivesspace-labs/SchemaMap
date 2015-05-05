
package org.nyu.edu.dlts.client.model;

import java.io.Serializable;

/**
 * Class that holds information about a particular in a schema
 * @author nathan
 */
public class SchemaDataField implements Serializable {
    private static final String TYPE_VALUE = "Initial Value";
    private int id;
    private String name;
    private String fullName;
    private String type;
    private String mappedTo;
    private String note;
    private boolean required;
    private static int COUNTER = 0;
    
    /**
     * Default constructor which does nothing
     */
    public SchemaDataField() {
        this.id = Integer.valueOf(COUNTER++);
        this.name = "Test Schema Data Field";
        this.type = "Test Type";
        this.note = "none";
        this.mappedTo = "";
        this.required = false;
    }
    
    /**
     * Constructor that is typically used
     * 
     * @param name
     * @param type 
     */
    public SchemaDataField(String name, String type) {
        this.id = Integer.valueOf(COUNTER++);
        this.name = name;
        this.type = type;
        //this.note = "Test Note";
        //this.mappedTo = "Test Map";
    }
    
    /**
     * Constructor when mapping values instead of fields
     * 
     * @param name 
     */
    public SchemaDataField(String name) {
        this.id = Integer.valueOf(COUNTER++);
        this.name = name;
        this.type = TYPE_VALUE;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        String fullName = name + " " + type;
        
        if(fullName.length() > 60) {
            fullName = fullName.substring(0, 60) + " ...";
        } 
        
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getMappedTo() {
        return mappedTo;
    }

    public void setMappedTo(String mappedTo) {
        this.mappedTo = mappedTo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    } 
}
