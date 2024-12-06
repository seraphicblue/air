package com.example.air.airquality;

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

@Component
public class AirQualityFetcher {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String fetchAirQualityData() throws IOException, InterruptedException {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return fetchAirQualityData(currentDate);
    }

    public String fetchAirQualityData(String searchDate) throws IOException, InterruptedException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth");

        urlBuilder.append("?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=LX1Fa5ExG4QlJxkuPAQj8DYylJiU1O1b40lWB0K4uk%2F%2FMjcRGyU5YJNBsFJxFhZ2PY49hPWeyZbQrMkKUEH6kA%3D%3D");
        urlBuilder.append("&" + URLEncoder.encode("returnType", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("xml", StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("100", StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("1", StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("searchDate", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(searchDate, StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("InformCode", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("PM10", StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Content-Type", "application/xml")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}