package com.axvorquil.reputeiq.service;

import com.axvorquil.reputeiq.model.ReputeProfile;
import com.axvorquil.reputeiq.repository.AchievementRepository;
import com.axvorquil.reputeiq.repository.EndorsementRepository;
import com.axvorquil.reputeiq.repository.ReputeProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ReputeProfileRepository profileRepo;
    private final AchievementRepository achRepo;
    private final EndorsementRepository endorsRepo;

    @Cacheable(value = "profiles", key = "#tenantId + ':' + #userId")
    public ReputeProfile getOrCreateProfile(String tenantId, String userId) {
        return profileRepo.findByTenantIdAndUserId(tenantId, userId)
                .orElseGet(() -> {
                    String prefix = userId.length() >= 8 ? userId.substring(0, 8) : userId;
                    String randomHex = UUID.randomUUID().toString().replace("-", "").substring(0, 4);
                    String handle = prefix.toLowerCase() + "-" + randomHex;

                    ReputeProfile profile = ReputeProfile.builder()
                            .tenantId(tenantId)
                            .userId(userId)
                            .handle(handle)
                            .build();
                    return profileRepo.save(profile);
                });
    }

    @Cacheable(value = "profiles", key = "#tenantId + ':' + #userId")
    public ReputeProfile getProfile(String tenantId, String userId) {
        return profileRepo.findByTenantIdAndUserId(tenantId, userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
    }

    @CacheEvict(value = "profiles", key = "#tenantId + ':' + #userId")
    public ReputeProfile updateProfile(String tenantId, String userId, Map<String, Object> data) {
        ReputeProfile profile = getProfile(tenantId, userId);
        if (data.containsKey("headline")) profile.setHeadline((String) data.get("headline"));
        if (data.containsKey("summary")) profile.setSummary((String) data.get("summary"));
        if (data.containsKey("location")) profile.setLocation((String) data.get("location"));
        if (data.containsKey("handle")) profile.setHandle((String) data.get("handle"));
        if (data.containsKey("profileVisibility")) profile.setProfileVisibility((String) data.get("profileVisibility"));
        return profileRepo.save(profile);
    }

    public Optional<ReputeProfile> getPublicProfile(String handle) {
        return profileRepo.findByHandle(handle)
                .filter(p -> "PUBLIC".equals(p.getProfileVisibility()));
    }

    @CacheEvict(value = {"profiles", "score"}, key = "#tenantId + ':' + #userId")
    public ReputeProfile computeScore(String tenantId, String userId) {
        ReputeProfile profile = getProfile(tenantId, userId);

        int achievementCount = profile.getAchievementCount();
        int endorsementCount = profile.getEndorsementCount();

        int achievementScore = Math.min(30, achievementCount * 3);
        int endorsementScore = Math.min(40, endorsementCount * 8);
        int total = achievementScore + endorsementScore;

        String scoreBand;
        if (total < 20) scoreBand = "EMERGING";
        else if (total < 30) scoreBand = "CREDIBLE";
        else if (total < 50) scoreBand = "RECOGNIZED";
        else if (total < 65) scoreBand = "RESPECTED";
        else scoreBand = "DISTINGUISHED";

        profile.setReputeScore(total);
        profile.setScoreBand(scoreBand);
        profile.setScoreLastComputedAt(LocalDateTime.now());

        log.debug("Computed score for user {}: {} ({})", userId, total, scoreBand);
        return profileRepo.save(profile);
    }
}
