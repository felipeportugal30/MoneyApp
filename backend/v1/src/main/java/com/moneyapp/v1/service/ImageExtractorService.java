package com.moneyapp.v1.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.moneyapp.v1.model.File;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class ImageExtractorService {

    public String extract(File file) throws IOException, TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        tesseract.setLanguage("por");
        return tesseract.doOCR(new java.io.File(file.getPath()));
    }
}
