package com.br3akPoint.ai_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import response.ApiResponse;

import java.util.Map;

@FeignClient(name = "storage-service")
public interface StorageClient {

    @PostMapping(value = "/storage/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<Map<String, String>> upload(@RequestPart("file") MultipartFile file);
}
