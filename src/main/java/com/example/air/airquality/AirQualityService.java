package com.example.air.airquality;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class AirQualityService {
    private final WebClient webClient;
    private final RedisTemplate<String, AirQualityData> redisTemplate;
    private final AirQualityData hourlyData;
    private static final Logger log = (Logger) LoggerFactory.getLogger(AirQualityService.class);


    @Value("${api.airquality.serviceKey}")
    private String serviceKey;

    public AirQualityService(WebClient.Builder webClientBuilder,
                                RedisTemplate<String, AirQualityData> redisTemplate,
                                AirQualityData hourlyData) {
        this.webClient = webClientBuilder
                .baseUrl("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.redisTemplate = redisTemplate;
        this.hourlyData = hourlyData;
    }

    public Mono<AirQualityResponse> fetchAirQualityData(String informCode) {  // 메서드 추가
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("returnType", "json")
                        .queryParam("numOfRows", "1")
                        .queryParam("pageNo", "1")
                        .queryParam("searchDate", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .queryParam("InformCode", informCode)
                        .build())
                .retrieve()
                .bodyToMono(AirQualityResponse.class)
                .doOnError(e -> log.info("API 호출 실패: "));
    }

    private void savePM10Data(AirQualityResponse response) {
        AirQualityData data = convertToAirQualityData(response, "PM10");
        String key = "air:quality:pm10:" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        redisTemplate.opsForValue().set(key, data);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);

        // 시간별 데이터 업데이트
        hourlyData.addPM10Measurement(data.getValue(), data.getGrade());

        log.info("PM10 데이터 저장 완료: {}");
    }

    private void savePM25Data(AirQualityResponse response) {
        AirQualityData data = convertToAirQualityData(response, "PM25");
        String key = "air:quality:pm25:" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        redisTemplate.opsForValue().set(key, data);
        redisTemplate.expire(key, 24, TimeUnit.HOURS);

        // 시간별 데이터 업데이트
        hourlyData.addPM25Measurement(data.getValue(), data.getGrade());

        log.info("PM2.5 데이터 저장 완료: {}");
    }

    private AirQualityData convertToAirQualityData(AirQualityResponse response, String type) {
        AirQualityResponse.Item item = response.getResponse().getBody().getItems().get(0);

        AirQualityData data = new AirQualityData();
        data.setTimestamp(LocalDateTime.now());
        data.setType(type);
        data.setGrade(item.getInformGrade());
        data.setCause(item.getInformCause());
        data.setValue(extractValueFromGrade(item.getInformGrade()));

        return data;
    }

    private double extractValueFromGrade(String grade) {
        // 예시: "서울: 나쁨, 제주: 나쁨, 전남: 나쁨" 에서 수치 추출
        if (grade.contains("좋음")) return 30.0;
        if (grade.contains("보통")) return 60.0;
        if (grade.contains("나쁨")) return 90.0;
        if (grade.contains("매우나쁨")) return 120.0;
        return 0.0;
    }
}


