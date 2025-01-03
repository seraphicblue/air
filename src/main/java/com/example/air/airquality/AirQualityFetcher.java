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
    private final ObjectMapper objectMapper = new ObjectMapper();//JSON 처리를 위해서 사용
    private final HttpClient httpClient; // HTTP 클라이언트 (Java 11 HttpClient 사용)
    private final ExecutorService executorService;// 병렬 처리를 위한 ExecutorService

    public AirQualityFetcher() {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .build();
    }

    public String fetchAirQualityData() throws IOException, InterruptedException, ExecutionException {
        LocalDate today = LocalDate.now();
        String currentDate = today.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Virtual Thread를 사용하여 병렬 요청, pm25,pm10 data를 각각 가져옴
        CompletableFuture<String> pm10Future = CompletableFuture.supplyAsync(() -> {
            try {
                return fetchAirQualityDataByType(currentDate, "PM10");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);

        CompletableFuture<String> pm25Future = CompletableFuture.supplyAsync(() -> {
            try {
                return fetchAirQualityDataByType(currentDate, "PM25");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executorService);

        // 병렬로 실행된 결과 조합
        String pm10Data = pm10Future.get();
        String pm25Data = pm25Future.get();

        // 두가지 결과를 합함
        return combineJsonResponses(pm10Data, pm25Data);
    }

    private String fetchAirQualityDataByType(String searchDate, String informCode) throws IOException, InterruptedException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth");

        urlBuilder.append("?").append(URLEncoder.encode("serviceKey", StandardCharsets.UTF_8)).append("=LX1Fa5ExG4QlJxkuPAQj8DYylJiU1O1b40lWB0K4uk%2F%2FMjcRGyU5YJNBsFJxFhZ2PY49hPWeyZbQrMkKUEH6kA%3D%3D");
        urlBuilder.append("&").append(URLEncoder.encode("returnType", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode("json", StandardCharsets.UTF_8));
        urlBuilder.append("&").append(URLEncoder.encode("numOfRows", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode("100", StandardCharsets.UTF_8));
        urlBuilder.append("&").append(URLEncoder.encode("pageNo", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode("1", StandardCharsets.UTF_8));
        urlBuilder.append("&").append(URLEncoder.encode("searchDate", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(searchDate, StandardCharsets.UTF_8));
        urlBuilder.append("&").append(URLEncoder.encode("InformCode", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(informCode, StandardCharsets.UTF_8));
        // HTTP 요청 생성 및 전송
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

            // 결과를 포맷팅된 JSON 문자열로 변환
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(combinedJson);
        } catch (Exception e) {
            throw new RuntimeException("JSON 결합 중 오류 발생", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        executorService.close();  // 애플리케이션 종료 시 ExecutorService 종료
    }}