package com.callyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

@Component
@Slf4j
public class KeepAliveService {

    @Value("${RENDER_EXTERNAL_URL:}")
    private String renderExternalUrl;

    /**
     * Pings the app every 10 minutes to prevent Render free tier from sleeping.
     * Render puts free services to sleep after 15 min of inactivity.
     * This self-ping keeps it alive 24/7.
     */
    @Scheduled(fixedRate = 600000) // every 10 minutes
    public void keepAlive() {
        if (renderExternalUrl == null || renderExternalUrl.isBlank()) {
            return; // Skip when running locally
        }

        try {
            String pingUrl = renderExternalUrl + "/api/v1/calls/stats";
            HttpURLConnection conn = (HttpURLConnection) new URL(pingUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            int status = conn.getResponseCode();
            conn.disconnect();
            log.debug("Keep-alive ping: status={}, time={}", status, Instant.now());
        } catch (Exception e) {
            log.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
