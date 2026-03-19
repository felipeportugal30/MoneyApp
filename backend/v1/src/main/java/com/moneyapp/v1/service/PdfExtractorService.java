package com.moneyapp.v1.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.moneyapp.v1.model.File;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Service
public class PdfExtractorService {
    
    public String extract(File file) throws IOException {
        try (PDDocument document = Loader.loadPDF(new java.io.File(file.getPath()))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
