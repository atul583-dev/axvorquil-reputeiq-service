package com.axvorquil.reputeiq.controller;

import com.axvorquil.reputeiq.model.InnerCircleContact;
import com.axvorquil.reputeiq.service.InnerCircleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reputeiq/circle")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class InnerCircleController {

    private final InnerCircleService innerCircleService;

    @GetMapping
    public ResponseEntity<List<InnerCircleContact>> list(@RequestParam String profileId) {
        return ResponseEntity.ok(innerCircleService.list(profileId));
    }

    @PostMapping
    public ResponseEntity<InnerCircleContact> add(
            @RequestHeader(value = "X-Profile-ID", defaultValue = "") String profileIdHeader,
            @RequestBody Map<String, Object> body) {
        String profileId = body.containsKey("profileId") ? (String) body.get("profileId") : profileIdHeader;
        return ResponseEntity.ok(innerCircleService.add(profileId, body));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InnerCircleContact> update(
            @RequestParam String profileId,
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(innerCircleService.update(profileId, id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(
            @RequestParam String profileId,
            @PathVariable String id) {
        innerCircleService.remove(profileId, id);
        return ResponseEntity.noContent().build();
    }
}
