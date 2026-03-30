package com.callyzer.service;

import com.callyzer.model.CallLog;
import com.callyzer.model.Dto;
import com.callyzer.repository.CallLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CallLogService {

    private final CallLogRepository repository;

    public CallLog save(Dto.CallEventRequest request) {
        // Dedup check
        if (repository.existsByDeviceIdAndPhoneNumberAndCallTimestamp(
                request.getDeviceId(), request.getPhoneNumber(), request.getTimestamp())) {
            log.info("Duplicate call event ignored: device={}, number={}, timestamp={}",
                    request.getDeviceId(), request.getPhoneNumber(), request.getTimestamp());
            return null;
        }

        CallLog callLog = CallLog.builder()
                .deviceId(request.getDeviceId())
                .phoneNumber(request.getPhoneNumber())
                .callType(parseCallType(request.getCallType()))
                .duration(request.getDuration())
                .callTimestamp(request.getTimestamp())
                .contactName(request.getContactName())
                .build();

        CallLog saved = repository.save(callLog);
        log.info("Saved call event: id={}, device={}, number={}, type={}",
                saved.getId(), saved.getDeviceId(), saved.getPhoneNumber(), saved.getCallType());
        return saved;
    }

    public int saveBatch(List<Dto.CallEventRequest> requests) {
        int savedCount = 0;
        for (Dto.CallEventRequest request : requests) {
            CallLog saved = save(request);
            if (saved != null) {
                savedCount++;
            }
        }
        log.info("Batch import complete: saved {} of {} records", savedCount, requests.size());
        return savedCount;
    }

    public List<Dto.CallEventResponse> getAll(int page, int size) {
        Page<CallLog> callLogs = repository.findAllByOrderByCallTimestampDesc(PageRequest.of(page, size));
        return callLogs.getContent().stream()
                .map(Dto.CallEventResponse::from)
                .collect(Collectors.toList());
    }

    public List<Dto.CallEventResponse> getByDevice(String deviceId) {
        return repository.findByDeviceIdOrderByCallTimestampDesc(deviceId).stream()
                .map(Dto.CallEventResponse::from)
                .collect(Collectors.toList());
    }

    public Dto.StatsResponse getStats() {
        // All-time stats
        long totalCalls = repository.count();
        long incomingCalls = repository.countByCallType(CallLog.CallType.INCOMING);
        long outgoingCalls = repository.countByCallType(CallLog.CallType.OUTGOING);
        long missedCalls = repository.countByCallType(CallLog.CallType.MISSED);

        // Use efficient DB query instead of loading all records into memory
        double avgDuration = repository.findAverageDuration();

        // All-time total talk time
        long totalTalkTimeSeconds = repository.findTotalDurationSince(0L);

        // Today's stats — start of today in IST (UTC+5:30)
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        long startOfTodayMs = LocalDate.now(istZone)
                .atStartOfDay(istZone)
                .toInstant()
                .toEpochMilli();

        long todayTotalCalls = repository.countCallsSince(startOfTodayMs);
        long todayIncomingCalls = repository.countCallsSinceByType(startOfTodayMs, CallLog.CallType.INCOMING);
        long todayOutgoingCalls = repository.countCallsSinceByType(startOfTodayMs, CallLog.CallType.OUTGOING);
        long todayMissedCalls = repository.countCallsSinceByType(startOfTodayMs, CallLog.CallType.MISSED);
        long todayTalkTimeSeconds = repository.findTotalDurationSince(startOfTodayMs);

        List<Dto.DeviceInfo> devices = repository.countByDevice().stream()
                .map(row -> Dto.DeviceInfo.builder()
                        .deviceId((String) row[0])
                        .callCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return Dto.StatsResponse.builder()
                .totalCalls(totalCalls)
                .incomingCalls(incomingCalls)
                .outgoingCalls(outgoingCalls)
                .missedCalls(missedCalls)
                .avgDurationSeconds(avgDuration)
                .totalTalkTimeSeconds(totalTalkTimeSeconds)
                .todayTotalCalls(todayTotalCalls)
                .todayIncomingCalls(todayIncomingCalls)
                .todayOutgoingCalls(todayOutgoingCalls)
                .todayMissedCalls(todayMissedCalls)
                .todayTalkTimeSeconds(todayTalkTimeSeconds)
                .devices(devices)
                .build();
    }

    private CallLog.CallType parseCallType(String type) {
        if (type == null) return CallLog.CallType.UNKNOWN;
        try {
            return CallLog.CallType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CallLog.CallType.UNKNOWN;
        }
    }
}
