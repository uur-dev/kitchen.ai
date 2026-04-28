package com.br3akPoint.recipe_service.controller;

import com.br3akPoint.util.UserContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RecipeController {

    @GetMapping("/testing")
    public String testApi() {
        String email = UserContext.getEmail();
        Long userId = UserContext.getUserId();
        return "from Recipe -> User Id:" + userId + " Email: " + email;
    }

}
