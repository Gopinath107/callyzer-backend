package com.callyzer.controller;

import com.callyzer.model.CallLog;
import com.callyzer.model.Dto;
import com.callyzer.service.CallLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/calls")
@RequiredArgsConstructor
@Slf4j
public class CallEventController {

    private final CallLogService callLogService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${callyzer.app-key}")
    private String appKey;

    @PostMapping("/event")
    public ResponseEntity<?> receiveCallEvent(
            @RequestHeader(value = "X-App-Key", required = false) String key,
            @RequestBody Dto.CallEventRequest request) {

        if (!appKey.equals(key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or missing X-App-Key"));
        }

        CallLog saved = callLogService.save(request);
        if (saved == null) {
            return ResponseEntity.ok(Map.of("status", "duplicate", "message", "Event already exists"));
        }

        Dto.CallEventResponse response = Dto.CallEventResponse.from(saved);

        // Push to WebSocket subscribers
        messagingTemplate.convertAndSend("/topic/calls", response);
        log.info("Pushed call event to WebSocket: id={}", saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/batch")
    public ResponseEntity<?> receiveBatch(
            @RequestHeader(value = "X-App-Key", required = false) String key,
            @RequestBody List<Dto.CallEventRequest> requests) {

        if (!appKey.equals(key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or missing X-App-Key"));
        }

        int saved = callLogService.saveBatch(requests);

        // Notify dashboard of batch import
        messagingTemplate.convertAndSend("/topic/refresh",
                Map.of("action", "batch_imported", "count", saved));
        log.info("Batch import: {} records saved from {} submitted", saved, requests.size());

        return ResponseEntity.ok(Map.of("status", "ok", "saved", saved, "total", requests.size()));
    }

    @GetMapping
    public ResponseEntity<List<Dto.CallEventResponse>> getAllCalls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(callLogService.getAll(page, size));
    }

    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<Dto.CallEventResponse>> getByDevice(@PathVariable String deviceId) {
        return ResponseEntity.ok(callLogService.getByDevice(deviceId));
    }

    @GetMapping("/stats")
    public ResponseEntity<Dto.StatsResponse> getStats() {
        return ResponseEntity.ok(callLogService.getStats());
    }
}
