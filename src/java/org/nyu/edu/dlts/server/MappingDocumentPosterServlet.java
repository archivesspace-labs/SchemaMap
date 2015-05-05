/*
 * A simple servlet to serv up the mapping document xls
 * This is needed becuase tomecat can't seem to correctly
 * serve a static page
 */
package org.nyu.edu.dlts.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author nathan
 */
public class MappingDocumentPosterServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        ServletContext context = getServletConfig().getServletContext();
        String filePath = context.getRealPath("/schemas/AT/" + MappingDocumentGenerator.AT_DOCUMENT_NAME);
        
        File file = new File(filePath);

        response.setHeader("Content-Type", getServletContext().getMimeType(file.getName()));
        response.setHeader("Content-Length", new Long(file.length()).toString());
        response.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        
        try {
            input = new BufferedInputStream(new FileInputStream(file));
            output = new BufferedOutputStream(response.getOutputStream());

            byte[] buffer = new byte[8192];
            for (int length = 0; (length = input.read(buffer)) > 0;) {
                output.write(buffer, 0, length);
            }
             
        } finally {            
            if (output != null) try { output.close(); } catch (IOException ignore) {}
            if (input != null) try { input.close(); } catch (IOException ignore) {}
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
        return "Mapping Document Generator ...";
    }// </editor-fold>
}
