package com.br3akPoint.constant;

import lombok.Getter;

@Getter
public enum CommonServerErrors {
    MaxUploadSizeExceeded("sever_error_file_size_exceeded"),
    MethodNotAllowed("server_error_method_not_allowed");

    private final String message;
    CommonServerErrors(String errorMessage) {
        this.message = errorMessage;
    }

}
