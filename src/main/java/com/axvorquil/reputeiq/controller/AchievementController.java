package com.axvorquil.reputeiq.controller;

import com.axvorquil.reputeiq.model.Achievement;
import com.axvorquil.reputeiq.service.AchievementService;
import com.axvorquil.reputeiq.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reputeiq/achievements")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final AiService aiService;

    @GetMapping
    public ResponseEntity<List<Achievement>> list(@RequestParam String profileId) {
        return ResponseEntity.ok(achievementService.list(profileId));
    }

    @PostMapping
    public ResponseEntity<Achievement> create(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestHeader(value = "X-Profile-ID", defaultValue = "") String profileId,
            @RequestBody Map<String, Object> body) {
        String pid = body.containsKey("profileId") ? (String) body.get("profileId") : profileId;
        return ResponseEntity.ok(achievementService.create(tenantId, pid, body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Achievement> update(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String profileId,
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(achievementService.update(tenantId, profileId, id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String profileId,
            @PathVariable String id) {
        achievementService.delete(tenantId, profileId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/{profileId}")
    public ResponseEntity<List<Achievement>> listPublic(@PathVariable String profileId) {
        return ResponseEntity.ok(achievementService.listPublic(profileId));
    }

    @PostMapping("/enrich")
    public ResponseEntity<Map<String, Object>> enrich(@RequestBody Map<String, Object> body) {
        String rawInput = (String) body.getOrDefault("rawInput", "");
        String category = (String) body.getOrDefault("category", "CAREER");
        return ResponseEntity.ok(aiService.enrichAchievement(rawInput, category));
    }
}
