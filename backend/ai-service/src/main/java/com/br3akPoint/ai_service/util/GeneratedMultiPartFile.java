package com.br3akPoint.ai_service.util;

import org.jspecify.annotations.NonNull;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.util.Base64;

public class GeneratedMultiPartFile implements MultipartFile {

    private final byte[] content;
    private final String fileName;
    private final String contentType;

    // Constructor for byte array
    public GeneratedMultiPartFile(byte[] content, String fileName, String contentType) {
        this.content = content;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    // Constructor for Base64 string
    public GeneratedMultiPartFile(String base64, String fileName, String contentType) {
        String pureBase64 = base64.contains(",") ? base64.split(",")[1] : base64;
        this.content = Base64.getDecoder().decode(pureBase64);
        this.fileName = fileName;
        this.contentType = contentType;
    }

    @Override
    public @NonNull String getName() {
        return "file";
    }

    @Override
    public String getOriginalFilename() {
        return fileName;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte @NonNull [] getBytes() {
        return content;
    }

    @Override
    public @NonNull InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException, IllegalStateException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
    }
}