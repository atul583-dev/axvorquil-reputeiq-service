package com.axvorquil.reputeiq.service;

import com.axvorquil.reputeiq.model.InnerCircleContact;
import com.axvorquil.reputeiq.repository.InnerCircleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InnerCircleService {

    private final InnerCircleRepository repo;

    public List<InnerCircleContact> list(String profileId) {
        return repo.findByProfileIdOrderByCreatedAtDesc(profileId);
    }

    public InnerCircleContact add(String profileId, Map<String, Object> body) {
        InnerCircleContact contact = InnerCircleContact.builder()
                .profileId(profileId)
                .tenantId((String) body.get("tenantId"))
                .contactName((String) body.get("contactName"))
                .contactPhone((String) body.get("contactPhone"))
                .contactEmail((String) body.get("contactEmail"))
                .relationship((String) body.get("relationship"))
                .build();
        return repo.save(contact);
    }

    public InnerCircleContact update(String profileId, String id, Map<String, Object> body) {
        InnerCircleContact contact = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contact not found: " + id));
        if (body.containsKey("contactName")) contact.setContactName((String) body.get("contactName"));
        if (body.containsKey("contactPhone")) contact.setContactPhone((String) body.get("contactPhone"));
        if (body.containsKey("contactEmail")) contact.setContactEmail((String) body.get("contactEmail"));
        if (body.containsKey("relationship")) contact.setRelationship((String) body.get("relationship"));
        return repo.save(contact);
    }

    public void remove(String profileId, String id) {
        repo.deleteById(id);
    }

    public List<InnerCircleContact> getActiveContacts(String profileId) {
        return repo.findByProfileIdAndOptedOutFalse(profileId);
    }
}
