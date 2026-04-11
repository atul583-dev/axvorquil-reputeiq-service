package com.axvorquil.reputeiq.repository;

import com.axvorquil.reputeiq.model.ReputeProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReputeProfileRepository extends MongoRepository<ReputeProfile, String> {
    Optional<ReputeProfile> findByTenantIdAndUserId(String tenantId, String userId);
    Optional<ReputeProfile> findByHandle(String handle);
    List<ReputeProfile> findByTenantIdOrderByReputeScoreDesc(String tenantId);
}
