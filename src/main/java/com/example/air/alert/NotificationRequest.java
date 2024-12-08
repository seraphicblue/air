package com.example.air.alert;

import lombok.Builder;
import lombok.Getter;
import lombok.AccessLevel;

@Getter
@Builder
public class NotificationRequest {
    private final String title;
    private final String message;
}