package com.example.air.airquality;

import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.List;

@Getter
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class AirQualityResponse {
    @XmlElement(name = "header")
    private Header header;

    @XmlElement(name = "body")
    private Body body;

    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Header {
        @XmlElement(name = "resultCode")
        private String resultCode;

        @XmlElement(name = "resultMsg")
        private String resultMsg;
    }

    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Body {
        @XmlElementWrapper(name = "items")
        @XmlElement(name = "item")
        private List<Item> items;

        @XmlElement(name = "numOfRows")
        private int numOfRows;

        @XmlElement(name = "pageNo")
        private int pageNo;

        @XmlElement(name = "totalCount")
        private int totalCount;
    }

    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {
        @XmlElement(name = "informCode")
        private String informCode;

        @XmlElement(name = "informCause")
        private String informCause;

        @XmlElement(name = "informOverall")
        private String informOverall;

        @XmlElement(name = "informGrade")
        private String informGrade;

        @XmlElement(name = "informData")
        private String informData;

        @XmlElement(name = "dataTime")
        private String dataTime;
    }
}