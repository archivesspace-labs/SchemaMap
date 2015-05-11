/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nyu.edu.dlts.server;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.pure.SchematronResourcePure;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;

/**
 *
 * @author nathan
 */
public class SchematronServlet extends HttpServlet {

    private File schematronFile;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();

        // Configure a repository (to ensure a secure temp location is used)
        ServletContext servletContext = this.getServletConfig().getServletContext();

        String schematronFilename = servletContext.getRealPath("/schematrons/ArchivesSpace-EAD-validator.sch");
        schematronFile = new File(schematronFilename);

        File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        factory.setRepository(repository);

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        try {
            // Parse the request
            List<FileItem> items = upload.parseRequest(request);
            Iterator<FileItem> iter = items.iterator();
            File[] uploadedFiles = new File[4];
            String format = null;
            
            int fileIndex = 0;
            while (iter.hasNext()) {
                FileItem item = iter.next();

                if (item.isFormField()) {
                    format = item.getString();
                } else {
                    String fieldName = item.getFieldName();
                    String fileName = item.getName();
                    
                    if(!fileName.trim().isEmpty()) {
                        uploadedFiles[fileIndex] = new File(repository, fileName);
                        item.write(uploadedFiles[fileIndex]);
                    }
                    
                    fileIndex++;
                }
            }
            
            // if we have files the process them
            if (uploadedFiles[0] != null && format != null) {
                String results = "";
                
                for(int i = 0; i < uploadedFiles.length; i++) {
                    File file = uploadedFiles[i];
                    if(file != null) {
                        results += validateUsingSchematron(format, file);
                    }
                }
                
                out.println(results);
            } else {
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>SchematronServlet Error</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>Error, missing upload file ...</h1>");
                out.println("</body>");
                out.println("</html>");
            }
        } catch (FileUploadException ex) {
            Logger.getLogger(SchematronServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(SchematronServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
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
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Servlet for running xml files against the Archivesspace schematron";
    }// </editor-fold>

    /**
     * Method that calls the Schematron library to validate the file
     *
     * @param format
     * @param uploadedFile
     * @return String
     */
    public String validateUsingSchematron(String format, File uploadedFile) {
        String result = "";

        final ISchematronResource aResPure = SchematronResourcePure.fromFile(schematronFile);
        if (!aResPure.isValidSchematron()) {
            result = "Invalid Schematron!";
        }
        
        try {
            boolean valid = aResPure.getSchematronValidity(new StreamSource(uploadedFile)).isValid();
            
            if(!valid) {
                SchematronOutputType schematronOutputType = aResPure.applySchematronValidationToSVRL(new StreamSource(uploadedFile));
                List<Object> list = schematronOutputType.getActivePatternAndFiredRuleAndFailedAssert();
                
                for(Object object: list) {
                    if(object instanceof FailedAssert) {
                        FailedAssert failedAssert = (FailedAssert)object;
                        String errorText = failedAssert.getText().replace("\n", " ").replaceAll("\\s+", " ");
                        result += "Error :: " + errorText + 
                                "\nLocation :: " + failedAssert.getLocation() + 
                                "\nTest :: " + failedAssert.getTest() + "\n\n";
                    }
                }
                
                result = "File :: " + uploadedFile.getName() + "\n" + result;
            } else {
                result = "File :: " + uploadedFile.getName() + " -- Valid " + format + "\n\n";
            }
        } catch (Exception ex) {
            Logger.getLogger(SchematronServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    /**
     * Method to set the schematron file
     *
     * @param file
     */
    public void setSchemtronFile(File file) {
        schematronFile = file;
    }

    /**
     * Main method for testing purposes
     *
     * @param args
     */
    public static void main(String[] args) {
        String schematronFilename = "/Users/nathan/NetBeansProjects/SchemaMap/web/schematrons/ArchivesSpace-EAD-validator.sch";
        File schematronFile = new File(schematronFilename);
        File xmlFile = new File("/Users/nathan/NetBeansProjects/SchemaMap/web/schematrons/testEAD_Invalid.xml");

        SchematronServlet schematronServlet = new SchematronServlet();
        schematronServlet.setSchemtronFile(schematronFile);
        System.out.println(schematronServlet.validateUsingSchematron("EAD 2002", xmlFile));
    }
}
