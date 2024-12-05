package com.example.air.airquality;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/air-quality")
public class AirQualityController {
    private final AirQualityService airQualityService;  // 이름 변경

    public AirQualityController(AirQualityService airQualityService) {
        this.airQualityService = airQualityService;
    }


    @GetMapping("/current")
    public Mono<Map<String, Object>> getCurrentAirQuality() {
        return airQualityService.fetchAirQualityData("PM10")
                .zipWith(airQualityService.fetchAirQualityData("PM25"))
                .map(tuple -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("pm10", extractGradeData(tuple.getT1()));
                    result.put("pm25", extractGradeData(tuple.getT2()));
                    return result;
                })
                .onErrorResume(e -> Mono.just(Map.of("error", "대기 데이터를 가져오지 못했습니다.", "message", e.getMessage())));
    }
    @GetMapping("/hi")
    public String get(){
     return "hi";
    }

    private Map<String, String> extractGradeData(AirQualityResponse response) {
        AirQualityResponse.Item item = response.getResponse().getBody().getItems().get(0);
        return Map.of(
                "grade", item.getInformGrade(),
                "cause", item.getInformCause(),
                "time", item.getDataTime()
        );
    }
}