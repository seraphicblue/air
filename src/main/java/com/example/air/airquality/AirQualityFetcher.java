package com.example.air.airquality;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

import lombok.RequiredArgsConstructor;
import jakarta.annotation.PreDestroy;

@Component
@RequiredArgsConstructor
public class AirQualityFetcher {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    private final ExecutorService executorService;

    public AirQualityFetcher() {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .build();
    }

    public String fetchAirQualityData() throws IOException, InterruptedException, ExecutionException {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String currentDate = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Virtual Thread를 사용한 병렬 요청
        CompletableFuture<String> pm10Future = CompletableFuture.supplyAsync(() -> {
            try {
                return fetchAirQualityDataByType(currentDate, "PM10");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, httpClient.executor().get());

        CompletableFuture<String> pm25Future = CompletableFuture.supplyAsync(() -> {
            try {
                return fetchAirQualityDataByType(currentDate, "PM25");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, httpClient.executor().get());

        // 병렬로 실행된 결과 조합
        String pm10Data = pm10Future.get();
        String pm25Data = pm25Future.get();

        return combineJsonResponses(pm10Data, pm25Data);
    }

    private String fetchAirQualityDataByType(String searchDate, String informCode) throws IOException, InterruptedException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth");

        urlBuilder.append("?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=LX1Fa5ExG4QlJxkuPAQj8DYylJiU1O1b40lWB0K4uk%2F%2FMjcRGyU5YJNBsFJxFhZ2PY49hPWeyZbQrMkKUEH6kA%3D%3D");
        urlBuilder.append("&" + URLEncoder.encode("returnType", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("json", StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("100", StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("1", StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("searchDate", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(searchDate, StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("InformCode", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(informCode, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    private String combineJsonResponses(String pm10Json, String pm25Json) {
        try {
            // JSON 파싱
            JsonNode pm10Node = objectMapper.readTree(pm10Json);
            JsonNode pm25Node = objectMapper.readTree(pm25Json);

            // 새로운 결합된 JSON 객체 생성
            ObjectNode combinedJson = objectMapper.createObjectNode();
            ObjectNode responseNode = combinedJson.putObject("response");
            ObjectNode bodyNode = responseNode.putObject("body");
            ArrayNode itemsNode = bodyNode.putArray("items");

            // PM10 items 추가
            JsonNode pm10Items = pm10Node.path("response").path("body").path("items");
            if (pm10Items.isArray()) {
                for (JsonNode item : pm10Items) {
                    itemsNode.add(item);
                }
            }

            // PM2.5 items 추가
            JsonNode pm25Items = pm25Node.path("response").path("body").path("items");
            if (pm25Items.isArray()) {
                for (JsonNode item : pm25Items) {
                    itemsNode.add(item);
                }
            }

            // 결과를 예쁘게 포맷팅된 JSON 문자열로 변환
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(combinedJson);
        } catch (Exception e) {
            throw new RuntimeException("JSON 결합 중 오류 발생", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        executorService.close();  // shutdown() 대신 close() 사용
    }}