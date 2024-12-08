package com.example.air.alert;

import org.springframework.stereotype.Service;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.DisposableBean;

@Slf4j
@Service
public class WebPushService implements DisposableBean {
    private final ChromeDriver driver;

    public WebPushService() {
        driver = initializeChromeDriver();
    }

    private ChromeDriver initializeChromeDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 1);
        options.setExperimentalOption("prefs", prefs);

        try {
            return new ChromeDriver(options);
        } catch (Exception e) {
            log.error("ChromeDriver 초기화 실패", e);
            throw new RuntimeException("ChromeDriver 초기화 중 오류 발생", e);
        }
    }

    public void sendPushNotification(NotificationRequest request) {
        try {
            String script = String.format(
                    "new Notification('%s', {body: '%s'});",
                    request.getTitle().replace("'", "\\'"),
                    request.getMessage().replace("'", "\\'")
            );
            driver.executeScript(script);
            log.info("알림 전송 완료: {}", request.getTitle());
        } catch (Exception e) {
            log.error("알림 전송 실패: {}", request.getTitle(), e);
            throw new RuntimeException("알림 전송 중 오류 발생", e);
        }
    }

    @Override
    public void destroy() {
        try {
            if (driver != null) {
                driver.quit();
                log.info("ChromeDriver 정상 종료");
            }
        } catch (Exception e) {
            log.error("ChromeDriver 종료 중 오류 발생", e);
        }
    }


}