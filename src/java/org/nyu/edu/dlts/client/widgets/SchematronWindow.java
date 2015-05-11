/*
 * Simple class to modify enrollment in a variation course
 */
package org.nyu.edu.dlts.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction;
import com.sencha.gxt.core.client.dom.ScrollSupport;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.widget.core.client.Window;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent;
import com.sencha.gxt.widget.core.client.event.SubmitCompleteEvent.SubmitCompleteHandler;
import com.sencha.gxt.widget.core.client.form.ComboBox;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.FileUploadField;
import com.sencha.gxt.widget.core.client.form.FormPanel;
import com.sencha.gxt.widget.core.client.form.FormPanel.Encoding;
import com.sencha.gxt.widget.core.client.form.FormPanel.Method;
import com.sencha.gxt.widget.core.client.form.TextArea;
import com.sencha.gxt.widget.core.client.info.Info;

/**
 *
 * @author nathan
 */
public class SchematronWindow extends Window {

    private String heading;

    private boolean modal = false;

    private String postURL;

    private TextArea textArea;

    /**
     * Constructor which takes the search results
     */
    public SchematronWindow() {
        heading = "Archivesspace Schematron";
        postURL = GWT.getModuleBaseURL().replace("org.nyu.edu.dlts.Main/", "") + "SchematronServlet";

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

        // add the file upload form
        final FormPanel form = new FormPanel();
        form.setAction(postURL);
        form.setEncoding(Encoding.MULTIPART);
        form.setMethod(Method.POST);
        
        // add handeler for when 
        form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
            public void onSubmitComplete(SubmitCompleteEvent event) {
                String resultHtml = event.getResults();
                textArea.setValue(resultHtml);
            }
        });

        container.add(form);

        // add the container that holds all the UI componeny for the form
        VerticalLayoutContainer formPanelContainer = new VerticalLayoutContainer();
        form.add(formPanelContainer);
        
        // add the first file upload component
        final FileUploadField file1 = new FileUploadField();
        file1.setName("uploadedFile1");
        file1.setAllowBlank(false);
        file1.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                Info.display("File 1 Changed", "You selected " + file1.getValue());
            }
        });

        formPanelContainer.add(new FieldLabel(file1, "File 1"), new VerticalLayoutData(-18, -1));
        
        // add the second file upload component
        final FileUploadField file2 = new FileUploadField();
        file2.setName("uploadedFile2");
        file2.setAllowBlank(true);
        file2.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                Info.display("File 2 Changed", "You selected " + file2.getValue());
            }
        });

        formPanelContainer.add(new FieldLabel(file2, "File 2"), new VerticalLayoutData(-18, -1));
        
        // add the third file upload component
        final FileUploadField file3 = new FileUploadField();
        file3.setName("uploadedFile2");
        file3.setAllowBlank(true);
        file3.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                Info.display("File 3 Changed", "You selected " + file3.getValue());
            }
        });

        formPanelContainer.add(new FieldLabel(file3, "File 3"), new VerticalLayoutData(-18, -1));
        
        // add the third file upload component
        final FileUploadField file4 = new FileUploadField();
        file4.setName("uploadedFile4");
        file4.setAllowBlank(true);
        file4.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                Info.display("File 4 Changed", "You selected " + file4.getValue());
            }
        });

        formPanelContainer.add(new FieldLabel(file4, "File 4"), new VerticalLayoutData(-18, -1));

        // add the combo box which allows selection of the type of file
        ListStore<String> schematrons = new ListStore<String>(new ModelKeyProvider<String>() {
            @Override
            public String getKey(String item) {
                return item;
            }
        });

        schematrons.add("EAD 2002");
        schematrons.add("EAD 2015");

        ComboBox<String> combo = new ComboBox<String>(schematrons, new StringLabelProvider());
        combo.setName("format");
        combo.setTriggerAction(TriggerAction.ALL);

        formPanelContainer.add(new FieldLabel(combo, "File Type"), new VerticalLayoutData(-18, -1));

        // add the text area to display result frmo schematron run
        textArea = new TextArea();
        textArea.setSize("500", "300");
        textArea.setValue("Schematron Processor @ " + postURL);
        formPanelContainer.add(textArea);

        // add the buttons now
        TextButton resetButton = new TextButton("Reset");
        resetButton.addSelectHandler(new SelectHandler() {
            @Override
            public void onSelect(SelectEvent event) {
                form.reset();
                file1.reset();
                file2.reset();
                file3.reset();
                file4.reset();
            }
        });

        addButton(resetButton);

        // add the submit button
        TextButton submitButton = new TextButton("Submit");
        submitButton.addSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
                if (!form.isValid()) {
                    return;
                }

                form.submit();

                // aler the user that their file is being validated
                textArea.setValue("Validating EAD file(s) ...");
            }
        });

        addButton(submitButton);

        /* add button to close this window
        TextButton closeButton = new TextButton("Close", new SelectHandler() {
            public void onSelect(SelectEvent event) {
                hide();
            }
        });

        addButton(closeButton);*/

        // add the button bar to the sourth 
        setFocusWidget(submitButton);
    }
}
