package com.axvorquil.reputeiq.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "reputeiq_broadcasts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Broadcast {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String profileId;

    @Builder.Default
    private List<String> achievementIds = new ArrayList<>();

    private String messageText;

    @Builder.Default
    private String messageTone = "PROFESSIONAL";

    private int recipientCount;

    @Builder.Default
    private String status = "DRAFT";

    @Builder.Default
    private int openCount = 0;

    @Builder.Default
    private int clickCount = 0;

    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;

    @CreatedDate
    private LocalDateTime createdAt;
}
