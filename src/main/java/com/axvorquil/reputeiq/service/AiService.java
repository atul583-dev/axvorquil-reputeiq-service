package com.axvorquil.reputeiq.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${azure-openai.endpoint}")
    private String azureEndpoint;

    @Value("${azure-openai.api-key}")
    private String apiKey;

    @Value("${azure-openai.deployment}")
    private String deployment;

    @Value("${azure-openai.api-version:2025-04-01-preview}")
    private String apiVersion;

    public AiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> enrichAchievement(String rawInput, String category) {
        String systemPrompt = "You are a professional reputation coach. Given a raw achievement input, generate a JSON response with these fields: " +
                "title (concise professional title, max 10 words), " +
                "narrative (3-4 sentences in first person, professional tone), " +
                "impactStatement (one sentence with measurable impact, start with a verb), " +
                "aiQualityScore (0.0-1.0 based on specificity of input). " +
                "Return ONLY valid JSON, no markdown.";
        String userMessage = "Category: " + category + "\nRaw input: " + rawInput;

        try {
            String response = callAzureOpenAI(systemPrompt, userMessage);
            JsonNode node = objectMapper.readTree(response);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("title", node.path("title").asText(rawInput));
            result.put("narrative", node.path("narrative").asText(""));
            result.put("impactStatement", node.path("impactStatement").asText(""));
            result.put("aiQualityScore", node.path("aiQualityScore").asDouble(0.5));
            return result;
        } catch (Exception e) {
            log.warn("enrichAchievement AI call failed: {}", e.getMessage());
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("title", rawInput);
            fallback.put("narrative", "");
            fallback.put("impactStatement", "");
            fallback.put("aiQualityScore", 0.5);
            return fallback;
        }
    }

    public String generateEndorsementAsk(String achievementTitle, String endorserName, String relationship) {
        String systemPrompt = "You are a professional reputation coach. Generate a friendly, personalized WhatsApp message (100-150 characters) asking the given person to endorse an achievement. Be warm and concise.";
        String userMessage = "Endorser: " + endorserName + " (" + relationship + ")\nAchievement: " + achievementTitle;

        try {
            return callAzureOpenAI(systemPrompt, userMessage);
        } catch (Exception e) {
            log.warn("generateEndorsementAsk AI call failed: {}", e.getMessage());
            return "Hi " + endorserName + ", would you be willing to endorse my achievement: " + achievementTitle + "? It would mean a lot!";
        }
    }

    public String generateBroadcastDraft(List<String> achievementTitles, String tone, String senderName) {
        String achievementsStr = String.join(", ", achievementTitles);
        String systemPrompt = "You are a professional reputation coach. Generate a WhatsApp/SMS broadcast message. Return ONLY the message text, no quotes or extra commentary.";
        String userMessage = "Generate a " + tone + " WhatsApp/SMS message (max 160 chars) for " + senderName +
                " to share these recent achievements with their network: " + achievementsStr +
                ". Be warm and human, not boastful.";

        try {
            return callAzureOpenAI(systemPrompt, userMessage);
        } catch (Exception e) {
            log.warn("generateBroadcastDraft AI call failed: {}", e.getMessage());
            String first = achievementTitles.isEmpty() ? "recent milestone" : achievementTitles.get(0);
            return "Hi! Just sharing a quick update on my recent achievements: " + first + ". Read more: [link]";
        }
    }

    private String callAzureOpenAI(String systemPrompt, String userMessage) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("messages", messages);
        body.put("max_completion_tokens", 1024);

        String url = azureEndpoint + "openai/deployments/" + deployment
                + "/chat/completions?api-version=" + apiVersion;

        ResponseEntity<String> resp = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        JsonNode root = objectMapper.readTree(resp.getBody());
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}
