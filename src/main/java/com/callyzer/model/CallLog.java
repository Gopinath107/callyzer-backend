package com.callyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "call_logs", indexes = {
        @Index(name = "idx_device_id", columnList = "deviceId"),
        @Index(name = "idx_timestamp", columnList = "callTimestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CallLog {

    public enum CallType {
        INCOMING, OUTGOING, MISSED, REJECTED, UNKNOWN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallType callType;

    private Long duration;

    @Column(nullable = false)
    private Long callTimestamp;

    private String contactName;

    private Instant receivedAt;

    @PrePersist
    public void prePersist() {
        this.receivedAt = Instant.now();
    }
}
