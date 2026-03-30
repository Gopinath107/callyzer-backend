package com.callyzer.model;

import lombok.*;

import java.time.Instant;
import java.util.List;

public class Dto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CallEventRequest {
        private String deviceId;
        private String phoneNumber;
        private String callType;
        private Long duration;
        private Long timestamp;
        private String contactName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CallEventResponse {
        private Long id;
        private String deviceId;
        private String phoneNumber;
        private String callType;
        private Long duration;
        private Long callTimestamp;
        private String contactName;
        private Instant receivedAt;

        public static CallEventResponse from(CallLog callLog) {
            return CallEventResponse.builder()
                    .id(callLog.getId())
                    .deviceId(callLog.getDeviceId())
                    .phoneNumber(callLog.getPhoneNumber())
                    .callType(callLog.getCallType().name())
                    .duration(callLog.getDuration())
                    .callTimestamp(callLog.getCallTimestamp())
                    .contactName(callLog.getContactName())
                    .receivedAt(callLog.getReceivedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatsResponse {
        private long totalCalls;
        private long incomingCalls;
        private long outgoingCalls;
        private long missedCalls;
        private double avgDurationSeconds;
        private List<DeviceInfo> devices;

        // NEW: Today's analytics
        private long todayTotalCalls;
        private long todayIncomingCalls;
        private long todayOutgoingCalls;
        private long todayMissedCalls;
        private long todayTalkTimeSeconds;   // Total seconds spoken today
        private long totalTalkTimeSeconds;   // Total seconds spoken all time
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeviceInfo {
        private String deviceId;
        private long callCount;
    }
}
