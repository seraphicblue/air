package com.example.air.airquality;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityData {
    private LocalDateTime timestamp;
    private String type;        // PM10 or PM25
    private double value;       // 측정값
    private String grade;       // 등급 (좋음, 보통, 나쁨, 매우나쁨)
    private String cause;       // 발생원인
    private LocalDateTime dataTime;    // 측정시간

    // 시간별 측정값 기록을 위한 리스트
    private List<Measurement> pm10Measurements = new ArrayList<>();
    private List<Measurement> pm25Measurements = new ArrayList<>();


    public AirQualityData(LocalDateTime now, String type, double value, String informGrade, String informCause, LocalDateTime  dataTime) {
        this.timestamp = timestamp;  // 현재 시간
        this.type = type;            // PM10 또는 PM25
        this.value = value;          // 측정값
        this.grade = grade;          // 등급 (좋음, 보통, 나쁨, 매우나쁨)
        this.cause = cause;          // 발생 원인
        this.dataTime = dataTime;    // 측정 시간
    }


    public void addPM10Measurement(double value, String grade) {
        pm10Measurements.add(new Measurement(LocalDateTime.now(), value, grade));
    }

    public void addPM25Measurement(double value, String grade) {
        pm25Measurements.add(new Measurement(LocalDateTime.now(), value, grade));
    }

    // 내부 클래스: 측정 데이터를 저장
    @Data
    @NoArgsConstructor
    private static class Measurement {
        private LocalDateTime time;
        private double value;
        private String grade;

        public Measurement(LocalDateTime time, double value, String grade) {
            this.time = time;
            this.value = value;
            this.grade = grade;
        }
    }
}
