package com.moneyapp.v1.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneyapp.v1.model.Expense;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.service.FileExtractorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/extractor-file")
@RequiredArgsConstructor
public class FileExtractorController {
    
    private final FileExtractorService fileExtractorService;

    @PostMapping("/{file_id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Expense>> extractFile(
        @PathVariable UUID file_id,
        @AuthenticationPrincipal User user
    ) throws Exception {
        return ResponseEntity.ok(fileExtractorService.process(file_id, user));
    }
}
