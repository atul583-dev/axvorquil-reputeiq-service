package com.axvorquil.reputeiq.service;

import com.axvorquil.reputeiq.model.Achievement;
import com.axvorquil.reputeiq.model.Endorsement;
import com.axvorquil.reputeiq.repository.AchievementRepository;
import com.axvorquil.reputeiq.repository.EndorsementRepository;
import com.axvorquil.reputeiq.repository.ReputeProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EndorsementService {

    private final EndorsementRepository repo;
    private final AchievementRepository achRepo;
    private final ReputeProfileRepository profileRepo;
    private final AiService aiService;
    private final ProfileService profileService;

    public Endorsement createRequest(String tenantId, String profileId, Map<String, Object> body) {
        String achievementId = (String) body.get("achievementId");
        String endorserName = (String) body.get("endorserName");
        String endorserPhone = (String) body.get("endorserPhone");
        String endorserEmail = (String) body.get("endorserEmail");
        String endorserRelationship = (String) body.get("endorserRelationship");

        String requestToken = UUID.randomUUID().toString().replace("-", "");

        // Load achievement title for context
        String achievementTitle = achRepo.findById(achievementId)
                .map(Achievement::getTitle)
                .orElse("achievement");

        String requestMessage = aiService.generateEndorsementAsk(achievementTitle, endorserName, endorserRelationship);

        Endorsement endorsement = Endorsement.builder()
                .tenantId(tenantId)
                .achievementId(achievementId)
                .requestorProfileId(profileId)
                .endorserName(endorserName)
                .endorserPhone(endorserPhone)
                .endorserEmail(endorserEmail)
                .endorserRelationship(endorserRelationship)
                .requestToken(requestToken)
                .requestMessage(requestMessage)
                .status("REQUESTED")
                .expiresAt(LocalDateTime.now().plusDays(14))
                .build();

        return repo.save(endorsement);
    }

    public List<Endorsement> listByProfile(String profileId) {
        return repo.findByRequestorProfileIdOrderByRequestedAtDesc(profileId);
    }

    public Endorsement getByToken(String token) {
        Endorsement endorsement = repo.findByRequestToken(token)
                .orElseThrow(() -> new RuntimeException("Endorsement not found for token: " + token));

        if (endorsement.getExpiresAt() != null && LocalDateTime.now().isAfter(endorsement.getExpiresAt())
                && !"COMPLETED".equals(endorsement.getStatus()) && !"EXPIRED".equals(endorsement.getStatus())) {
            endorsement.setStatus("EXPIRED");
            repo.save(endorsement);
        }

        return endorsement;
    }

    public Endorsement submitEndorsement(String token, Map<String, Object> body) {
        Endorsement endorsement = repo.findByRequestToken(token)
                .orElseThrow(() -> new RuntimeException("Endorsement not found for token: " + token));

        if (!"REQUESTED".equals(endorsement.getStatus()) && !"VIEWED".equals(endorsement.getStatus())) {
            throw new RuntimeException("Endorsement cannot be submitted in status: " + endorsement.getStatus());
        }

        Object ratingObj = body.get("rating");
        if (ratingObj instanceof Number) {
            endorsement.setRating(((Number) ratingObj).intValue());
        }
        endorsement.setTestimonialText((String) body.get("testimonialText"));
        endorsement.setEndorserVerifiedOtp(true);
        endorsement.setStatus("COMPLETED");
        endorsement.setCompletedAt(LocalDateTime.now());
        Endorsement saved = repo.save(endorsement);

        // Increment achievement endorsement count
        achRepo.findById(endorsement.getAchievementId()).ifPresent(ach -> {
            ach.setEndorsementCount(ach.getEndorsementCount() + 1);
            achRepo.save(ach);
        });

        // Increment profile endorsement count and recompute score
        profileRepo.findById(endorsement.getRequestorProfileId()).ifPresent(profile -> {
            profile.setEndorsementCount(profile.getEndorsementCount() + 1);
            profileRepo.save(profile);
            try {
                profileService.computeScore(endorsement.getTenantId(), profile.getUserId());
            } catch (Exception e) {
                log.warn("Score recompute failed after endorsement: {}", e.getMessage());
            }
        });

        return saved;
    }

    public Endorsement markViewed(String token) {
        Endorsement endorsement = repo.findByRequestToken(token)
                .orElseThrow(() -> new RuntimeException("Endorsement not found for token: " + token));
        if ("REQUESTED".equals(endorsement.getStatus())) {
            endorsement.setStatus("VIEWED");
            return repo.save(endorsement);
        }
        return endorsement;
    }

    public void cancelRequest(String profileId, String id) {
        Endorsement endorsement = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Endorsement not found: " + id));
        if (!profileId.equals(endorsement.getRequestorProfileId())) {
            throw new RuntimeException("Not authorized to cancel this endorsement request");
        }
        repo.deleteById(id);
    }
}
