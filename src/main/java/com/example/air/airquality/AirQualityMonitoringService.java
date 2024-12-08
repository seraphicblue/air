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
    private final AirQualityFetcher airQualityFetcher;// 대기질 데이터를 가져오는 컴포넌트
    private final ObjectMapper objectMapper;
    private final WebPushService webPushService;// 사용자에게 푸시 알림을 전송하는 서비스

    @Scheduled(cron = "0 0 * * * *") // 매시 정각마다 실행 후 대기질 데이터를 확인하고 나쁜 상태를 알림
    public void monitorAirQuality() {
        try {
            String jsonResponse = airQualityFetcher.fetchAirQualityData(); // 대기질 데이터를 API를 통해 가져옴
            JsonNode rootNode = objectMapper.readTree(jsonResponse); // 가져온 JSON 데이터를 파싱
            JsonNode items = rootNode.path("response").path("body").path("items");// JSON 데이터에서 관심 있는 대기질 항목 추출

            // 각 항목을 순회하면서, 나쁜 대기질 데이터를 필터링하여 처리
            StreamSupport.stream(items.spliterator(), false)
                    .filter(item -> item.has("informGrade"))
                    .forEach(item -> {
                        String grade = item.get("informGrade").asText();
                        if (isAirQualityBad(grade)) {// 나쁜 대기질 여부 판단
                            sendAirQualityAlert(item);// 알림 전송
                        }
                    });
        } catch (Exception e) {
            log.error("대기오염을 모니터링하는데에 문제가 있습니다.", e);
        }
    }

    // 특정 대기질 데이터를 기반으로 알림을 전송
    private void sendAirQualityAlert(JsonNode item) {

        String title = "대기질이 나쁜 지역입니다.";
        String informGrade = item.get("informGrade").asText();
        log.debug("알림으로 보낼 데이터 입니다.: {}", informGrade);  // 원본 데이터 로깅

        String[] regions = informGrade.split(",");  // informData가 아닌 informGrade로 수정
        StringBuilder badRegions = new StringBuilder();

        for (String region : regions) {
            log.debug("각지역들 입니다.: {}", region.trim());  // 각 지역 처리 로깅
            if (region.contains("보통") || region.contains("매우나쁨") || region.contains("위험")) {
                badRegions.append(region.trim()).append("\n");
                log.debug("Added bad region: {}", region.trim());
            }
        }

        if (!badRegions.isEmpty()) {
            String message = String.format(
                    "[%s 기준]\n%s\n측정 시간: %s",
                    item.get("informCode").asText(),// 대기질 코드
                    badRegions.toString(), // 나쁜 지역 리스트
                    item.get("dataTime").asText()// 측정 시간
            );

            log.info("위내용의 알림을 보냈습니다.: {}", message);

            // 푸시 알림 전송
            webPushService.sendPushNotification(
                    NotificationRequest.builder()
                            .title(title)// 알림 제목
                            .message(message)
                            .build()// 알림 메시지
            );
        } else {
            log.debug("대기 오염이 나쁜 지역이 없습니다.");
        }
    }

    private boolean isAirQualityBad(String grade) {
        log.debug("등급을 체크합니다.: {}", grade);  // 로깅 추가
        boolean isBad = grade != null && (
                grade.contains("보통") ||
                        grade.contains("매우나쁨") ||
                        grade.contains("위험")
        );
        log.debug(" {} 해당등급은 나쁜 등급입니다. : {}", grade, isBad);
        return isBad;
    }
}