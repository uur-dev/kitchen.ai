package com.br3akPoint.recipe_service.repository;

import com.br3akPoint.recipe_service.entity.Recipe;
import com.br3akPoint.recipe_service.entity.RecipeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Page<Recipe> findByUserId(Long userId, Pageable pageable);
    Boolean existsByRequestId(Long requestId);
}
