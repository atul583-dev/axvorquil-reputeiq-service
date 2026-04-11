package com.axvorquil.reputeiq.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "reputeiq_achievements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String profileId;

    private String userId;

    private String title;
    private String rawInput;
    private String narrative;
    private String impactStatement;

    private String category;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    private String visibility = "PUBLIC";

    @Builder.Default
    private double aiQualityScore = 0.0;

    @Builder.Default
    private int endorsementCount = 0;

    @Builder.Default
    private String sourceType = "MANUAL";

    private String achievedAt;

    @CreatedDate
    private LocalDateTime createdAt;
}
