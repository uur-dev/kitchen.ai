package com.br3akPoint.ai_service.repository;

import com.br3akPoint.ai_service.data.RecipeProcessingTracker;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackerRepository extends MongoRepository<RecipeProcessingTracker, String> {
}
