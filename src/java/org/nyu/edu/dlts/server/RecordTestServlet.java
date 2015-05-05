/*
 * A very simple servlet to run unit test on ASpace json records.
 * One of the record submitted must be the expected record before a test
 * can be run.
 */
package org.nyu.edu.dlts.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 *
 * @author Nathan Stevens
 */
public class RecordTestServlet extends HttpServlet {
    private HashMap<String, JSONObject> recordList = new HashMap<String,JSONObject>();
    

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // set the header to output text
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // first try generating the json object from the text of the request
            JSONObject record = null;
            
            try {
                record = getRecord(request);
            } catch (Exception ex) {
                out.println("Error Processing JSON Record ...");
                out.println(ex.toString());
                return;
            }
            
            // get the task to do, which could be test or store
            if(record != null) {
                String[] sa = record.getString("task").split("-");
                String task = sa[0].toUpperCase();  // store or test
                String program = sa[1]; // AT or AR so that the correct expected Record could be compared against
                
                String recordType = record.getString("jsonmodel_type") + "_" + program;
                out.println("Task: " + task);
                out.println("Record Type: " + recordType);
                
                // remove the task field from json record so that it can be compaired  
                record.remove("task");
                
                if(task.contains("STORE")) {
                    recordList.put(recordType, record);
                    
                    out.println("Record Stored ...");
                    //out.println(record.toString(4));
                } else if (task.contains("TEST")) { 
                    //out.println("Testing Record ...");
                    //out.println(record.toString(4));
                    
                    // get the stored record to test against
                    JSONObject expectedRecord = recordList.get(recordType);
                    
                    if(expectedRecord != null) {
                        //out.println("Testing Againts:\n");
                        //out.println(expectedRecord.toString(4));
                        //out.println("\n");
                        
                        try {
                            JSONAssert.assertEquals(expectedRecord, record, false);
                            
                            out.println("Test Passed ...\n");
                        } catch(AssertionError ae) {
                            out.println("Test Failed\n");
                            out.println(ae.toString());
                        }
                    } else {
                        out.println("No Test Record Found ...");
                    }
                }
            } else {
                // If person going to browser or an error then display html
                out.println("Record Test Servlet");  
                out.println("Stored Test Records: " + recordList.size() + "\n");
            
                for(String key: recordList.keySet()) {
                    JSONObject savedReport = recordList.get(key);
                    out.println("Record Type: " + key);
                    out.println(savedReport.toString(4));
                    out.println("\n");
                }
            }
            
        } finally {            
            out.close();
        }
    }
    
    /**
     * Method to parse the json report from the AT or something
     * 
     * @param request
     * @return 
     */
    private JSONObject getRecord(HttpServletRequest request) throws Exception {
        StringBuilder jb = new StringBuilder();
        String line = null;

        BufferedReader reader = request.getReader();

        while ((line = reader.readLine()) != null) {
            jb.append(line);
        }

        // Check to see line is not null which is the case if we viewing this in
        // a browser
        String jsonText = jb.toString();
        if (!jsonText.isEmpty()) {
            JSONObject recordJS = new JSONObject(jsonText);

            // remove certain information so that we only comparing 
            // data fields, not uri and other field which should be different
            RecordTestUtil.cleanRecord(recordJS);

            return recordJS;
        } else {
            return null;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Run Unit test on ASpace records";
    }// </editor-fold>
}
