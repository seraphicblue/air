package com.example.air.alert;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationRequest {
    private final String title;
    private final String message;
}