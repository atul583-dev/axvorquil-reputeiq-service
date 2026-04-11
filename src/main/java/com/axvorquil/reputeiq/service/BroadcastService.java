package com.axvorquil.reputeiq.service;

import com.axvorquil.reputeiq.model.Achievement;
import com.axvorquil.reputeiq.model.Broadcast;
import com.axvorquil.reputeiq.repository.AchievementRepository;
import com.axvorquil.reputeiq.repository.BroadcastRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BroadcastService {

    private final BroadcastRepository repo;
    private final AchievementRepository achRepo;
    private final AiService aiService;

    public List<Broadcast> list(String profileId) {
        return repo.findByProfileIdOrderByCreatedAtDesc(profileId);
    }

    @SuppressWarnings("unchecked")
    public Broadcast create(String profileId, Map<String, Object> body) {
        List<String> achievementIds = body.containsKey("achievementIds")
                ? (List<String>) body.get("achievementIds")
                : new ArrayList<>();

        String messageTone = (String) body.getOrDefault("messageTone", "PROFESSIONAL");
        String senderName = (String) body.getOrDefault("senderName", "");
        String messageText = (String) body.get("messageText");

        // Load achievement titles for AI draft if messageText not provided
        if (messageText == null || messageText.isBlank()) {
            List<String> titles = achievementIds.stream()
                    .map(id -> achRepo.findById(id).map(Achievement::getTitle).orElse("achievement"))
                    .collect(Collectors.toList());
            messageText = aiService.generateBroadcastDraft(titles, messageTone, senderName);
        }

        Object recipientsObj = body.get("recipients");
        int recipientCount = 0;
        if (recipientsObj instanceof Number) {
            recipientCount = ((Number) recipientsObj).intValue();
        } else if (recipientsObj instanceof List) {
            recipientCount = ((List<?>) recipientsObj).size();
        }

        Broadcast broadcast = Broadcast.builder()
                .profileId(profileId)
                .tenantId((String) body.get("tenantId"))
                .achievementIds(achievementIds)
                .messageText(messageText)
                .messageTone(messageTone)
                .recipientCount(recipientCount)
                .status("DRAFT")
                .build();

        return repo.save(broadcast);
    }

    public Broadcast send(String profileId, String id) {
        Broadcast broadcast = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Broadcast not found: " + id));
        if (!"DRAFT".equals(broadcast.getStatus())) {
            throw new RuntimeException("Broadcast is not in DRAFT status: " + broadcast.getStatus());
        }
        broadcast.setStatus("SENT");
        broadcast.setSentAt(LocalDateTime.now());
        return repo.save(broadcast);
    }

    public void delete(String profileId, String id) {
        Broadcast broadcast = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Broadcast not found: " + id));
        if (!"DRAFT".equals(broadcast.getStatus())) {
            throw new RuntimeException("Only DRAFT broadcasts can be deleted");
        }
        repo.deleteById(id);
    }
}
