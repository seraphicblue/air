package com.example.air.airquality;

import com.example.air.alert.NotificationRequest;
import com.example.air.alert.WebPushService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.stream.StreamSupport;
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class AirQualityMonitoringService {
    private final AirQualityFetcher airQualityFetcher;
    private final ObjectMapper objectMapper;
    private final WebPushService webPushService;

    @Scheduled(cron = "0 0 * * * *") // 매시 정각마다 실행
    public void monitorAirQuality() {
        try {
            String jsonResponse = airQualityFetcher.fetchAirQualityData();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode items = rootNode.path("response").path("body").path("items");

            StreamSupport.stream(items.spliterator(), false)
                    .filter(item -> item.has("informGrade"))
                    .forEach(item -> {
                        String grade = item.get("informGrade").asText();
                        if (isAirQualityBad(grade)) {
                            sendAirQualityAlert(item);
                        }
                    });
        } catch (Exception e) {
            log.error("Error monitoring air quality", e);
        }
    }

    private void sendAirQualityAlert(JsonNode item) {
        log.info("Processing air quality alert for data: {}", item.toString());  // 로깅 추가

        String title = "대기질 알림";
        String informGrade = item.get("informGrade").asText();
        log.debug("Raw informGrade: {}", informGrade);  // 원본 데이터 로깅

        String[] regions = informGrade.split(",");  // informData가 아닌 informGrade로 수정
        StringBuilder badRegions = new StringBuilder();

        for (String region : regions) {
            log.debug("Processing region: {}", region.trim());  // 각 지역 처리 로깅
            if (region.contains("보통") || region.contains("매우나쁨") || region.contains("위험")) {
                badRegions.append(region.trim()).append("\n");
                log.debug("Added bad region: {}", region.trim());
            }
        }

        if (!badRegions.isEmpty()) {
            String message = String.format(
                    "[%s 기준]\n%s\n측정 시간: %s",
                    item.get("informCode").asText(),
                    badRegions.toString(),
                    item.get("dataTime").asText()
            );

            log.info("Sending notification with message: {}", message);

            webPushService.sendPushNotification(
                    NotificationRequest.builder()
                            .title(title)
                            .message(message)
                            .build()
            );
        } else {
            log.debug("No bad regions found");
        }
    }

    private boolean isAirQualityBad(String grade) {
        log.debug("Checking grade: {}", grade);  // 로깅 추가
        boolean isBad = grade != null && (
                grade.contains("보통") ||
                        grade.contains("매우나쁨") ||
                        grade.contains("위험")
        );
        log.debug("Grade {} is bad: {}", grade, isBad);
        return isBad;
    }
}