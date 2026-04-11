package com.axvorquil.reputeiq.model;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reputeiq_inner_circle")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InnerCircleContact {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @Indexed
    private String profileId;

    private String contactName;
    private String contactPhone;
    private String contactEmail;

    private String relationship;

    @Builder.Default
    private boolean optedOut = false;

    @CreatedDate
    private LocalDateTime createdAt;
}
