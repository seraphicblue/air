package com.example.air.airquality;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Component
public class AirQualityFetcher {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String fetchAirQualityData() throws IOException, InterruptedException, ExecutionException {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String currentDate = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);        // 병렬로 두 요청 실행
        CompletableFuture<String> pm10Future = CompletableFuture.supplyAsync(() -> {
            try {
                return fetchAirQualityDataByType(currentDate, "PM10");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        CompletableFuture<String> pm25Future = CompletableFuture.supplyAsync(() -> {
            try {
                return fetchAirQualityDataByType(currentDate, "PM25");
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        // 두 결과 기다리고 합치기
        String pm10Data = pm10Future.get();
        String pm25Data = pm25Future.get();

        return combineXmlResponses(pm10Data, pm25Data);
    }

    private String fetchAirQualityDataByType(String searchDate, String informCode) throws IOException, InterruptedException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMinuDustFrcstDspth");

        urlBuilder.append("?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=LX1Fa5ExG4QlJxkuPAQj8DYylJiU1O1b40lWB0K4uk%2F%2FMjcRGyU5YJNBsFJxFhZ2PY49hPWeyZbQrMkKUEH6kA%3D%3D");
        urlBuilder.append("&" + URLEncoder.encode("returnType", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("xml", StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("100", StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("1", StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("searchDate", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(searchDate, StandardCharsets.UTF_8));
        urlBuilder.append("&" + URLEncoder.encode("InformCode", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(informCode, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlBuilder.toString()))
                .header("Content-Type", "application/xml")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String combineXmlResponses(String pm10Xml, String pm25Xml) {
        try {
            // XML 파싱을 위한 DocumentBuilder 생성
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // PM10과 PM2.5 XML을 파싱
            Document pm10Doc = builder.parse(new InputSource(new StringReader(pm10Xml)));
            Document pm25Doc = builder.parse(new InputSource(new StringReader(pm25Xml)));

            // 새로운 결합된 XML 문서 생성
            Document combinedDoc = builder.newDocument();
            Element rootElement = combinedDoc.createElement("response");
            combinedDoc.appendChild(rootElement);

            Element bodyElement = combinedDoc.createElement("body");
            rootElement.appendChild(bodyElement);

            Element itemsElement = combinedDoc.createElement("items");
            bodyElement.appendChild(itemsElement);

            // PM10 items 복사
            NodeList pm10Items = pm10Doc.getElementsByTagName("item");
            for (int i = 0; i < pm10Items.getLength(); i++) {
                Node importedNode = combinedDoc.importNode(pm10Items.item(i), true);
                itemsElement.appendChild(importedNode);
            }

            // PM2.5 items 복사
            NodeList pm25Items = pm25Doc.getElementsByTagName("item");
            for (int i = 0; i < pm25Items.getLength(); i++) {
                Node importedNode = combinedDoc.importNode(pm25Items.item(i), true);
                itemsElement.appendChild(importedNode);
            }

            // XML을 문자열로 변환
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(combinedDoc), new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("XML 결합 중 오류 발생", e);
        }
    }
}
