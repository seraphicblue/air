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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class AirQualityMonitoringService {
    private final AirQualityFetcher airQualityFetcher;// 대기질 데이터를 가져오는 컴포넌트
    private final ObjectMapper objectMapper;
    private final WebPushService webPushService;// 사용자에게 푸시 알림을 전송하는 서비스
    // 이전 알림 시간과 메시지를 추적하기 위한 캐시
    private LocalDateTime lastNotificationTime;
    private Set<String> lastNotifiedMessages = new HashSet<>();

    @Scheduled(cron = "0 0 * * * *") // 매시 정각마다 실행 후 대기질 데이터를 확인하고 나쁜 상태를 알림
    public void monitorAirQuality() {
        try {
            String jsonResponse = airQualityFetcher.fetchAirQualityData(); // 대기질 데이터를 API를 통해 가져옴
            JsonNode rootNode = objectMapper.readTree(jsonResponse); // 가져온 JSON 데이터를 파싱
            JsonNode items = rootNode.path("response").path("body").path("items");// JSON 데이터에서 관심 있는 대기질 항목 추출

            // 현재 시간 기준으로 모든 나쁜 대기질 데이터를 한 번에 수집
            Set<String> currentBadRegions = new HashSet<>();
            // AtomicReference를 사용하여 lambda에서 안전하게 값을 수정할 수 있게 함
            AtomicReference<String> informCodeRef = new AtomicReference<>();
            AtomicReference<String> dataTimeRef = new AtomicReference<>();


            // 각 항목을 순회하면서, 나쁜 대기질 데이터를 필터링하여 처리
            StreamSupport.stream(items.spliterator(), false)
                    .filter(item -> item.has("informGrade"))
                    .forEach(item -> {
                        String grade = item.get("informGrade").asText();
                        if (isAirQualityBad(grade)) {
                            processBadAirQuality(item, currentBadRegions);
                            // 첫 번째 유효한 항목에서만 정보 저장
                            if (informCodeRef.get() == null) {
                                informCodeRef.set(item.get("informCode").asText());
                                dataTimeRef.set(item.get("dataTime").asText());
                            }
                        }
                    });

            // 수집된 데이터가 있고, 이전 알림과 다른 경우에만 알림 전송
            if (!currentBadRegions.isEmpty() && shouldSendNotification(currentBadRegions)) {
                String informCode = informCodeRef.get();
                String dataTime = dataTimeRef.get();
                if (informCode != null && dataTime != null) {
                    sendAirQualityAlert(currentBadRegions, informCode, dataTime);
                }
            }
        } catch (Exception e) {
            log.error("대기오염을 모니터링하는데에 문제가 있습니다.", e);
        }

    }
    private void processBadAirQuality(JsonNode item, Set<String> currentBadRegions) {
        String informGrade = item.get("informGrade").asText();
        String[] regions = informGrade.split(",");

        for (String region : regions) {
            region = region.trim();
            if (region.contains("나쁨") || region.contains("매우나쁨")) {
                currentBadRegions.add(region);
                log.debug("대기상태 나쁜지역 추가: {}", region);
            }
        }
    }
    // 특정 대기질 데이터를 기반으로 알림을 전송
    private void sendAirQualityAlert(Set<String> badRegions, String informCode, String dataTime) {
        String title = "대기질이 나쁜 지역입니다.";
        StringBuilder messageBuilder = new StringBuilder();

        messageBuilder.append(String.format("[%s 기준]\n", informCode));// 대기질 코드
        badRegions.forEach(region -> messageBuilder.append(region).append("\n")); // 나쁜 지역 리스트
        messageBuilder.append(String.format("측정 시간: %s", dataTime));// 측정 시간

        String message = messageBuilder.toString();
        log.info("위내용의 알림을 보냈습니다.: {}", message);

        webPushService.sendPushNotification(
                NotificationRequest.builder()
                        .title(title)// 알림 제목
                        .message(message)
                        .build()// 알림 메시지
        );
    }


    private boolean shouldSendNotification(Set<String> currentBadRegions) {
        LocalDateTime now = LocalDateTime.now();

        // 마지막 알림으로부터 1시간이 지났거나, 내용이 다른 경우에만 알림 전송
        boolean shouldSend = lastNotificationTime == null ||
                now.isAfter(lastNotificationTime.plusHours(1)) ||
                !lastNotifiedMessages.equals(currentBadRegions);

        if (shouldSend) {
            lastNotificationTime = now;
            lastNotifiedMessages = new HashSet<>(currentBadRegions);
        }

        return shouldSend;
    }



    private boolean isAirQualityBad(String grade) {
        log.debug("등급을 체크합니다.: {}", grade);  // 로깅 추가
        boolean isBad = grade != null && (
                grade.contains("나쁨") ||
                        grade.contains("매우나쁨")
        );
        log.debug(" {} 해당등급은 나쁜 등급입니다. : {}", grade, isBad);
        return isBad;
    }
}