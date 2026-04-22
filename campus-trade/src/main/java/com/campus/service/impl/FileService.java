package com.campus.service.impl;
import com.campus.common.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    @Value("${file.upload-dir:./uploads}") private String uploadDir;
    @Value("${file.access-url:http://localhost:8080/files}") private String accessUrl;
    private static final Set<String> ALLOWED = Set.of("image/jpeg","image/png","image/webp","image/gif");

    public String uploadImage(MultipartFile file) throws IOException {
        String ct = file.getContentType();
        if (ct == null || !ALLOWED.contains(ct)) throw BusinessException.of("仅支持 JPG/PNG/WEBP/GIF");
        if (file.getSize() > 10 * 1024 * 1024) throw BusinessException.of("图片不能超过 10MB");
        String ext  = ct.split("/")[1];
        String dir  = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String name = UUID.randomUUID() + "." + ext;
        Path target = Paths.get(uploadDir, dir, name);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return accessUrl + "/" + dir + "/" + name;
    }
}
