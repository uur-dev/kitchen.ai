package com.br3akPoint.recipe_service.repository;

import com.br3akPoint.recipe_service.entity.RecipeCuisine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCuisineRepository extends JpaRepository<RecipeCuisine, Long> {
}
