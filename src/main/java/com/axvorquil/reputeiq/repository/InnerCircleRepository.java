package com.axvorquil.reputeiq.repository;

import com.axvorquil.reputeiq.model.InnerCircleContact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InnerCircleRepository extends MongoRepository<InnerCircleContact, String> {
    List<InnerCircleContact> findByProfileIdOrderByCreatedAtDesc(String profileId);
    List<InnerCircleContact> findByProfileIdAndOptedOutFalse(String profileId);
}
