# Air Quality Monitoring Project

실시간 대기질 모니터링 및 알림 서비스

## 🔍 프로젝트 구성
- chart.js를 활용한 대기질 데이터 모니터링
- 웹소켓 기반 실시간 알림
- Virtual Thread를 활용한 고성능 처리

## 🚀 실행 방법

1. Docker 이미지 빌드
```bash
docker build -t my-airquality-app .
```

2. Docker 컨테이너 실행
```bash
docker run -p 8080:8080 my-airquality-app
```

## 📊 대시보드 확인
대기질 차트및 푸시알림은 아래 URL에서 접속 가능합니다:
```
http://localhost:8080/chart
```

## 🛠 기술 스택

### Backend
- Java 21 (Virtual Thread 지원)
- Spring Boot 3.2.1
- Spring WebFlux
- Spring WebSocket

### Frontend
- Thymeleaf

### 개발 도구
- Lombok
- Spring Boot DevTools
