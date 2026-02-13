# 💸 Keep Or Cut

> 구독 서비스, 유지할까 해지할까? 데이터로 판단하세요.

<br>

## 📌 소개

**KEEP or CUT**는 사용자의 디지털 구독 서비스 이용 패턴을 정량화하여, 유지/해지 판단의 객관적 근거를 제공하는 의사 결정 도구입니다. 
"이번 달도 그냥 유지할까?" 대신 "효율 10%, 연간 18만원 낭비"라는 명확한 데이터로 결정하세요.

<br>

## 🚀 로컬 실행

### 1. 레포지토리 클론
```bash
git clone [repository-url]
cd subscription-keep-or-cut
```

### 2. 백엔드 실행
```bash
# subscription-keep-or-cut\backend 폴더 열기
cd backend

# BackApplication.main() 실행
# (IDE에서 BackApplication.java 파일의 main 메서드 실행)
```

### 3. 프론트엔드 실행
```bash
# subscription-keep-or-cut\frontend 폴더 열기
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm run dev

# 브라우저에서 접속
# http://localhost:3001 (또는 http://localhost:3000)
```

<br>

## 💡 주요 기능

<img width="2544" height="1434" alt="1" src="https://github.com/user-attachments/assets/006b9163-d0f1-470c-b5db-6cccaa22c561" />
<img width="2544" height="1436" alt="2-1" src="https://github.com/user-attachments/assets/1cfc0232-9b25-4728-9e91-bf8d65fd450e" />
<img width="2520" height="1374" alt="3" src="https://github.com/user-attachments/assets/5b8fa02a-0806-41c9-9df9-8c4eeffce50b" />
<img width="2412" height="1264" alt="4" src="https://github.com/user-attachments/assets/c38d7501-3900-443a-adbb-69eaba5adfff" />
<img width="2032" height="1310" alt="5" src="https://github.com/user-attachments/assets/67860629-1442-4a08-a23e-bd7dd1b7ce9f" />

<br>

1. **구독 등록** - 월/분기/연간 결제 지원, 공유 비용 분담 계산
2. **사용량 기록** - 콘텐츠형(시간), 생산성형(일수) 구분 입력
3. **효율 평가** - 5단계 등급 (효율적 / 유지 / 재검토 / 비효율 / 유령 구독)
4. **대시보드** - 월별 구독 현황 및 연간 낭비 추정액

<br>

## 🎯 해결하는 문제

사용자들은 이미 "이용 빈도"를 구독 유지/해지의 가장 중요한 판단 기준으로 인식하고 있습니다 (오픈서베이, 구독서비스 트렌드 리포트 2025).

하지만 정작 다음 질문에는 답하지 못합니다:

- "자주 이용"이란 구체적으로 얼마나 자주인가?
- 현재 나의 이용량은 "자주"에 해당하는가?
- 여러 구독 중 어떤 것이 가장 비효율적인가?

본 서비스는 사용자들이 중요하게 느끼는 **이용 빈도를 측정 가능한 수치로 전환**하여, 유지/해지 판단의 정량적 근거를 제공합니다.

<br>

## 📝프로젝트 구조
```
src/
├── main/
│   ├── java/com/subasset/
│   │   ├── domain/          # 도메인 엔티티
│   │   ├── repository/      # 데이터 접근 계층
│   │   ├── service/         # 비즈니스 로직
│   │   ├── controller/      # API 컨트롤러
│   │   ├── dto/             # 요청/응답 DTO
│   │   └── global/          # 공통 설정 및 예외 처리
│   └── resources/
│       ├── application.yml
│       └── data.sql         # 초기 카테고리 데이터
└── test/                    # 테스트 코드
```
