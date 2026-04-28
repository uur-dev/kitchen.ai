package com.br3akPoint.auth_service.constant;

public class ValidationConstant {
    public static final String Password_Regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";
    public static final int Password_Min_Size = 8;

    ///Errors
    public static final String Invalid_Email = "validation_error_invalid_email";
    public static final String Invalid_Password = "validation_error_invalid_password_format";
    public static final String Invalid_Password_Length = "validation_error_invalid_password_lenght";
    public static final String Password_Required = "validation_error_password_required";
    public static final String Email_Required = "validation_error_email_required";
}
