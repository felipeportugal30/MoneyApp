package com.moneyapp.v1.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import com.moneyapp.v1.dto.DeleteFileResponseDto;
import com.moneyapp.v1.dto.FileListResponseDto;
import com.moneyapp.v1.dto.UploadFileResponseDto;
import com.moneyapp.v1.exception.InvalidRequestException;
import com.moneyapp.v1.exception.NotFoundException;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.model.File;
import com.moneyapp.v1.repository.FileRepository;

import java.io.IOException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
    
    private final FileRepository fileRepository;

    @Value("${upload.dir:uploads}")
    private String uploadDir;
    
    public UploadFileResponseDto uploadFile(List<MultipartFile> files, User user) throws IOException {

        List<UUID> ids = new ArrayList<>();
        List<String> filenames = new ArrayList<>();
        List<Double> sizes = new ArrayList<>();

        for (MultipartFile multipartFile : files) {
            if (multipartFile.isEmpty()) continue;

            byte[] bytes = multipartFile.getBytes();
            String hash = generateHash(bytes);

            if (fileRepository.findByHash(hash).isPresent()) {
                continue;
            }

            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String extension = getExtension(multipartFile.getOriginalFilename());
            String fileName = hash + extension;
            Path filePath = dirPath.resolve(fileName);
            Files.write(filePath, multipartFile.getBytes());

            File file = new File();
            file.setFilename(multipartFile.getOriginalFilename());
            file.setPath(filePath.toString());
            file.setHash(hash);
            file.setSize(multipartFile.getSize());
            file.setUser(user);

            File saved = fileRepository.save(file);

            ids.add(saved.getId());
            filenames.add(saved.getFilename());
            sizes.add(Math.round((saved.getSize() / 1_048_576.0) * 100.0) / 100.0);
        }

        if (ids.isEmpty()) {
            throw new InvalidRequestException("Documents is already uploaded or invalid.");
        }

        return new UploadFileResponseDto(
            "Upload finished with success.", 
            ids, 
            filenames, 
            sizes, 
            ids.size(), 
            true
        );
    }

    public List<FileListResponseDto> listFiles(User user) {

        List<File> files = fileRepository.findByUser(user);
        
        return files.stream().map(f -> new FileListResponseDto(
            f.getId(),
            f.getFilename(),
            f.getHash(),
            f.getSize(),
            f.getPath()
        )).toList();
    }

    public DeleteFileResponseDto deleteFile(UUID fileId, User user) throws IOException {
        File file = fileRepository.findByIdAndUser(fileId, user)
            .orElseThrow(() -> new NotFoundException("File not found"));

        Files.deleteIfExists(Paths.get(file.getPath()));
        fileRepository.delete(file);

        return new DeleteFileResponseDto(
            "File deleted with success", 
            file.getId(),
            file.getFilename(),
            file.getHash(),
            file.getSize(),
            file.getPath()
        );
    }

    private String generateHash(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
