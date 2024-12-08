package com.example.air.airquality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AirQualityController {

    private final ObjectMapper objectMapper;
    private final AirQualityFetcher airQualityFetcher;
    private final AirQualityMonitoringService monitoringService;

@GetMapping("/chart")
    public String showChart(Model model) {
    try {

        String jsonResponse = airQualityFetcher.fetchAirQualityData();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode items = rootNode.path("response").path("body").path("items");

        // 데이터 변환
        List<Map<String, String>> formattedData = StreamSupport.stream(items.spliterator(), false)
                .filter(item -> item.has("informCode") && !item.get("informCode").isNull())
                .map(item -> Map.of(
                        "informCode", item.get("informCode").asText(),
                        "informGrade", item.get("informGrade").asText(),
                        "informData", item.get("informData").asText(),
                        "dataTime", item.get("dataTime").asText()
                ))
                .collect(Collectors.toList());

        // 데이터를 보여주면서 동시에 모니터링 체크 실행
        monitoringService.monitorAirQuality();
        // 차트 표시를 위한 데이터 모델에 추가
        model.addAttribute("airQualityData", formattedData);

        // 알림을 위한 추가 데이터
        model.addAttribute("lastUpdateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        return "airQuality";
    } catch (Exception e) {
        log.error("Error fetching air quality data", e);
        model.addAttribute("error", "대기 데이터를 가져오지 못했습니다.");
        return "error";
    }
}



    @GetMapping("/hi")
    public String get(){
     return "hi";
    }





}