package com.defecttracker.service.impl;

import com.defecttracker.exception.BadRequestException;
import com.defecttracker.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class StorageServiceImpl implements StorageService {

    private final Path fileStorageLocation;

    public StorageServiceImpl(@Value("${app.storage.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (originalFileName.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence " + originalFileName);
            }

            String fileExtension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                fileExtension = originalFileName.substring(i);
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + uniqueFileName;
        } catch (IOException ex) {
            log.error("Could not store file " + originalFileName, ex);
            throw new RuntimeException("Could not store file " + originalFileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileUrlOrName) {
        if (fileUrlOrName == null || fileUrlOrName.trim().isEmpty()) {
            return;
        }
        try {
            String fileName = fileUrlOrName.replace("/uploads/", "");
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.warn("Could not delete file: " + fileUrlOrName, ex);
        }
    }
}
