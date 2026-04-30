package com.br3akPoint.recipe_service.repository;

import com.br3akPoint.recipe_service.entity.RecipeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRequestRepo extends JpaRepository<RecipeRequest, Long> {

    Page<RecipeRequest> findByUserId(Long userId, Pageable pageable);
}
