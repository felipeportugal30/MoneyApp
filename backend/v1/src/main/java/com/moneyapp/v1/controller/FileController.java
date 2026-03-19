package com.moneyapp.v1.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.moneyapp.v1.dto.DeleteFileResponseDto;
import com.moneyapp.v1.dto.FileListResponseDto;
import com.moneyapp.v1.dto.UploadFileResponseDto;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.service.FileService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    
    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UploadFileResponseDto> uploadFile(
        @RequestParam("files") List<MultipartFile> files, 
        @AuthenticationPrincipal User user
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.uploadFile(files, user));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<FileListResponseDto>> listUserFiles(
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(fileService.listFiles(user));
    }

    @DeleteMapping("/delete/{file_id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DeleteFileResponseDto> deleteUserFile(
        @PathVariable UUID file_id,
        @AuthenticationPrincipal User user
    ) throws IOException {
        return ResponseEntity.ok(fileService.deleteFile(file_id, user));
    }
}
