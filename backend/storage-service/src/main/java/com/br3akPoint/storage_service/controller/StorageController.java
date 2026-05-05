package com.br3akPoint.storage_service.controller;

import com.br3akPoint.storage_service.service.FirebaseStorageService;
import com.br3akPoint.storage_service.util.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import response.ApiResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/storage")
public class StorageController {

    private final FirebaseStorageService storageService;

    @Autowired
    public StorageController(FirebaseStorageService service) {
        this.storageService = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<?>> upload(MultipartFile file) throws IOException {

        List<String> errors = FileValidator.validate(file);
        if(errors.isEmpty()) {
            //no error
            String url = storageService.uploadFile(file);
            return ResponseEntity.ok(ApiResponse.responseData(Map.of("url", url)));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, errors));
        }
    }
}
