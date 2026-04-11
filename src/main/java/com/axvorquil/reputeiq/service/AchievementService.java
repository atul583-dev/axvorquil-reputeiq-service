package com.axvorquil.reputeiq.service;

import com.axvorquil.reputeiq.model.Achievement;
import com.axvorquil.reputeiq.model.ReputeProfile;
import com.axvorquil.reputeiq.repository.AchievementRepository;
import com.axvorquil.reputeiq.repository.ReputeProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {

    private final AchievementRepository repo;
    private final ReputeProfileRepository profileRepo;
    private final AiService aiService;
    private final ProfileService profileService;

    public List<Achievement> list(String profileId) {
        return repo.findByProfileIdOrderByCreatedAtDesc(profileId);
    }

    public Achievement create(String tenantId, String profileId, Map<String, Object> body) {
        String rawInput = (String) body.getOrDefault("rawInput", "");
        String category = (String) body.getOrDefault("category", "CAREER");

        Map<String, Object> enriched = aiService.enrichAchievement(rawInput, category);

        Achievement achievement = Achievement.builder()
                .tenantId(tenantId)
                .profileId(profileId)
                .rawInput(rawInput)
                .category(category)
                .title((String) enriched.get("title"))
                .narrative((String) enriched.get("narrative"))
                .impactStatement((String) enriched.get("impactStatement"))
                .aiQualityScore(toDouble(enriched.get("aiQualityScore")))
                .achievedAt((String) body.get("achievedAt"))
                .sourceType("MANUAL")
                .build();

        Achievement saved = repo.save(achievement);

        // Increment count and recompute score
        profileRepo.findById(profileId).ifPresent(profile -> {
            profile.setAchievementCount(profile.getAchievementCount() + 1);
            profileRepo.save(profile);
        });

        // Try to compute score (best-effort, need tenantId+userId from profile)
        profileRepo.findById(profileId).ifPresent(profile -> {
            try {
                profileService.computeScore(tenantId, profile.getUserId());
            } catch (Exception e) {
                log.warn("Score computation failed after achievement create: {}", e.getMessage());
            }
        });

        return saved;
    }

    public Achievement update(String tenantId, String profileId, String id, Map<String, Object> body) {
        Achievement achievement = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Achievement not found: " + id));
        if (body.containsKey("title")) achievement.setTitle((String) body.get("title"));
        if (body.containsKey("narrative")) achievement.setNarrative((String) body.get("narrative"));
        if (body.containsKey("impactStatement")) achievement.setImpactStatement((String) body.get("impactStatement"));
        if (body.containsKey("visibility")) achievement.setVisibility((String) body.get("visibility"));
        if (body.containsKey("achievedAt")) achievement.setAchievedAt((String) body.get("achievedAt"));
        if (body.containsKey("category")) achievement.setCategory((String) body.get("category"));
        return repo.save(achievement);
    }

    public void delete(String tenantId, String profileId, String id) {
        repo.deleteById(id);
        profileRepo.findById(profileId).ifPresent(profile -> {
            int count = Math.max(0, profile.getAchievementCount() - 1);
            profile.setAchievementCount(count);
            profileRepo.save(profile);
            try {
                profileService.computeScore(tenantId, profile.getUserId());
            } catch (Exception e) {
                log.warn("Score computation failed after achievement delete: {}", e.getMessage());
            }
        });
    }

    public List<Achievement> listPublic(String profileId) {
        return repo.findByProfileIdAndVisibilityOrderByCreatedAtDesc(profileId, "PUBLIC");
    }

    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception e) { return 0.0; }
    }
}
