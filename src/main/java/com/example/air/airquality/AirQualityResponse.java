package com.example.air.airquality;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
public class AirQualityResponse {
    private Response response;

    @Data
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class Body {
        private List<Item> items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }

    @Data
    public static class Item {
        private String dataTime;         // 통보시간
        private String informCode;       // 통보코드 (PM10, PM25)
        private String informGrade;      // 예보등급 (좋음, 보통, 나쁨, 매우나쁨)
        private String informOverall;    // 예보개황
        private String informCause;      // 발생원인
    }
}