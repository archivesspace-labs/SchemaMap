/*
 * Simple class to analyze the data mapping
 */
package org.nyu.edu.dlts.client.widgets;


import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.TextArea;
import java.util.ArrayList;
import java.util.HashMap;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataField;

/**
 *
 * @author nathan
 */
public class DataMapAnalyzerWindow extends Window {
    private SchemaDataInfoPanel infoPanel;
    
    private SchemaData schemaData;
    
    private ArrayList<SchemaData> aspaceSchemaDataList;
    
    private HashMap<String, SchemaDataField> aspaceSchemaDataMap = new HashMap<String, SchemaDataField>();
    
    private TextArea textArea;
    
    private String heading;
    
    private boolean modal = false;
    
    /**
     * Constructor which takes the search results
     */
    public DataMapAnalyzerWindow(SchemaDataInfoPanel infoPanel, 
            SchemaData schemaData, ArrayList<SchemaData> aspaceSchemaDataList) {
        
        this.infoPanel = infoPanel;
        this.schemaData = schemaData;
        this.aspaceSchemaDataList = aspaceSchemaDataList;
        
        heading = "Data Mapping Report -- " + schemaData.getName().toUpperCase();
        
        initSchemaDataMap();
        
        initUI();
        
        analyzeDataMap();
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
        
        textArea = new TextArea();
        textArea.setSize("650", "600");
        
        textArea.setValue("Data Map Report\n\n");
        
        container.add(textArea);
        
        // add button to close this window
        TextButton closeButton = new TextButton("Close", new SelectHandler() {
            public void onSelect(SelectEvent event) {
                hide();
            }
        });
        
        addButton(closeButton);
              
        // add the button bar to the sourth 
        setFocusWidget(closeButton);
    }
    
    /**
     * Put the aspace schema data into a hash map to make find them more efficient
     */
    private void initSchemaDataMap() {
        for(SchemaData schemaData: aspaceSchemaDataList) {
            for(SchemaDataField schemaDataField: schemaData.getFields()) {
                String fullFieldName = schemaData.getName() + "_" + schemaDataField.getName();
                aspaceSchemaDataMap.put(fullFieldName, schemaDataField);
            }
        }
    }

    /**
     * Method to analyze the mapping between the schema fields for 
     * type and data length mismatches
     */
    private void analyzeDataMap() {
        StringBuilder sb = new StringBuilder();
        
        for(SchemaDataField schemaDataField: schemaData.getFields()) {
            String fieldName = schemaDataField.getName();
            String dataType = schemaDataField.getType();
            String mappedTo = schemaDataField.getMappedTo();
            
            // skip analysing dataTypes sets and objects
            if(dataType.contains("SET") || dataType.contains("OBJECT")) {
                continue;
            }
            
            if(mappedTo != null && mappedTo.contains("->")) {
                String[] sa = mappedTo.split("\\s*->\\s*");
                String fullFieldName = null;
                
                if(sa.length == 2) {
                    fullFieldName = sa[0] + "_" + sa[1];
                } else if(sa.length > 2) {
                    fullFieldName = sa[sa.length - 2] + "_" + sa[sa.length - 1];
                }
                
                if(fullFieldName != null) {
                    SchemaDataField aspaceSchemaDataField = aspaceSchemaDataMap.get(fullFieldName);
                    
                    if(aspaceSchemaDataField != null) {
                        String result = compareFields(schemaDataField, aspaceSchemaDataField);
                        sb.append(fieldName).append(": ").append(result).append("\n\n");
                    } else {
                        sb.append(fieldName).append(": ").append("ASpace field not found ...\n\n");
                    }
                }
            } else if(mappedTo != null) {
                sb.append(fieldName).append(": ").append("Unable to Check ...\n\n");
            } else {
                sb.append(fieldName).append(": ").append("Not Mapped ...\n\n");
            }
        }
        
        textArea.setValue(sb.toString());
    }
    
    /**
     * Method to compare two fields
     * 
     * @param schemaDataField
     * @param aspaceSchemaDataField
     * @return 
     */
    private String compareFields(SchemaDataField schemaDataField, SchemaDataField aspaceSchemaDataField) {
        String dataType = schemaDataField.getType().trim();
        String aspaceDataType = aspaceSchemaDataField.getType().trim();
        
        // get the data type length
        String dataTypeToCompare;
        int dtl1 = -1;
        int dtl2 = -1;
        
        
        // see whether to add the length to strings nd get the length
        if(dataType.contains("STRING")) {
            dataTypeToCompare = "STRING";
            
            String[] sa = dataType.split("\\s+");
            if(sa.length == 1) {
                dataType = "STRING 255";
                dtl1 = 255;
            } else {
                dtl1 = Integer.parseInt(sa[1]);
            }        
        } else if(dataType.contains("TEXT")) {
            dataTypeToCompare = "STRING";
            dtl1 = 65000;
        } else {
            dataTypeToCompare = dataType;
        }
        
        if(aspaceDataType.contains("STRING")) {
            String[] sa = aspaceDataType.split("MAX LENGTH:\\s*");
            if(sa.length == 1) {
                aspaceDataType = "STRING (MAX LENGTH: ?)";
            } else {
                dtl2 = Integer.parseInt(sa[1].replace(")", ""));
            }
        }
        
        String analysis = "OK";
        
        // check for type mismatches
        if(!aspaceDataType.contains("ARRAY") && !aspaceDataType.contains(dataTypeToCompare)) {
            analysis = "Data Type Mismatch";
        } else if(dtl2 != -1 && dtl1 > dtl2) {
            analysis = "Length Mismatch";
        }
        
        String result = dataType + " => " + aspaceDataType + " :: " + analysis;
        
        return result;
    }    
}
