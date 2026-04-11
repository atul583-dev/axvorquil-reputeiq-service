package com.axvorquil.reputeiq.repository;

import com.axvorquil.reputeiq.model.Endorsement;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EndorsementRepository extends MongoRepository<Endorsement, String> {
    List<Endorsement> findByRequestorProfileIdOrderByRequestedAtDesc(String profileId);
    List<Endorsement> findByAchievementIdOrderByRequestedAtDesc(String achievementId);
    Optional<Endorsement> findByRequestToken(String token);
    long countByRequestorProfileIdAndStatus(String profileId, String status);
}
