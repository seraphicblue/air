실행방법 
1. Docker 이미지 빌드
docker build -t my-airquality-app .
2. Docker 컨테이너 실행
docker run -p 8080:8080 my-airquality-app

어플리케이션은 http://localhost:8080/chart 에서 접속 가능합니다
