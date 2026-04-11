package com.axvorquil.reputeiq.repository;

import com.axvorquil.reputeiq.model.Achievement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends MongoRepository<Achievement, String> {
    List<Achievement> findByProfileIdOrderByCreatedAtDesc(String profileId);
    List<Achievement> findByProfileIdAndVisibilityOrderByCreatedAtDesc(String profileId, String visibility);
    long countByProfileId(String profileId);
}
