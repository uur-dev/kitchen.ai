package com.br3akPoint.recipe_service.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ServerError {
    Recipe_RequestId_Not_Found("server_error_recipe_request_id_not_found"),
    Recipe_Not_Found("server_error_recipe_not_found"),
    Recipe_Exist_Wrt_Request_Id("server_error_recipe_exist_wrt_request_id");

    private final String message;
}
