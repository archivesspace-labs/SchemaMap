/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.client.model;

import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

/**
 *
 * @author nathan
 */
public interface SchemaDataFieldProperties extends PropertyAccess<SchemaDataField> {
    ModelKeyProvider<SchemaDataField> id();
    
    ValueProvider<SchemaDataField, String> name();
    ValueProvider<SchemaDataField, String> fullName();
    ValueProvider<SchemaDataField, String> type();   
    ValueProvider<SchemaDataField, String> mappedTo();   
    ValueProvider<SchemaDataField, String> note();
    ValueProvider<SchemaDataField, Boolean> required();
}
