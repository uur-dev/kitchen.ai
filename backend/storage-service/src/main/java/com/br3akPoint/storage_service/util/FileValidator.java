package com.br3akPoint.storage_service.util;

import com.br3akPoint.storage_service.constant.FileError;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileValidator {

    private static final long MAX_FILE_SIZE_BYTES = 3L * 1024 * 1024; // 3 MB

    /**
     * MIME types for common image formats and voice message audio formats.
     * Audio formats cover:
     * - iOS voice messages: m4a (audio/mp4, audio/x-m4a), aac
     * - Android voice messages: ogg/opus (audio/ogg), 3gp (audio/3gpp)
     * - Common cross-platform: mp3, wav, webm
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            // Image formats
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/heic",
            "image/heif",

            // Audio formats (voice messages)
            "audio/mp4",        // .m4a — iOS default voice message format
            "audio/x-m4a",      // .m4a — alternate MIME for iOS
            "audio/aac",        // .aac — iOS fallback
            "audio/ogg",        // .ogg / .opus — Android (WhatsApp, Telegram, etc.)
            "audio/opus",       // .opus — modern Android
            "audio/3gpp",       // .3gp  — older Android default
            "audio/mpeg",       // .mp3  — common cross-platform
            "audio/wav",        // .wav
            "audio/webm"        // .webm — browser-based recorders
    );

    private FileValidator() {
        // Utility class — do not instantiate
    }

    /**
     * Validates a {@link MultipartFile} against size and content-type constraints.
     *
     * @param file the uploaded file to validate
     * @return an unmodifiable list of human-readable error messages;
     *         empty if the file passes all checks
     */
    public static List<String> validate(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            errors.add(FileError.Empty_File.getMessage());
            return List.copyOf(errors); // No further checks make sense
        }

        validateSize(file, errors);
        validateContentType(file, errors);

        return List.copyOf(errors);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void validateSize(MultipartFile file, List<String> errors) {
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            errors.add(FileError.Large_File.getMessage());
        }
    }

    private static void validateContentType(MultipartFile file, List<String> errors) {
        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            errors.add(FileError.Invalid_File_Type.getMessage());
            return;
        }

        // Normalise: strip parameters such as "; codecs=opus"
        String normalised = contentType.split(";")[0].trim().toLowerCase();

        if (!ALLOWED_MIME_TYPES.contains(normalised)) {
            errors.add(FileError.Invalid_File_Type.getMessage());
        }
    }
}