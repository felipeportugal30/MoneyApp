package com.moneyapp.v1.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import net.sourceforge.tess4j.TesseractException;

import org.springframework.stereotype.Service;

import com.moneyapp.v1.exception.InvalidRequestException;
import com.moneyapp.v1.exception.NotFoundException;
import com.moneyapp.v1.model.Transaction;
import com.moneyapp.v1.model.File;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.repository.FileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileExtractorService {

    private final PdfExtractorService pdfExtractor;
    private final CsvExtractorService csvExtractor;
    private final ImageExtractorService imageExtractor;
    private final GroqService groqService;
    private final TransactionService transactionService;
    private final FileRepository fileRepository;

    public List<Transaction> process(UUID file_id, User user) throws Exception, IOException, TesseractException {
        File fileEntity = fileRepository.findByIdAndUser(file_id, user)
                .orElseThrow(() -> new NotFoundException("File not found."));

        String rawText = extractText(fileEntity);
        String json = groqService.extractTransactions(rawText, "portuguese");

        return transactionService.saveTransactions(json, fileEntity, user);
    }

    public String extractText(File file) throws IOException, TesseractException {

        String filename = file.getFilename().toLowerCase();
        String result;

        if (filename.endsWith(".pdf")) {
            result = pdfExtractor.extract(file);
        } else if (filename.endsWith(".csv") || filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            result = csvExtractor.extract(file);
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg") || filename.endsWith(".png")) {
            result = imageExtractor.extract(file);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filename);
        }
        
        if (result == null || result.isBlank()) {
            throw new InvalidRequestException("Nothing was extracted from the file.");
        }

        return result;
    }
}