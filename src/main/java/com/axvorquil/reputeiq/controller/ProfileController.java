package com.axvorquil.reputeiq.controller;

import com.axvorquil.reputeiq.model.ReputeProfile;
import com.axvorquil.reputeiq.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reputeiq/profiles")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ReputeProfile> getOrCreate(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestHeader(value = "X-User-Id", defaultValue = "guest") String userId) {
        return ResponseEntity.ok(profileService.getOrCreateProfile(tenantId, userId));
    }

    @PutMapping("/me")
    public ResponseEntity<ReputeProfile> update(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestHeader(value = "X-User-Id", defaultValue = "guest") String userId,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(profileService.updateProfile(tenantId, userId, body));
    }

    @GetMapping("/public/{handle}")
    public ResponseEntity<ReputeProfile> getPublicProfile(@PathVariable String handle) {
        return profileService.getPublicProfile(handle)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/me/score")
    public ResponseEntity<ReputeProfile> computeScore(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestHeader(value = "X-User-Id", defaultValue = "guest") String userId) {
        return ResponseEntity.ok(profileService.computeScore(tenantId, userId));
    }
}
