package com.prollpa.serviceImpl;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prollpa.exception.ResourceNotFoundException;

@Service
public class ExcelToJsonServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(ExcelToJsonServiceImpl.class);

    // Retrieve all sheet names from the Excel file
    public List<String> getAllSheet(MultipartFile file) {
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

    // Convert Excel to generic JSON format
    public byte[] convertExcelToJson(MultipartFile file, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) throw new ResourceNotFoundException("Sheet not present");

            List<Map<String, String>> jsonData = new ArrayList<>();
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) throw new ResourceNotFoundException("Header row not present");

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) headers.add(cell.getStringCellValue());

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    rowData.put(headers.get(j), cell == null ? "" : getCellValue(cell));
                }
                jsonData.add(rowData);
            }

            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsBytes(jsonData);
        } catch (Exception e) {
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    // Convert Excel to FAQ JSON format
    public byte[] convertToFAQJSON(MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            logger.info("Processing sheet: {}", sheet.getSheetName());

            JSONObject faqJson = new JSONObject();
            faqJson.put("subtitle5", getCellValue(sheet, 38));
            faqJson.put("subtitle4", getCellValue(sheet, 39));
            faqJson.put("title", getCellValue(sheet, 40));
            faqJson.put("subtitle2", getCellValue(sheet, 41));
            faqJson.put("subtitle3", getCellValue(sheet, 42));
            faqJson.put("subtitle1", getCellValue(sheet, 43));

            faqJson.put("content1", extractContent(sheet, 0, 7));
            faqJson.put("content2", extractContent(sheet, 8, 20));
            faqJson.put("content3", extractContent(sheet, 21, 23));
            faqJson.put("content4", extractContent(sheet, 24, 25));
            faqJson.put("content5", extractContent(sheet, 26, 37));

            JSONObject finalJson = new JSONObject();
            finalJson.put("faq", faqJson);
            logger.info("FAQ JSON generated successfully");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(finalJson.toString(4).getBytes(StandardCharsets.UTF_8));
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            throw new ResourceNotFoundException(e.getMessage());
        }
    }

    // Get single-cell value safely
    private String getCellValue(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row != null) {
            Cell cell = row.getCell(1);
            if (cell != null) {
                return getCellValue(cell);
            }
        }
        return "";
    }

    // Extract FAQ content from rowsf
    private JSONArray extractContent(Sheet sheet, int startRow, int endRow) {
        JSONArray contentArray = new JSONArray();
        for (int i = startRow; i <= endRow; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                Cell cell = row.getCell(1);
                if (cell != null) {
                    String cellValue = getCellValue(cell);
                    if (!cellValue.isEmpty()) {
                        processFAQEntry(cellValue, contentArray);
                    }
                }
            }
        }
        return contentArray;
    }

    // Process FAQ question and answer
    private void processFAQEntry(String cellValue, JSONArray contentArray) {
        boolean isArabic = containsArabic(cellValue);
        String delimiter = isArabic ? "؟" : "\\?";
        String[] parts = cellValue.split(delimiter, 2);
        if (parts.length < 2) return;

        String question = parts[0].trim() + (isArabic ? "؟" : "?");
        String answer = parts[1].trim();

        answer = answer.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
        String direction = isArabic ? "rtl" : "ltr";

        String description = "<div style='direction: " + direction + "; text-align: " + (isArabic ? "right" : "left") + ";'>"
                + "<div><p>" + answer.replace("\n", "<br>") + "</p></div></div>";

        JSONObject faqEntry = new JSONObject();
        faqEntry.put("heading", question);
        faqEntry.put("description", description);
        contentArray.put(faqEntry);
    }

    // Check if text contains Arabic characters
    private boolean containsArabic(String text) {
        return Pattern.compile("[\\u0600-\\u06FF]").matcher(text).find();
    }

    // Get cell value safely
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
            default:
                return "";
        }
    }
}
