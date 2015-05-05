/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.client.model;

import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.LabelProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;
import java.util.ArrayList;

/**
 *
 * @author nathan
 */
public interface SchemaDataProperties extends PropertyAccess<SchemaData> {
    ModelKeyProvider<SchemaData> id();
    
    @Path("name")
    LabelProvider<SchemaData> nameLabel();
    
    ValueProvider<SchemaData, String> name();
    ValueProvider<SchemaData, ArrayList<String>> fields();
}
