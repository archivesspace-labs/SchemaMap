/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.core.client.util.Margins;
import com.sencha.gxt.core.client.util.Padding;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.SortDir;
import com.sencha.gxt.data.shared.Store.StoreSortInfo;
import com.sencha.gxt.widget.core.client.ContentPanel;
import com.sencha.gxt.widget.core.client.ListView;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.*;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer.AccordionLayoutAppearance;
import com.sencha.gxt.widget.core.client.container.AccordionLayoutContainer.ExpandMode;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.container.BoxLayoutContainer.BoxLayoutData;
import com.sencha.gxt.widget.core.client.container.HBoxLayoutContainer.HBoxLayoutAlign;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.info.Info;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import java.util.ArrayList;
import java.util.HashMap;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataProperties;
import org.nyu.edu.dlts.client.widgets.LoginDialog;
import org.nyu.edu.dlts.client.widgets.SchemaDataInfoPanel;
import org.nyu.edu.dlts.client.widgets.SchematronWindow;

/**
 * Main entry point.
 *
 * @author nathan
 */
public class MainEntryPoint implements IsWidget, EntryPoint {
    // The listviews that hold schema names and values for AT, Archon, and ASPace
    private ListView<SchemaData, String> schemaListViewAT;
    private ListView<SchemaData, String> schemaListViewATV;
    private ListView<SchemaData, String> schemaListViewEAD;
    private ListView<SchemaData, String> schemaListViewAS;
    
    // List that stores aspace schema data
    private ArrayList<SchemaData> aspaceSchemaDataList;
    
    // a row layout for adding the template
    final VerticalLayoutContainer rowLayoutContainer = new VerticalLayoutContainer();
    
    private TextButton loginButton;
    
    private HTML loginHtml;
    
    // used to specify if user is logged on
    public static boolean loggedIn = false;
    public static String username = "";

    // define a renderieir class to show the fields
    public interface SchemaRenderer extends XTemplates {

        @XTemplate("<p>Schema Name: <b>{data.name}</b></p><p>Notes: {data.note}</p></br>")
        public SafeHtml renderHeader(SchemaData data);

        @XTemplate("<p>Schema Name: <b>{data.name}</b></p><p>Notes: {data.note}</p></br><p>Field Name, Data Type and Length:</p><tpl for=\"data.fields\"><p>{#}: {name} -- {type}</p></tpl>")
        public SafeHtml render(SchemaData data);
    }
    
    private final SchemaRenderer renderer = GWT.create(SchemaRenderer.class);

    /*
     * This method generates the UI
     */
    public Widget asWidget() {

        final BorderLayoutContainer container = new BorderLayoutContainer();
        container.setBorders(true);

        ContentPanel north = new ContentPanel();
        north.setHeaderVisible(false);

        ContentPanel west = new ContentPanel();

        ContentPanel center = new ContentPanel();
        center.setHeaderVisible(false);

        // add the row panel for using XTemplate
        rowLayoutContainer.setScrollMode(ScrollSupport.ScrollMode.AUTOY);
        center.add(rowLayoutContainer);

        BorderLayoutData northData = new BorderLayoutData(35);
        northData.setMargins(new Margins(5));
        northData.setCollapsible(false);

        BorderLayoutData westData = new BorderLayoutData(250);
        westData.setCollapsible(true);
        westData.setSplit(true);
        westData.setCollapseMini(true);
        westData.setMargins(new Margins(0, 5, 0, 5));

        MarginData centerData = new MarginData();

        BorderLayoutData eastData = new BorderLayoutData(150);
        eastData.setMargins(new Margins(0, 5, 0, 5));
        eastData.setCollapsible(true);
        eastData.setSplit(true);

        container.setNorthWidget(north, northData);
        container.setWestWidget(west, westData);
        container.setCenterWidget(center, centerData);

        SimpleContainer simple = new SimpleContainer();
        simple.add(container, new MarginData(10));

        // add the login/logout buttons
        addWidgetsToNorthPanel(north);

        // add the Lists containing the List of AT Schemas
        addSchemaListViews(west);

        return simple;
    }

    /*
     * Method to add information and login/logout buttons
     */
    private void addWidgetsToNorthPanel(ContentPanel northPanel) {
        HBoxLayoutContainer container = new HBoxLayoutContainer();
        container.setPadding(new Padding(5));
        container.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);

        BoxLayoutData flex = new BoxLayoutData(new Margins(0, 5, 0, 0));
        flex.setFlex(1);
        HTML html = new HTML("<b>Archives Space Schema Mapper</b> v2.0.1 (05/11/2015)");
        container.add(html, flex);
        
        // add the html that hold the login information
        loginHtml = new HTML("");
        container.add(loginHtml, flex);
        
        // the button to view mapping documentation
        TextButton viewButton = new TextButton("Generate AT Mapping Document");
        viewButton.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                generateMappingDocuments();
            }
        });
        
        container.add(viewButton, new BoxLayoutData(new Margins(0, 5, 0, 0)));
        
        // the button to perform a schematron validation
        TextButton schematronButton = new TextButton("Schematron");
        schematronButton.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                SchematronWindow schematronWindow = new SchematronWindow();
                schematronWindow.show();
            }
        });
        
        container.add(schematronButton, new BoxLayoutData(new Margins(0, 5, 0, 0)));
        
        // the login button
        loginButton = new TextButton("Login");
        loginButton.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                // display the login dialog
                LoginDialog loginDialog = new LoginDialog(getEntryPoint());
                loginDialog.show();
            }
        });
        
        container.add(loginButton, new BoxLayoutData(new Margins(0, 5, 0, 0)));

        final TextButton logoutButton = new TextButton("Logout");
        container.add(logoutButton, new BoxLayoutData(new Margins(0, 5, 0, 0)));
        logoutButton.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                // clear the cookie and reload the page now
                Cookies.removeCookie("authorized", "/");                
                Window.Location.reload();
            }
        });

        northPanel.add(container);
    }
    
    /**
     * Method to generate the listViews that holds the list of AT
     * schemas and place them is views
     *
     * @param centerPanel This west centerPanel
     */
    private void addSchemaListViews(ContentPanel centerPanel) {

        AccordionLayoutContainer container = new AccordionLayoutContainer();
        container.setExpandMode(ExpandMode.SINGLE_FILL);

        AccordionLayoutAppearance appearance = GWT.<AccordionLayoutAppearance>create(AccordionLayoutAppearance.class);
        
        // create change handeler for the list views
        SelectionChangedHandler<SchemaData> changeHandler = new SelectionChangedHandler<SchemaData>() {

            @Override
            public void onSelectionChanged(SelectionChangedEvent<SchemaData> event) {
                if (event.getSelection().size() > 0) {
                    SchemaData schemaData = event.getSelection().get(0);

                    // display the field info data now
                    displayFieldInfo(schemaData);
                }
            }
        };
        
        // create the property access object used to access information in Schema Data Objects
        SchemaDataProperties dp = GWT.create(SchemaDataProperties.class);
        
        // Add the contentPanel that hold AT schema data
        ContentPanel cp = new ContentPanel(appearance);
        cp.setAnimCollapse(false);
        cp.setHeadingText("AT Schema");
        container.add(cp);
        container.setActiveWidget(cp);

        // Create the store that the contains the schema data
        ListStore<SchemaData> storeAT = new ListStore<SchemaData>(dp.id());
        storeAT.addSortInfo(new StoreSortInfo<SchemaData>(dp.name(), SortDir.ASC));
        storeAT.add(new SchemaData("Loading AT Schema Data, Please wait ...", null));

        // Create the tree using the store and value provider for the name field
        schemaListViewAT = new ListView<SchemaData, String>(storeAT, dp.name());
        schemaListViewAT.getSelectionModel().addSelectionChangedHandler(changeHandler);
        cp.add(schemaListViewAT);
        
        // Add the contentPanel that hold AT Value data
        cp = new ContentPanel(appearance);
        cp.setAnimCollapse(false);
        cp.setHeadingText("AT Data Values");
        container.add(cp);

        // Create the store that the contains the AT initial data value
        ListStore<SchemaData> storeATV = new ListStore<SchemaData>(dp.id());
        storeATV.addSortInfo(new StoreSortInfo<SchemaData>(dp.name(), SortDir.ASC));
        SchemaData tempValue = new SchemaData("Loading AT Data Values, Please wait ...", null);
        tempValue.setType(SchemaData.AT_VALUE);
        storeATV.add(tempValue);

        // Create the tree using the store and value provider for the name field
        schemaListViewATV = new ListView<SchemaData, String>(storeATV, dp.name());
        schemaListViewATV.getSelectionModel().addSelectionChangedHandler(changeHandler);
        cp.add(schemaListViewATV);
        
        // Add the contentPanel that hold EAD schema data
        cp = new ContentPanel(appearance);
        cp.setAnimCollapse(false);
        cp.setHeadingText("EAD Schemas");
        container.add(cp);

        // Create the store that the contains the AT initial data value
        ListStore<SchemaData> storeEAD = new ListStore<SchemaData>(dp.id());
        storeEAD.addSortInfo(new StoreSortInfo<SchemaData>(dp.name(), SortDir.ASC));
        SchemaData tempEAD = new SchemaData("Loading EAD Schema, Please wait ...", null);
        tempEAD.setType(SchemaData.EAD_TYPE);
        storeEAD.add(tempEAD);

        // Create the tree using the store and value provider for the name field
        schemaListViewEAD = new ListView<SchemaData, String>(storeEAD, dp.name());
        schemaListViewEAD.getSelectionModel().addSelectionChangedHandler(changeHandler);
        cp.add(schemaListViewEAD);
        

        // add Archivespace schema list view
        cp = new ContentPanel(appearance);
        cp.setAnimCollapse(false);
        cp.setHeadingText("ASpace Schema");
        container.add(cp);

        // Create the store and list view for archive space schema data
        ListStore<SchemaData> storeAS = new ListStore<SchemaData>(dp.id());
        storeAS.addSortInfo(new StoreSortInfo<SchemaData>(dp.name(), SortDir.ASC));
        storeAS.add(new SchemaData("Loading ASpace Schema Data, Please wait ...", null));

        schemaListViewAS = new ListView<SchemaData, String>(storeAS, dp.name());
        schemaListViewAS.getSelectionModel().addSelectionChangedHandler(changeHandler);
        cp.add(schemaListViewAS);
        
        centerPanel.add(container);
    }

    /**
     * Method that displays information about the particular field
     * @param schemaData 
     */
    private void displayFieldInfo(SchemaData schemaData) {
        //TEST CODE -- display the SchemaData Information
        rowLayoutContainer.clear();

        /*HTML text = new HTML(renderer.renderHeader(schemaData));
        text.addStyleName("pad-text");
        text.setLayoutData(new VerticalLayoutData(1, -1));
        rowLayoutContainer.add(text, new VerticalLayoutData(-1, -1, new Margins(4)));*/

        // add the table that holds the field information
        SchemaDataInfoPanel grid = new SchemaDataInfoPanel(aspaceSchemaDataList, schemaData);
        rowLayoutContainer.add(grid, new VerticalLayoutData(-1, -1, new Margins(4)));
    }

    /**
     * Method to make asynch call to load the schema data
     */
    private void loadSchemaData() {
        // Create an asynchronous callback to handle the AT schema data
        AsyncCallback<ArrayList<SchemaData>> callbackAT = new AsyncCallback<ArrayList<SchemaData>>() {

            public void onSuccess(ArrayList<SchemaData> result) {
                schemaListViewAT.getStore().clear();
                schemaListViewAT.getStore().addAll(result);
                System.out.println("Loaded AT Schema data: " + result.size());
            }

            public void onFailure(Throwable caught) {
                System.out.println("Error loading AT schema data");
            }
        };
        
        // Create an asynchronous callback to handle the EAD schema data
        AsyncCallback<ArrayList<SchemaData>> callbackEAD = new AsyncCallback<ArrayList<SchemaData>>() {

            public void onSuccess(ArrayList<SchemaData> result) {
                schemaListViewEAD.getStore().clear();
                schemaListViewEAD.getStore().addAll(result);
                System.out.println("Loaded EAD Schema data: " + result.size());
            }

            public void onFailure(Throwable caught) {
                System.out.println("Error loading EAD schema data");
            }
        };
        
        // Create an asynchronous callback to handle the ASpace schema data.
        AsyncCallback<ArrayList<SchemaData>> callbackAS = new AsyncCallback<ArrayList<SchemaData>>() {

            public void onSuccess(ArrayList<SchemaData> result) {
                schemaListViewAS.getStore().clear();
                schemaListViewAS.getStore().addAll(result);

                aspaceSchemaDataList = result;

                System.out.println("Loaded ASpace Schema data: " + result.size());
            }

            public void onFailure(Throwable caught) {
                System.out.println("Error loading ASpace schema data");
            }
        };

        // make the call to service 
        SchemaDataServiceAsync service = getService();
        
        service.getSchemaDataAT(callbackAT);
        service.getSchemaDataEAD(callbackEAD);
        service.getSchemaDataAS(callbackAS);
    }
    
    /**
     * Method to make asynch call to load the schema data values
     */
    private void loadSchemaDataValue() {
        // Create an asynchronous callback to handle the AT schema data
        AsyncCallback<HashMap<String, SchemaData>> callbackATV = new AsyncCallback<HashMap<String, SchemaData>>() {

            public void onSuccess(HashMap<String, SchemaData> result) {
                // generate temporary arraylist that holds dummy schemadata
                ArrayList<SchemaData> schemaDataList = new ArrayList<SchemaData>();
                
                for (String key: result.keySet()) {
                    SchemaData schemaData = result.get(key);
                    schemaDataList.add(schemaData);
                }
                
                schemaListViewATV.getStore().clear();
                schemaListViewATV.getStore().addAll(schemaDataList);
                
                System.out.println("Loaded AT Schema Data Values: " + schemaDataList.size());
            }

            public void onFailure(Throwable caught) {
                System.out.println("Error loading AT schema data values");
            }
        };
        

        // make the call to service 
        SchemaDataServiceAsync service = getService();
        
        service.getDataValues(SchemaData.AT_VALUE, callbackATV);
    }
    
    /**
     * Method to make a call to the server to generate the mapping documents
     */
    private void generateMappingDocuments() {
        // Create an asynchronous callback to handle the AT schema data
        AsyncCallback<String> callback = new AsyncCallback<String>() {

            public void onSuccess(String result) {
                Info.display("Document Generated", result);
                Window.open(result, "_blank", "");
            }

            public void onFailure(Throwable caught) {
                System.out.println("Error generating mapping document");
            }
        };
        
        SchemaDataServiceAsync service = getService();
        service.generateMappingDocument(callback);
    }
    
    /**
     * This is called after a user successfully logs in
     * 
     * @param loggedIn 
     */
    public void setLoggedIn(boolean loggedIn, String message) {
        this.loggedIn = loggedIn;
        this.username = message;
        
        if(loggedIn) {
            loginButton.setEnabled(false);
            loginHtml.setHTML("welcome <i>" + message + "</i>");
        }
    }
    
    /**
     * Method to return this object for any dialog that may need it
     * 
     * @return 
     */
    public MainEntryPoint getEntryPoint() {
        return this;
    }

    /**
     * The entry point method, called automatically by loading a module that
     * declares an implementing class as an entry-point
     */
    public void onModuleLoad() {
        Widget container = asWidget();
        Viewport viewport = new Viewport();
        viewport.add(container);
        RootPanel.get().add(viewport);

        // call the method to load the schema information
        loadSchemaData();
        
        // call the method to load the schema data values
        loadSchemaDataValue();
        
        // check cookie if we are loged on
        String authorized = Cookies.getCookie("authorized");
        if(authorized != null && authorized.contains("yes")) {
            username = authorized.replace("yes --", "");
            setLoggedIn(true, username);
        }
    }

    /**
     * Method to get the service for getting and updating data from the backend
     * database.
     *
     * @return
     */
    public static SchemaDataServiceAsync getService() {
        return GWT.create(SchemaDataService.class);
    }
}
