package com.br3akPoint.storage_service.service;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@Service
public class FirebaseStorageService {
    private final Bucket firebaseBucket;

    @Value("${firebase.storage.folder}")
    private String folderName;

    @Autowired
    public FirebaseStorageService(Bucket bucket) {
        this.firebaseBucket = bucket;
    }

    public String uploadFile(MultipartFile file) throws IOException {
        String fileName = folderName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        Blob blob = firebaseBucket.create(fileName, file.getBytes(), file.getContentType());

        // Make this object publicly readable
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        String encodedPath = fileName.replace("/", "%2F");

        return "https://firebasestorage.googleapis.com/v0/b/"
                + firebaseBucket.getName() + "/o/" + encodedPath + "?alt=media";
    }
}
