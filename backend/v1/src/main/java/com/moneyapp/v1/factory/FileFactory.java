package com.moneyapp.v1.factory;

import java.nio.file.Path;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.moneyapp.v1.model.File;
import com.moneyapp.v1.model.User;

@Component
public class FileFactory {
    
    public File create(MultipartFile multipartFile, String hash, Path filePath, User user) {
        File file = new File();
        file.setFilename(multipartFile.getOriginalFilename());
        file.setPath(filePath.toString());
        file.setHash(hash);
        file.setSize(multipartFile.getSize());
        file.setUser(user);
        return file;
    }
}
