package com.br3akPoint.storage_service.constant;

import lombok.Getter;

@Getter
public enum FileError {
    Empty_File("validation_error_empty_file"),
    Large_File("validation_error_file_size"),
    Invalid_File_Type("validation_error_invalid_file_type");

    private final String message;
    FileError(String errorMessage) {
        this.message = errorMessage;
    }

}
