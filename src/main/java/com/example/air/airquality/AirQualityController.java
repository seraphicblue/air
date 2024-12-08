package com.example.air.airquality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Controller
public class AirQualityController {



    /* 현재 대기 수치를 반환하는 api
    * pm10, 수치
    * pm25, 수치 형태로 반환이 됨 */
    @Autowired
    private AirQualityFetcher airQualityFetcher;

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
            String jsonResponse = airQualityFetcher.fetchAirQualityData();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // items 노드 가져오기
            JsonNode items = rootNode.path("response").path("body").path("items");

            // 스트림 처리로 데이터 변환
            List<Map<String, String>> formattedData = StreamSupport.stream(items.spliterator(), false)
                    .filter(item -> item.has("informCode") && !item.get("informCode").isNull())
                    .map(item -> Map.of(
                            "informCode", item.get("informCode").asText(),
                            "informGrade", item.get("informGrade").asText(),
                            "informData", item.get("informData").asText(),
                            "dataTime", item.get("dataTime").asText()
                    ))
                    .collect(Collectors.toList());

            model.addAttribute("airQualityData", formattedData);
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