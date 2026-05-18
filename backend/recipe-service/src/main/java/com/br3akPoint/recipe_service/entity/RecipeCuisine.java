package com.br3akPoint.recipe_service.entity;

import entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "recipe_cuisine",
        indexes = {
                @Index(name = "idx_recipe_cuisine_value", columnList = "value")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeCuisine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value", nullable = false, unique = true, length = 100)
    private String value;

    @Column(name = "country_localized_key", nullable = false, length = 255)
    private String countryLocalizedKey;

    @Column(name = "label_localized_key", nullable = false, length = 255)
    private String labelLocalizedKey;

    @Column(name = "flag", nullable = true, length = 10)
    private String flag;
}
