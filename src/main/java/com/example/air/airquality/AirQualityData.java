package com.example.air.airquality;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class AirQualityData {
    private LocalDateTime timestamp;
    private String type;        // PM10 or PM25
    private double value;       // 측정값
    private String grade;       // 등급 (좋음, 보통, 나쁨, 매우나쁨)
    private String cause;       // 발생원인
    private String dataTime;    // 측정시간

    public void addPM10Measurement(double value, String grade) {
    }

    public void addPM25Measurement(double value, java.lang.String grade) {
    }
}