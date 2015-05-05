/*
 * Simple class to modify enrollment in a variation course
 */
package org.nyu.edu.dlts.client.widgets;


import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.TextArea;
import java.util.HashMap;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataField;

/**
 *
 * @author nathan
 */
public class SchemaFieldsCopyWindow extends Window {
    public static final String COPY_VIEW = "copy";
    public static final String IMPORT_VIEW = "import";
    
    private SchemaDataInfoPanel infoPanel;
    
    private SchemaData schemaData;
    
    private String view;
    
    private String heading;
    
    private boolean modal = false;
    
    /**
     * Constructor which takes the search results
     */
    public SchemaFieldsCopyWindow(SchemaDataInfoPanel infoPanel, String view, SchemaData schemaData) {
        this.infoPanel = infoPanel;
        this.view = view;
        this.schemaData = schemaData;
        
        if(view.equals(COPY_VIEW)) {
            heading = "Copy Schema Fields -- " + schemaData.getName().toUpperCase();
        } else {
            heading = "Import Schema Fields -- " + schemaData.getName().toUpperCase();
            modal = true;
        }
                
        initUI();
    }
   
    /**
     * Method to create the user interface
     */
    protected void initUI() {
        setModal(modal);
        setHeadingText(heading);
        
        VerticalLayoutContainer container = new VerticalLayoutContainer();
        container.setScrollMode(ScrollSupport.ScrollMode.AUTOY);
        setWidget(container);
        
        final TextArea textArea = new TextArea();
        textArea.setSize("500", "400");
        
        
        // if we are in copy view then display the current text
        if(view.equals(COPY_VIEW)) {
            textArea.setValue(getFieldsAsText());
        } else {
            textArea.setValue("Paste {Tab} delimited data here");
        }
        
        container.add(textArea);
        
        // add the import data button
        TextButton saveButton = new TextButton("Import Mapping Data", new SelectHandler() {
            public void onSelect(SelectEvent event) {
                importData(textArea.getValue());
            }
        });
        
        // add button to close this window
        TextButton closeButton = new TextButton("Close", new SelectHandler() {
            public void onSelect(SelectEvent event) {
                hide();
            }
        });
        
        if(view.equals(IMPORT_VIEW)) {
            addButton(saveButton);
        }
        addButton(closeButton);
              
        // add the button bar to the sourth 
        setFocusWidget(saveButton);
    }
    
    /**
     * Method to return the fields as a tab delimited text
     * 
     * @return 
     */
    private String getFieldsAsText() {
        StringBuilder sb = new StringBuilder();
        
        String schemaName = schemaData.getName();
        for(SchemaDataField field: schemaData.getFields()) {
            sb.append(schemaName);
            sb.append(" -> ");
            sb.append(field.getName());
            sb.append(" \t ");
            
            String mappedTo = field.getMappedTo();
            if(mappedTo != null) {
                sb.append(mappedTo);
            }
            
            sb.append(" \t ");
            
            String note = field.getNote();
            if(note != null) {
                sb.append(note);
            }
            
            sb.append(" \n");
        }
        
        return sb.toString();
    }
    
    /**
     * Method to import data 
     */
    public void importData(String tabData) {
        HashMap<String, String> fieldMapping = new HashMap<String, String>();
        
        String[] lines = tabData.split("\\n");
        
        for(String line: lines) {
            if(line.contains("->")) {
                String[] info = line.split("\\s*\\t\\s*");
                
                String key = info[0];
                
                String mapping = "";
                if(info[1] != null) {
                    mapping = info[1];
                }
                
                String note = "";
                if(info[2] != null) {
                    note = info[2];
                }
                
                String fieldInfo = mapping + "\\t" + note;
                fieldMapping.put(key, fieldInfo);
            }
        }
        
        // now update the schema data object
    }
}
