package com.br3akPoint.auth_service.constant;

public class ServerErrors {
    public static final String Missing_App_ID = "server_error_missing_x-app-id";
    public static final String Missing_Signatures = "server_error_missing_x-signatures";
    public static final String Invalid_App_ID = "server_error_invalid_app_id";
    public static final String Invalid_Signatures = "server_error_invalid_signatures";
    public static final String Signature_Expired = "server_error_signatures_expired";

    ///auth errors
    public static final String User_Already_Exist = "server_error_user_already_exist";
    public static final String Invalid_User_or_Password = "server_error_invalid_user_or_password";
}
