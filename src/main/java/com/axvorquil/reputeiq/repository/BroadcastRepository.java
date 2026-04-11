package com.axvorquil.reputeiq.repository;

import com.axvorquil.reputeiq.model.Broadcast;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BroadcastRepository extends MongoRepository<Broadcast, String> {
    List<Broadcast> findByProfileIdOrderByCreatedAtDesc(String profileId);
}
