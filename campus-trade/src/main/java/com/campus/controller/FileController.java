package com.campus.controller;
import com.campus.common.result.Result;
import com.campus.service.impl.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/image")
    public Result<Void> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return Result.success(fileService.uploadImage(file));
    }
}
