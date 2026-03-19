package com.moneyapp.v1.service;

import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.moneyapp.v1.model.File;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;


@Service
public class CsvExtractorService {

    public String extract(File file) throws IOException {
        String filename = file.getFilename().toLowerCase();
        if (filename.endsWith(".csv")) {
            return extractCsv(file);
        } else {
            return extractExcel(file);
        }
    }

    private String extractCsv(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file.getPath()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private String extractExcel(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (Workbook workbook = WorkbookFactory.create(new java.io.File(file.getPath()))) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    sb.append(cell.toString()).append("\t");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}