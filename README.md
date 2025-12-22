# NBP/DR 실시간 전환 모니터링 대시보드

## 개요
NBP 서버와 DR 서버 간 트래픽/세션 비율을 실시간으로 모니터링하는 웹 대시보드입니다.

## 시스템 요구사항
- Java 1.7 이상
- Maven 3.x
- Spring Boot 1.5.x

## 빌드 및 실행

### 빌드
```bash
mvn clean package
```

### 실행
```bash
java -jar target/nbp-dr-monitoring-1.0.0.jar
```

### 개발 모드 실행
```bash
mvn spring-boot:run
```

## 접속 정보
- 웹 대시보드: http://localhost:8080
- 데모 페이지: http://localhost:8080/demo.html (실제 데이터 없이 미리보기 가능)
- 헬스체크: http://localhost:8080/actuator/health

## 설정 파일
- `application.properties`: 애플리케이션 기본 설정
- `system.properties`: 서비스별 모니터링 대상 서버 설정

## API 엔드포인트
- `GET /api/services`: 서비스 목록 조회
- `GET /api/services/{id}/ratio`: 실시간 상세 조회
- `GET /api/services/{id}/history`: 이력 조회

## 디렉토리 구조
```
src/
├── main/
│   ├── java/com/kgm/monitoring/
│   │   ├── controller/     # REST API 컨트롤러
│   │   ├── service/        # 비즈니스 로직
│   │   ├── config/         # 설정 클래스
│   │   └── model/          # 데이터 모델
│   └── resources/
│       ├── static/         # 웹 리소스 (HTML, CSS, JS)
│       └── application.properties
└── test/                   # 테스트 코드
```

## 개발 상태
현재 Task 1.1 (프로젝트 구조 설계) 완료
다음 단계: Task 1.2 (설정 파일 구조 설계)

## 개발 가이드
- 모든 개발은 Codex와 Kiro IDE를 활용하여 진행
- task.md 파일의 체크리스트를 참고하여 단계별 개발
- 한국어 주석 및 문서 작성