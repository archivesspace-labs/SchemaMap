/*
 * Simple class to select ASpace schema fields
 */
package org.nyu.edu.dlts.client.widgets;


import com.google.gwt.core.client.GWT;
import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataField;
import org.nyu.edu.dlts.client.model.SchemaDataFieldProperties;

/**
 *
 * @author nathan
 */
public class SchemaFieldsWindow extends Window {
    private SchemaDataInfoPanel infoPanel;
    
    private SchemaData schemaData;
    
    private ListView<SchemaDataField, String> fieldListView;
    
    /**
     * Constructor which takes the search results
     */
    public SchemaFieldsWindow(SchemaDataInfoPanel infoPanel, SchemaData schemaData) {
        this.infoPanel = infoPanel;
        this.schemaData = schemaData;
        initUI();
    }
   
    /**
     * Method to create the user interface
     */
    protected void initUI() {
        setModal(false);
        setHeadingText("Data Fields -- " + schemaData.getName().toUpperCase());
        
        VerticalLayoutContainer container = new VerticalLayoutContainer();
        container.setScrollMode(ScrollSupport.ScrollMode.AUTOY);
        
        setWidget(container);
        
        // add the list store which holds the data fields name
        SchemaDataFieldProperties dp = GWT.create(SchemaDataFieldProperties.class);
        
        ListStore<SchemaDataField> store = new ListStore<SchemaDataField>(dp.id());
        store.addSortInfo(new StoreSortInfo<SchemaDataField>(dp.fullName(), SortDir.ASC));
        store.addAll(schemaData.getFields());
        
        // add the listview, which displays the field names
        fieldListView = new ListView<SchemaDataField, String>(store, dp.fullName());
        
        container.add(fieldListView);
        
        // depending on the size of the store set the height
        if(store.size() > 14) {
            setHeight(400);
        }
        
        // add the add course button
        TextButton saveButton = new TextButton("Copy Selected Field", new SelectHandler() {
            public void onSelect(SelectEvent event) {
                SchemaDataField field = fieldListView.getSelectionModel().getSelectedItem();
                addDataFieldName(field.getName());
            }
        });
        
        // add button to close this window
        TextButton closeButton = new TextButton("Close", new SelectHandler() {
            public void onSelect(SelectEvent event) {
                hide();
            }
        });
        
        addButton(saveButton);
        addButton(closeButton);
              
        // add the button bar to the sourth 
        setFocusWidget(saveButton);
    }
    
    /**
     * Method to update the data field name on the panel
     * 
     * @param courseName 
     */
    private void addDataFieldName(String fieldName) {
        infoPanel.updateMapToTextField(schemaData.getName(), fieldName);
    }
}
