package com.axvorquil.reputeiq.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reputeiq_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputeProfile {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private String handle;

    private String headline;
    private String summary;
    private String location;

    @Builder.Default
    private int reputeScore = 0;

    @Builder.Default
    private String scoreBand = "EMERGING";

    @Builder.Default
    private int achievementCount = 0;

    @Builder.Default
    private int endorsementCount = 0;

    @Builder.Default
    private String profileVisibility = "PUBLIC";

    private LocalDateTime scoreLastComputedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
