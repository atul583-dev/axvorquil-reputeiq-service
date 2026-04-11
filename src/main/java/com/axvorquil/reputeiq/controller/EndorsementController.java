package com.axvorquil.reputeiq.controller;

import com.axvorquil.reputeiq.model.Endorsement;
import com.axvorquil.reputeiq.service.EndorsementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reputeiq/endorsements")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EndorsementController {

    private final EndorsementService endorsementService;

    @GetMapping
    public ResponseEntity<List<Endorsement>> listByProfile(@RequestParam String profileId) {
        return ResponseEntity.ok(endorsementService.listByProfile(profileId));
    }

    @PostMapping
    public ResponseEntity<Endorsement> createRequest(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestHeader(value = "X-Profile-ID", defaultValue = "") String profileIdHeader,
            @RequestBody Map<String, Object> body) {
        String profileId = body.containsKey("profileId") ? (String) body.get("profileId") : profileIdHeader;
        return ResponseEntity.ok(endorsementService.createRequest(tenantId, profileId, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @RequestParam String profileId,
            @PathVariable String id) {
        endorsementService.cancelRequest(profileId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public/{token}")
    public ResponseEntity<Endorsement> getByToken(@PathVariable String token) {
        Endorsement endorsement = endorsementService.getByToken(token);
        endorsementService.markViewed(token);
        return ResponseEntity.ok(endorsement);
    }

    @PostMapping("/public/{token}/submit")
    public ResponseEntity<Endorsement> submitEndorsement(
            @PathVariable String token,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(endorsementService.submitEndorsement(token, body));
    }
}
