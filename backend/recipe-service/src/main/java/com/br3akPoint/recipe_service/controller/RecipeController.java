package com.br3akPoint.recipe_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class RecipeController {

    @GetMapping("/testing")
    public String testApi() {
        return "This is Testing Api Calling from Recipe";
    }

}
