package com.br3akPoint.ai_service.util;

import org.jspecify.annotations.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public UrlMultipartFile(String fileUrl) throws IOException {
        try {
            URL rawUrl = new URL(fileUrl);
            // URI constructor auto-encodes the path (spaces → %20)
            URI uri = new URI(
                    rawUrl.getProtocol(),
                    rawUrl.getAuthority(),
                    rawUrl.getPath(),
                    rawUrl.getQuery(),
                    null
            );
            URL url = new URL(encodeUrl(fileUrl));

            this.originalFilename = rawUrl.getPath().substring(rawUrl.getPath().lastIndexOf('/') + 1);
            this.name = "file";
            this.contentType = URLConnection.guessContentTypeFromName(originalFilename);

            try (InputStream inputStream = url.openStream()) {
                this.content = inputStream.readAllBytes();
            }

        } catch (URISyntaxException e) {
            throw new IOException("Invalid URL: " + fileUrl, e);
        }
    }

    private String encodeUrl(String fileUrl) throws IOException{
        String[] parts = fileUrl.split("\\?", 2);
        String query = parts.length > 1 ? "?" + parts[1] : "";

        int lastSlash = parts[0].lastIndexOf('/');
        String base = parts[0].substring(0, lastSlash + 1);
        String filename = parts[0].substring(lastSlash + 1);

        // Already percent-encoded parts stay intact, rest gets encoded
        String encodedFilename = URLEncoder.encode(
                URLDecoder.decode(filename, StandardCharsets.UTF_8), // decode first to avoid double-encoding
                StandardCharsets.UTF_8
        ).replace("+", "%20");

        return base + encodedFilename + query;
    }

    @Override @NonNull
    public String getName() { return name; }

    @Override
    public String getOriginalFilename() { return originalFilename; }

    @Override
    public String getContentType() { return contentType; }

    @Override
    public boolean isEmpty() { return content.length == 0; }

    @Override
    public long getSize() { return content.length; }

    @Override @NonNull
    public byte[] getBytes() { return content; }

    @Override @NonNull
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(@NonNull File dest) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(content);
        }
    }
}