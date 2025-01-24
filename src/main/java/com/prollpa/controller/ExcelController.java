package com.prollpa.controller;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.prollpa.exception.ResourceNotFoundException;
import com.prollpa.serviceImpl.ExcelToJsonServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/excel")
@Tag(name = "Excel Controller API", description = "Excel sheet apis")
public class ExcelController {
	private static final Logger logger = LoggerFactory.getLogger(ExcelController.class);
	@Autowired
	private ExcelToJsonServiceImpl excelService;
	
	@PostMapping("/sheets")
	@Operation(summary = "get the excel sheet names")
    public ResponseEntity<List<String>> getSheetNames(@RequestParam("file") MultipartFile file) {
        List<String> allSheet = excelService.getAllSheet(file);
        return ResponseEntity.ok(allSheet);
    }
	@PostMapping("/convertJson")
	@Operation(summary = "convertJson")
	public ResponseEntity<byte[]> convertExcelToJson(
	        @RequestParam("file") MultipartFile file,
	        @RequestParam(value = "sheetName", required = false, defaultValue = "") String sheetName) {

	    // File Validation
	    if (file == null || file.isEmpty()) {
	        logger.warn("Uploaded file is null or empty");
	        return ResponseEntity.badRequest()
	                .body("Uploaded file is null or empty".getBytes());
	    }

	    if (!file.getOriginalFilename().endsWith(".xlsx")) {
	        logger.warn("Uploaded file is not an Excel file (.xlsx)");
	        return ResponseEntity.badRequest()
	                .body("Only .xlsx files are supported".getBytes());
	    }

	    try {
	        if (sheetName.isEmpty()) {
	            List<String> allSheet = excelService.getAllSheet(file);
	            if (allSheet.isEmpty()) {
	                logger.warn("The uploaded file does not contain any sheets");
	                return ResponseEntity.badRequest()
	                        .body("The uploaded file does not contain any sheets".getBytes());
	            }
	            sheetName = allSheet.get(0); // Default to the first sheet
	        }

	        logger.info("{} -> get", file.getOriginalFilename());

	        // Convert Excel to JSON
	        byte[] jsonBytes = excelService.convertExcelToJson(file, sheetName);

	        // Prepare response headers
	        HttpHeaders headers = new HttpHeaders();
	        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.json");
	        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

	        return ResponseEntity.ok()
	                .headers(headers)
	                .body(jsonBytes);

	    } catch (Exception e) {
	        logger.error("Error processing file: {}", e.getMessage(), e);
	        return ResponseEntity.status(500)
	                .body(("Error processing file: " + e.getMessage()).getBytes());
	    }
	}
	
	@PostMapping("/convertToFAQJSON")
	public ResponseEntity<byte[]> convertToFAQJSON(@RequestParam("file") MultipartFile file){
		 logger.info("Received request to generate downloadable FAQ JSON file");

	        if (file == null || file.isEmpty()) {
	            return ResponseEntity.badRequest().body(null);
	        }

	        if (!file.getOriginalFilename().endsWith(".xlsx")) {
	            return ResponseEntity.badRequest().body(null);
	        }

	        try {
	            byte[] jsonBytes = excelService.convertToFAQJSON(file);

	            HttpHeaders headers = new HttpHeaders();
	            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=faq.json");
	            headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

	            return ResponseEntity.ok()
	                    .headers(headers)
	                    .body(jsonBytes);
	        } catch (Exception e) {
	            logger.error("Error processing file: {}", e.getMessage(), e);
	            throw new ResourceNotFoundException(e.getMessage());
	        }
		
	}
	


    

	

}
