package com.br3akPoint.recipe_service.entity;

import com.br3akPoint.recipe_service.constant.RecipeRequestType;
import entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "recipe")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Recipe extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private RecipeRequest request;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", length = 20)
    private RecipeRequestType requestType;

    // Mapping PostgreSQL TEXT[]
    @Column(name = "steps", columnDefinition = "text[]")
    private List<String> steps;

    @Column(name = "instructions", columnDefinition = "text[]")
    private List<String> instructions;

    @Column(name = "ingredients", columnDefinition = "text[]")
    private List<String> ingredients;

    @Column(name = "rich_text_content", columnDefinition = "TEXT")
    private String richTextContent;
}
