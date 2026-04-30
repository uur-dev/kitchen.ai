package com.br3akPoint.recipe_service.entity;

import com.br3akPoint.entity.BaseEntity;
import com.br3akPoint.recipe_service.constant.RecipeRequestType;
import com.br3akPoint.recipe_service.constant.RecipeStatus;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "recipe_request")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RecipeRequest extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecipeRequestType type; // Consider using an Enum here

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RecipeStatus status = RecipeStatus.processing;

    @Column(name = "fail_reason", columnDefinition = "TEXT")
    private String failReason;
}
