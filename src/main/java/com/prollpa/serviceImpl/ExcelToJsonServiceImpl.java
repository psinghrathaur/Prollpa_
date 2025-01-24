package com.prollpa.serviceImpl;

import java.io.ByteArrayOutputStream;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prollpa.exception.ResourceNotFoundException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExcelToJsonServiceImpl {
	private static final Logger logger = LoggerFactory.getLogger(ExcelToJsonServiceImpl.class);

	
	public List<String> getAllSheet(MultipartFile file){
		List<String> sheetNames = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
        return sheetNames;
	}
	public byte[] convertExcelToJson(MultipartFile file ,String sheetName) {
		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new ResourceNotFoundException("sheet not present");
            }

            List<Map<String, String>> jsonData = new ArrayList<>();
            Row headerRow = sheet.getRow(0); // Assuming the first row contains headers
            if (headerRow == null) {
            	throw new ResourceNotFoundException("header row not Present");
            }

            // Read headers
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // Read rows and map to JSON
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = cell == null ? "" : getCellValue(cell);
                    rowData.put(headers.get(j), cellValue);
                }
                jsonData.add(rowData);
            }

            // Convert list to JSON
            ObjectMapper mapper = new ObjectMapper();
            byte[] jsonBytes = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(jsonData);

            // Return JSON as downloadable file
           

            return jsonBytes;

        } catch (Exception e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
		

	}
	private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return Double.toString(cell.getNumericCellValue());
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return "";
        }
    }
	
	
    public byte[] convertToFAQJSON(MultipartFile file)  {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Process the first sheet
            logger.info("Processing sheet: {}", sheet.getSheetName());

            JSONObject faqJson = new JSONObject();

            // Extract subtitles and titles
            faqJson.put("subtitle5", getCellValue(sheet, 38)); // B39
            faqJson.put("subtitle4", getCellValue(sheet, 39)); // B40
            faqJson.put("title", getCellValue(sheet, 40));     // B41
            faqJson.put("subtitle2", getCellValue(sheet, 41)); // B42
            faqJson.put("subtitle3", getCellValue(sheet, 42)); // B43
            faqJson.put("subtitle1", getCellValue(sheet, 43)); // B44

            // Extract content
            faqJson.put("content1", extractContent(sheet, 0, 7));   // B1-B8
            faqJson.put("content2", extractContent(sheet, 8, 20));  // B9-B21
            faqJson.put("content3", extractContent(sheet, 21, 23)); // B22-B24
            faqJson.put("content4", extractContent(sheet, 24, 25)); // B25-B26
            faqJson.put("content5", extractContent(sheet, 26, 37)); // B27-B38

            JSONObject finalJson = new JSONObject();
            finalJson.put("faq", faqJson);
            logger.info("FAQ JSON generated successfully");

            // Convert JSON to bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(finalJson.toString(4).getBytes(StandardCharsets.UTF_8)); // Pretty-printed JSON
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    // Extract a single-cell value
    private String getCellValue(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            Cell cell = row.getCell(1); // Assuming data is in column B (index 1)
            if (cell != null) {
                return cell.getStringCellValue().trim();
            }
        }
        return "";
    }

    // Extract content from a range of rows
    private JSONArray extractContent(Sheet sheet, int startRow, int endRow) {
        JSONArray contentArray = new JSONArray();
        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(1);
                if (cell != null) {
                    String cellValue = cell.getStringCellValue().trim();
                    if (!cellValue.isEmpty()) {
                        if (cellValue.contains("?")) {
                            String[] parts = cellValue.split("\\?", 2);
                            String question = parts[0].trim() + "?";
                            String answer = parts.length > 1 ? parts[1].trim() : "";

                            answer = answer.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

                            String description = "<div style=\"height: auto !important;\"><div><div class=\"col-md-10 inner-page-main main-box-content contentpage\">"
                                    + "<div class=\"col-sm-12 col-md-12 padding-zero  \"><div class=\"main-box\">"
                                    + "<div class=\"col-sm-12 col-md-12 padding-zero main-box-content\">"
                                    + "<div class=\"contentpage\"><p>" + answer.replace("\n", "<br>") + "</p>" +
                                    "</div>" +
                                    "</div>" +
                                    "</div>" +
                                    "</div>" +
                                    "</div>" +
                                    "</div>" +
                                    "</div>";

                            JSONObject faqEntry = new JSONObject();
                            faqEntry.put("heading", question);
                            faqEntry.put("description", description);
                            contentArray.put(faqEntry);
                        }
                    }
                }
            }
        }
        return contentArray;
    }
}
