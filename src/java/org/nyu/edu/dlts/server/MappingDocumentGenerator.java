package org.nyu.edu.dlts.server;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.nyu.edu.dlts.client.model.SchemaData;
import org.nyu.edu.dlts.client.model.SchemaDataField;


/**
 * A class to generate an excel file containing the mapping document
 * @author nathan
 */
public class MappingDocumentGenerator {
    public static final String AT_DOCUMENT_NAME  = "mappingDocumentAT.xls";
    
    private ArrayList<String> ignoreList = new ArrayList<String>();
    
    public MappingDocumentGenerator() {
        ignoreList.add("ATPluginData");
        ignoreList.add("ArchDescriptionSubjects");
        ignoreList.add("ArchDescriptionNames");
        ignoreList.add("Assessments");
        ignoreList.add("DatabaseFields");
        ignoreList.add("DatabaseTables");
        ignoreList.add("LookupList");
        ignoreList.add("LookupListItems");
        ignoreList.add("RepositoryNotesDefaultValues");
        ignoreList.add("RepositoryStatistics");
    }
    
    /**
     * Method to generate the mapping document
     * 
     * @param path
     * @param schemaDataListAT
     * @return 
     */
    public String generateATMappingDocument(String path, ArrayList<SchemaData> schemaDataListAT) throws Exception {
        //System.out.println("Path: " + path);
        
        sortSchemaDataList(schemaDataListAT);
        
        // create the workbook
        HSSFWorkbook wb = new HSSFWorkbook();
        
        // create a font with bold text
        CellStyle boldStyle = wb.createCellStyle();
        HSSFFont wbfont = wb.createFont();
        wbfont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        wbfont.setFontHeightInPoints((short)12);
        boldStyle.setFont(wbfont);
        
        for(SchemaData schemaData: schemaDataListAT) {
            String name = schemaData.getName();
            
            // if this schema is in the ignore list continue
            if(ignoreList.contains(name)) continue;
            
            // create s sheet and the header cells
            Sheet sheet = wb.createSheet(name);
            sheet.setColumnWidth(0, 30*256);
            sheet.setColumnWidth(1, 40*256);
            sheet.setColumnWidth(2, 200*256);
            
            Row headerRow = sheet.createRow(0);
            
            Cell cellA = headerRow.createCell(0);
            cellA.setCellStyle(boldStyle);
            cellA.setCellValue("AT Field Name");
            
            Cell cellB = headerRow.createCell(1);
            cellB.setCellStyle(boldStyle);
            cellB.setCellValue("Archives Space Field Mapped To");
            
            Cell cellC = headerRow.createCell(2);
            cellC.setCellStyle(boldStyle);
            cellC.setCellValue("Additional Mapping Notes");
            
            // for each of the fields enter a row in the spreadsheet
            int i = 1;
            for(SchemaDataField schemaDataField: schemaData.getFields()) {
                String dataFieldName = schemaDataField.getName();
                
                if(dataFieldName.contains("::")) {
                    String[] sa = dataFieldName.split("::");
                    dataFieldName = sa[1];
                }
                
                Row fieldRow = sheet.createRow(i);
            
                cellA = fieldRow.createCell(0);
                cellA.setCellValue(dataFieldName);
            
                cellB = fieldRow.createCell(1);
                cellB.setCellValue(schemaDataField.getMappedTo());
            
                cellC = fieldRow.createCell(2);
                cellC.setCellValue(schemaDataField.getNote());
                
                i++;
            }
            
            // add the row holding the notes of the schema
            i += 2;
            Row fieldRow = sheet.createRow(i);
            
            cellA = fieldRow.createCell(0);
            cellA.setCellValue("Additional Notes: " + schemaData.getNote());
            
            sheet.addMergedRegion(new CellRangeAddress(
                i, //first row (0-based)
                i, //last row  (0-based)
                0, //first column (0-based)
                2  //last column  (0-based)
            ));
        }
        
        // save the excel workbook now
        
        String documentFilePath = path + AT_DOCUMENT_NAME;
        FileOutputStream out = new FileOutputStream(documentFilePath);
        wb.write(out);
        out.close();
        
        return AT_DOCUMENT_NAME;
    }
    
    /**
     * Method to sort the schema data list
     * 
     * @param schemaDataListAT 
     */
    private void sortSchemaDataList(ArrayList<SchemaData> schemaDataListAT) {
        Collections.sort(schemaDataListAT, new CustomComparator());
    }
    
    /**
     * Implement inner class for sorting the SchemaData
     */
    public class CustomComparator implements Comparator<SchemaData> {
        @Override
        public int compare(SchemaData schemaData1, SchemaData schemaData2) {
            return schemaData1.getName().compareTo(schemaData2.getName());
        }
    }
    
    /**
     * Method for testing functionality of this class without 
     * going through web UI
     * 
     * @param args 
     */
    public static void main(String[] args) {
        try {
            // first get the saved schema datalist
            DatabaseUtil.setupTestDatabaseInfo();
            DatabaseUtil.getConnection();
            ArrayList<SchemaData> schemaDataList = DatabaseUtil.getSchemaDataList(SchemaData.AT_TYPE);
            DatabaseUtil.closeConnection();
            
            // now generate the documents now
            String path = "/Users/nathan/NetBeansProjects/SchemaMap/web/schemas/AT/";
            
            MappingDocumentGenerator mappingDocumentGenerator = new MappingDocumentGenerator();
            mappingDocumentGenerator.generateATMappingDocument(path, schemaDataList);
        } catch (Exception ex) {
            Logger.getLogger(MappingDocumentGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
