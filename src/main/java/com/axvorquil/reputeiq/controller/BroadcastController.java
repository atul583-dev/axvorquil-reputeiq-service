package com.axvorquil.reputeiq.controller;

import com.axvorquil.reputeiq.model.Broadcast;
import com.axvorquil.reputeiq.service.AiService;
import com.axvorquil.reputeiq.service.BroadcastService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reputeiq/broadcasts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class BroadcastController {

    private final BroadcastService broadcastService;
    private final AiService aiService;

    @GetMapping
    public ResponseEntity<List<Broadcast>> list(@RequestParam String profileId) {
        return ResponseEntity.ok(broadcastService.list(profileId));
    }

    @PostMapping
    public ResponseEntity<Broadcast> create(
            @RequestHeader(value = "X-Profile-ID", defaultValue = "") String profileIdHeader,
            @RequestBody Map<String, Object> body) {
        String profileId = body.containsKey("profileId") ? (String) body.get("profileId") : profileIdHeader;
        return ResponseEntity.ok(broadcastService.create(profileId, body));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<Broadcast> send(
            @RequestParam String profileId,
            @PathVariable String id) {
        return ResponseEntity.ok(broadcastService.send(profileId, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestParam String profileId,
            @PathVariable String id) {
        broadcastService.delete(profileId, id);
        return ResponseEntity.noContent().build();
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/draft")
    public ResponseEntity<Map<String, String>> draft(@RequestBody Map<String, Object> body) {
        List<String> achievementIds = body.containsKey("achievementIds")
                ? (List<String>) body.get("achievementIds")
                : new ArrayList<>();
        String tone = (String) body.getOrDefault("tone", "PROFESSIONAL");
        String senderName = (String) body.getOrDefault("senderName", "");

        String messageText = aiService.generateBroadcastDraft(achievementIds, tone, senderName);
        return ResponseEntity.ok(Map.of("messageText", messageText));
    }
}
