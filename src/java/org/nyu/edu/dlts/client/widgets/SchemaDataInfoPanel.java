/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.box.AlertMessageBox;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.RowNumberer;
import com.sencha.gxt.widget.core.client.info.Info;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.nyu.edu.dlts.client.MainEntryPoint;
import org.nyu.edu.dlts.client.SchemaDataServiceAsync;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataField;
import org.nyu.edu.dlts.client.model.SchemaDataFieldProperties;
import org.nyu.edu.dlts.client.model.SchemaDataProperties;

/**
 *
 * @author nathan
 */
public class SchemaDataInfoPanel implements IsWidget {

    private final String REST_API_URL = "http://archivesspace.github.io/archivesspace/doc/file.API.html";
    private SchemaDataFieldProperties props = GWT.create(SchemaDataFieldProperties.class);
    private SchemaData schemaData;
    private ArrayList<SchemaData> aspaceSchemaDataList;
    private Grid<SchemaDataField> grid;
    private TextField mapToTextField;
    private TextField noteTextField;

    /**
     * Main constructor
     *
     * @param schemaData
     */
    public SchemaDataInfoPanel(ArrayList<SchemaData> aspaceSchemaDataList, SchemaData schemaData) {
        this.aspaceSchemaDataList = aspaceSchemaDataList;
        this.schemaData = schemaData;
    }

    /**
     * Method that is called automatically
     *
     * @return
     */
    public Widget asWidget() {
        VerticalLayoutContainer container = new VerticalLayoutContainer();

        IdentityValueProvider<SchemaDataField> identity = new IdentityValueProvider<SchemaDataField>();

        RowNumberer<SchemaDataField> numberer = new RowNumberer<SchemaDataField>(identity);
        
        // initialize the column names
        String nameColTitle = "Field Name";
        String typeColTitle = "Type and Length";
        String mappedToColTitle = "Mapped To";
        String noteColTitle = "Mapping Note";
        
        String heading = schemaData.getName() + " Schema Fields";
        
        // if schema data is for values then rename the column names
        if(schemaData.getType().equals(SchemaData.AT_VALUE)) {
            nameColTitle = "Data Value";
            typeColTitle = "Data Type";
            heading = schemaData.getName();
        } else if(schemaData.getType().equals(SchemaData.AS_TYPE)) {
            mappedToColTitle = "Required?";
            noteColTitle = "Validation Note";
        }
        
        ColumnConfig<SchemaDataField, String> nameCol = new ColumnConfig<SchemaDataField, String>(props.name(), 150, nameColTitle);
        ColumnConfig<SchemaDataField, String> typeCol = new ColumnConfig<SchemaDataField, String>(props.type(), 150, typeColTitle);
        ColumnConfig<SchemaDataField, String> mappedToCol = new ColumnConfig<SchemaDataField, String>(props.mappedTo(), 250, mappedToColTitle);
        ColumnConfig<SchemaDataField, String> noteCol = new ColumnConfig<SchemaDataField, String>(props.note(), 400, noteColTitle);

        List<ColumnConfig<SchemaDataField, ?>> list = new ArrayList<ColumnConfig<SchemaDataField, ?>>();
        list.add(numberer);
        list.add(nameCol);
        list.add(typeCol);
        list.add(mappedToCol);
        list.add(noteCol);

        ColumnModel<SchemaDataField> cm = new ColumnModel<SchemaDataField>(list);

        ListStore<SchemaDataField> store = new ListStore<SchemaDataField>(props.id());
        store.addAll(schemaData.getFields());

        grid = new Grid<SchemaDataField>(store, cm);
        grid.setBorders(true);
        grid.focus();
        grid.getView().setAutoExpandColumn(nameCol);
        grid.getView().setStripeRows(true);
        grid.getView().setColumnLines(true);

        // add selection handeler to grid to update the correct text fields
        grid.getSelectionModel().addSelectionHandler(new SelectionHandler<SchemaDataField>() {

            @Override
            public void onSelection(SelectionEvent<SchemaDataField> event) {
                SchemaDataField selectedField = event.getSelectedItem();
                
                mapToTextField.setValue(selectedField.getMappedTo());
                noteTextField.setValue(selectedField.getNote());
            }
        });

        numberer.initPlugin(grid);

        FramedPanel fp = new FramedPanel();
        fp.setHeadingText(heading);
        fp.setPixelSize(980, 350);
        fp.addStyleName("margin-10");
        fp.setWidget(grid);

        // if data type is ASPace add buttons for going to schema site
        if (schemaData.getType().equals(SchemaData.AS_TYPE)) {
            // add the button to view the ASpace schema code
            TextButton viewSchemaButton = new TextButton("View Schema Code", new SelectHandler() {

                public void onSelect(SelectEvent event) {
                    displaySchemaCode();
                }
            });

            fp.addButton(viewSchemaButton);

            // add button to view the API documents
            TextButton viewAPIButton = new TextButton("View REST API", new SelectHandler() {

                public void onSelect(SelectEvent event) {
                    displayRestAPI();
                }
            });

            fp.addButton(viewAPIButton);
        }


        // add the grid to the container
        container.add(fp, new VerticalLayoutData(-1, -1, new Margins(4)));

        // TO-DO if the person is logged in then display this
        if (MainEntryPoint.loggedIn && (schemaData.getType().equals(SchemaData.AT_TYPE)
                || schemaData.getType().equals(SchemaData.AT_VALUE) 
                || schemaData.getType().equals(SchemaData.EAD_TYPE))) {
            container.add(getEditPanel(), new VerticalLayoutData(-1, -1, new Margins(4)));
        }

        return container;
    }

    /**
     * Return the panel that allows editing mapping information
     *
     * @return
     */
    private ContentPanel getEditPanel() {
        String heading = "Edit Mapping and Schema Information";
        
        // used to specify if certain gui components shown depending on schema data type
        boolean show = true; 
        
        if(schemaData.getType().equals(SchemaData.AT_VALUE)) {
            heading = "Edit Mapping Information";
            show = false;
        }
        
        // add the buttons that allow for entering the mapping information
        FramedPanel fp = new FramedPanel();
        fp.setHeadingText(heading);
        fp.setPixelSize(980, 200);
        fp.addStyleName("margin-10");

        VerticalLayoutContainer vlc = new VerticalLayoutContainer();
        fp.add(vlc);

        SchemaDataProperties props = GWT.create(SchemaDataProperties.class);
        ListStore<SchemaData> store = new ListStore<SchemaData>(props.id());
        store.addSortInfo(new StoreSortInfo<SchemaData>(props.name(), SortDir.ASC));

        store.addAll(aspaceSchemaDataList);

        final ComboBox<SchemaData> combo = new ComboBox<SchemaData>(store, props.nameLabel());
        combo.setForceSelection(true);
        combo.setTriggerAction(TriggerAction.ALL);
        
        if(show) {
            vlc.add(new FieldLabel(combo, "ASpace Schema"), new VerticalLayoutData(1, -1));
        }

        mapToTextField = new TextField();
        vlc.add(new FieldLabel(mapToTextField, "Mapped To"), new VerticalLayoutData(1, -1));

        noteTextField = new TextField();
        vlc.add(new FieldLabel(noteTextField, "Mapping Note"), new VerticalLayoutData(1, -1));

        final TextArea noteTextArea = new TextArea();
        noteTextArea.setValue(schemaData.getNote());
        vlc.add(new FieldLabel(noteTextArea, "Schema Note"), new VerticalLayoutData(1, 100));

        // add the button to update it now
        TextButton viewFieldButton = new TextButton("View Selected ASpace Schema Fields", new SelectHandler() {

            public void onSelect(SelectEvent event) {
                SchemaData sdata = combo.getValue();

                if (sdata != null) {
                    displaySchemaFieldsWindow(sdata);
                }
            }
        });

        if(show) {
            fp.addButton(viewFieldButton);
        }
        
        // add the button to copy an aspace field
        TextButton copyFieldButton = new TextButton("Copy Fields Mapping", new SelectHandler() {

            public void onSelect(SelectEvent event) {
                displayCopySchemaFieldsWindow();
            }
        });

        fp.addButton(copyFieldButton);

        /*// add the button to import the field mappings
        TextButton importFieldButton = new TextButton("Import Fields Mapping", new SelectHandler() {

            public void onSelect(SelectEvent event) {
                displayImportSchemaFieldsWindow();
            }
        });

        fp.addButton(importFieldButton);*/
        
        // add a button to analyze the mapping for string length mismatches
        TextButton analyzeMappingButton = new TextButton("Analyze Mapping", new SelectHandler() {

            public void onSelect(SelectEvent event) {
                displayDataMapAnalyzerWindow();
            }
        });

        fp.addButton(analyzeMappingButton);

        // add button to update the field info to the schema field object
        SelectHandler sh = new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                SchemaDataField field = grid.getSelectionModel().getSelectedItem();

                if (field != null) {
                    SchemaData selectedSchema = combo.getValue();
                    String fieldName = mapToTextField.getCurrentValue();
                    
                    // if the combo box is not empty then use info from that to update
                    if (selectedSchema != null) {
                        String schemaName = combo.getValue().getName();

                        if (fieldName == null) {
                            fieldName = "{see schema}";
                        }

                        // make sure not to duplicate the schema name
                        if(fieldName.contains(schemaName + " -> ")) {
                            field.setMappedTo(fieldName);
                        } else {
                            field.setMappedTo(schemaName + " -> " + fieldName);
                        }
                    } else {
                        field.setMappedTo(fieldName);
                    }

                    // update the note
                    String note = noteTextField.getCurrentValue();
                    field.setNote(note);
                    
                    // refresh the grid view now
                    int index = grid.getStore().indexOf(field);
                    grid.getView().refresh(false);
                    grid.getView().focusRow(index);
                }
                
                // always update the schema data note
                schemaData.setNote(noteTextArea.getValue());
            }
        };

        TextButton updateButton = new TextButton("Update");
        updateButton.addSelectHandler(sh);
        fp.addButton(updateButton);

        fp.addButton(new TextButton("Submit", new SelectHandler() {

            public void onSelect(SelectEvent event) {
                saveUpdates();
            }
        }));

        return fp;
    }
    
    /**
     * Method to display the window that show the schema fields
     */
    private void displaySchemaFieldsWindow(SchemaData sdata) {
        SchemaFieldsWindow window = new SchemaFieldsWindow(this, sdata);
        window.show();
    }

    /**
     * Method to display a window that allows copying the schema fields and
     * information in a text window
     *
     * @param sdata
     */
    private void displayCopySchemaFieldsWindow() {
        SchemaFieldsCopyWindow window = new SchemaFieldsCopyWindow(this,
                SchemaFieldsCopyWindow.COPY_VIEW, schemaData);
        window.show();
    }

    /**
     * Method to display import the schema fields window, which allows quick
     * import of data
     */
    private void displayImportSchemaFieldsWindow() {
        SchemaFieldsCopyWindow window = new SchemaFieldsCopyWindow(this,
                SchemaFieldsCopyWindow.IMPORT_VIEW, schemaData);
        window.show();
    }
    
    /**
     * Method to display a window which shows the analysis of the current mappings
     */
    private void displayDataMapAnalyzerWindow() {
        DataMapAnalyzerWindow window = new DataMapAnalyzerWindow(this, schemaData, aspaceSchemaDataList);
        window.show();
    }

    /**
     * Method that called to set the text in the map to text field
     *
     * @param fieldName
     */
    public void updateMapToTextField(String schemaName, String fieldName) {
        mapToTextField.setValue(fieldName);

        SchemaDataField field = grid.getSelectionModel().getSelectedItem();

        if (field != null) {
            field.setMappedTo(schemaName + " -> " + fieldName);

            field.setNote(noteTextField.getCurrentValue());

            // refresh the grid view now
            int index = grid.getStore().indexOf(field);
            grid.getView().refresh(false);
            grid.getView().focusRow(index);
        }
    }
    
    /**
     * Method import mapping data
     * 
     * @param mappingData 
     */
    public void importMappingInfo(HashMap<String,String> mappingData) {
        // go through each item and import the mapping information
        for(SchemaDataField field: schemaData.getFields()) {
            String info = mappingData.get(schemaData.getName() + " -> " + field.getName());
            
            if(info != null) {
                String[] sa = info.split("\\s*\\t\\s*");
                field.setMappedTo(sa[0]);
                field.setNote(sa[1]);
            }
        }
        
        // now update the view
        grid.getView().refresh(false);
    }

    /**
     * Method to display the RESP APIs
     */
    protected void displayRestAPI() {
        Window.open(REST_API_URL, "_blank", "");
    }

    /**
     * Method that display a page for the digitized item when button is pressed
     */
    protected void displaySchemaCode() {
        /*
         * Window window = new Window(); window.setSize("800", "600");
         * window.setModal(false); window.setHeadingText("Schema Data Code -- "
         * + schemaData.getName()); window.setBodyBorder(false);
         *
         * Frame content = new Frame(schemaData.getUrl());
         *
         * window.add(content);
        window.show();
         */

        Window.open(schemaData.getUrl(), "_blank", "");
    }

    /**
     * Method to save the updates on the backend database
     */
    private void saveUpdates() {
        SchemaDataServiceAsync service = MainEntryPoint.getService();

        // Create an asynchronous callback to handle the result.
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onSuccess(String message) {
                Info.display("Submit Meesage", message);
            }

            public void onFailure(Throwable caught) {
                AlertMessageBox alertMessageBox = new AlertMessageBox("Error Saving Data",
                        "Meesage: " + caught.toString());
                alertMessageBox.show();
                System.out.println("Unable to update schema data");
            }
        };

        service.updateSchemaData(MainEntryPoint.username, schemaData, callback);
    }
}
