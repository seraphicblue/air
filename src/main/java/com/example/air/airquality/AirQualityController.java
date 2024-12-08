package com.example.air.airquality;

import com.example.air.alert.NotificationRequest;
import com.example.air.alert.WebPushService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.Serializable;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
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

    //    @GetMapping("/current")
//    public Mono<Map<String, Object>> getCurrentAirQuality() {
//        return Mono.fromCallable(() -> {
//            try {
//                String xmlResponse = airQualityFetcher.fetchAirQualityData();
//                log.debug("Raw XML response: {}", xmlResponse);
//
//                JAXBContext context = JAXBContext.newInstance(AirQualityResponse.class);
//                Unmarshaller unmarshaller = context.createUnmarshaller();
//                AirQualityResponse response = (AirQualityResponse) unmarshaller.unmarshal(
//                        new StringReader(xmlResponse));
//
//                List<AirQualityResponse.Item> items = response.getBody().getItems();
//                List<Map<String, String>> formattedData = items.stream()
//                        .filter(item -> item.getInformCode() != null)
//                        .map(item -> {
//                            Map<String, String> data = new HashMap<>();
//                            data.put("informCode", item.getInformCode());
//                            data.put("informGrade", item.getInformGrade());
//                            data.put("informData", item.getInformData());
//                            data.put("dataTime", item.getDataTime());
//                            return data;
//                        })
//                        .collect(Collectors.toList());
//
//                Map<String, Object> result = new HashMap<>();
//                result.put("data", formattedData);
//                result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
//                return result;
//            } catch (Exception e) {
//                log.error("Error parsing air quality data", e);
//                Map<String, Object> errorResult = new HashMap<>();
//                errorResult.put("error", "대기 데이터를 가져오지 못했습니다.");
//                errorResult.put("message", e.getMessage());
//                return errorResult;
//            }
//        }).subscribeOn(Schedulers.boundedElastic());
//    }
@GetMapping("/chart")
    public String showChart(Model model) {
    try {
        // 데이터를 보여주면서 동시에 모니터링 체크 실행
        monitoringService.monitorAirQuality();

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