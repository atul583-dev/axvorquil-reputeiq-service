package com.axvorquil.reputeiq.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reputeiq_endorsements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Endorsement {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String achievementId;

    private String requestorProfileId;

    private String endorserName;
    private String endorserPhone;
    private String endorserEmail;

    private String endorserRelationship;

    @Indexed
    private String requestToken;

    private String requestMessage;

    @Builder.Default
    private String status = "REQUESTED";

    private int rating;
    private String testimonialText;

    @Builder.Default
    private boolean endorserVerifiedOtp = false;

    @CreatedDate
    private LocalDateTime requestedAt;

    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
}
