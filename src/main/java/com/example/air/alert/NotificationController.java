package com.example.air.alert;


import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final WebPushService webPushService;

    @Autowired
    public NotificationController(WebPushService webPushService) {
        this.webPushService = webPushService;
    }

    @PostMapping("/send")
    public void sendNotification(
            @RequestParam String title,
            @RequestParam String message
    ) {
        webPushService.sendPushNotification(
                NotificationRequest.builder()
                        .title(title)
                        .message(message)
                        .build()
        );
    }
}

