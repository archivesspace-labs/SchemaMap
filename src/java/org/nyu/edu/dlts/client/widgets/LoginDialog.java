/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.client.widgets;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.widget.core.client.Dialog;
import com.sencha.gxt.widget.core.client.Status;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.PasswordField;
import com.sencha.gxt.widget.core.client.form.TextField;
import com.sencha.gxt.widget.core.client.form.ValueBaseField;
import com.sencha.gxt.widget.core.client.toolbar.FillToolItem;
import java.util.Date;
import org.nyu.edu.dlts.client.MainEntryPoint;
import org.nyu.edu.dlts.client.SchemaDataServiceAsync;

/**
 *
 * @author nathan
 */
public class LoginDialog extends Dialog {
    protected MainEntryPoint entryPoint;
    protected TextField userName;
    protected PasswordField password;
    protected TextButton reset;
    protected TextButton login;
    protected Status status;
    private SchemaDataServiceAsync service;
    
    
    // defualt constructor
    public LoginDialog(MainEntryPoint entryPoint) {
        this.entryPoint = entryPoint;
        
        VerticalLayoutContainer container = new VerticalLayoutContainer();
        setWidget(container);

        setHeadingText("Schema Mapper Login");
        setModal(true);
        setBodyBorder(true);
        setBodyStyle("padding: 8px;background: none");
        //setWidth(400);
        setResizable(false);

        userName = new TextField();
        container.add(new FieldLabel(userName, "Username"), new VerticalLayoutData(1, -1));

        password = new PasswordField();
        container.add(new FieldLabel(password, "Password"), new VerticalLayoutData(1, -1));

        setFocusWidget(userName);
        
        // create the buttons
        createButtons();
        
        // initialize the connection to the course service
        service = MainEntryPoint.getService();
    }

    @Override
    protected void createButtons() {
        getButtonBar().clear(); // remove any buttons already there
        
        status = new Status();
        status.hide();
        addButton(status);
        
        addButton(new FillToolItem());

        reset = new TextButton("Reset");
        reset.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                userName.reset();
                password.reset();
                userName.focus();
            }
        });

        login = new TextButton("Login");
        login.addSelectHandler(new SelectHandler() {
            public void onSelect(SelectEvent event) {
                onSubmit();
            }
        });

        addButton(reset);
        addButton(login);
    }

    protected void onSubmit() {
        status.setBusy("please wait...");
        status.show();
        getButtonBar().disable();

        // try to authorize the user now
        service.authorize(userName.getValue(), password.getValue(), new AsyncCallback<String>() {

            public void onFailure(Throwable caught) {
                System.out.println("Failed to login in");
            }

            public void onSuccess(String result) {
                if (result.contains("authorized")) {
                    LoginDialog.this.hide();
                    
                    String message = result.replace("authorized --", "");
                    
                    // set a cookie so that we are not presented with this
                    // screen all the time
                    final long DURATION = 1000 * 60 * 60 * 24 * 14; //duration remembering login. 2 weeks in this example.
                    Date expires = new Date(System.currentTimeMillis() + DURATION);
                    
                    Cookies.setCookie("authorized", "yes --" + message, expires, null, "/", false);
                    
                    entryPoint.setLoggedIn(true, message);
                } else {
                    status.clearStatus(result);
                    getButtonBar().enable();
                }
            }
        });
    }
    
    // two method below are not used
    protected boolean hasValue(ValueBaseField field) {
        return field.getValue() != null && !field.getValue().toString().isEmpty();
    }

    protected void validate() {
        login.setEnabled(hasValue(userName) && hasValue(password)
                && password.getValue().length() > 3);
    }
}
