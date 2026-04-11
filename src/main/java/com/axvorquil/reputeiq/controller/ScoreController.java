package com.axvorquil.reputeiq.controller;

import com.axvorquil.reputeiq.model.ReputeProfile;
import com.axvorquil.reputeiq.repository.ReputeProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reputeiq/score")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ScoreController {

    private final ReputeProfileRepository profileRepo;

    @GetMapping("/breakdown")
    public ResponseEntity<Map<String, Object>> breakdown(@RequestParam String profileId) {
        ReputeProfile profile = profileRepo.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found: " + profileId));

        int achievementCount = profile.getAchievementCount();
        int endorsementCount = profile.getEndorsementCount();
        int achievementScore = Math.min(30, achievementCount * 3);
        int endorsementScore = Math.min(40, endorsementCount * 8);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reputeScore", profile.getReputeScore());
        result.put("scoreBand", profile.getScoreBand());
        result.put("achievementScore", achievementScore);
        result.put("endorsementScore", endorsementScore);
        result.put("achievementCount", achievementCount);
        result.put("endorsementCount", endorsementCount);
        result.put("scoreLastComputedAt", profile.getScoreLastComputedAt());

        return ResponseEntity.ok(result);
    }
}
