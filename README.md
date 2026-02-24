# Spring Gift Test - AI 활용 테스트 학습

## 학습 목표

**AI(Claude Code)를 활용하여 레거시 코드의 테스트 코드를 효율적으로 작성하는 방법을 학습합니다.**

### 핵심 고민

- AI에게 어떻게 지시해야 좋은 테스트 코드를 얻을 수 있을지
- 반복 작업을 자동화하는 Custom Skill을 어떻게 설계할지
- AI가 생성한 코드의 품질을 어떻게 검증할지

## AI 워크플로우 설계

### Custom Skills 구성

AI와의 협업을 위해 3개의 Custom Skill을 설계했습니다.

| Skill | 역할 | 트리거 |
|-------|------|--------|
| `/summarize` | 대화 내용 요약 | "요약해줘" |
| `/test-behavior` | 행위 분석 → 테스트 시나리오 도출 | "테스트 목록 뽑아줘" |
| `/generate-test` | 테스트 코드 생성 | "테스트 코드 짜줘" |

### 워크플로우

```
1. 행위 전달 → 2. /test-behavior → 3. /generate-test → 4. 커밋
```

**예시:**
```
User: "Option 재고 차감 테스트 짜줘"
AI: [/test-behavior 실행] → 시나리오 5개 도출
AI: [/generate-test 실행] → OptionTest.java 생성
AI: [커밋] → test(option): Option.decrease() 단위 테스트 추가
```

## 학습 과정에서 발견한 것들

자세한 내용은 [LEARNING.md](LEARNING.md)를 참고하세요.

- AI의 한계: 리플렉션 workaround
- 프롬프트 설계의 중요성
- Skill 자동 호출 설정
- Cucumber BDD 도입 과정
- PostgreSQL + Docker Compose 전환

## 요구사항 2: PostgreSQL + Docker Compose 통합

### 목표

H2 in-memory DB를 PostgreSQL로 전환하고, Docker Compose로 테스트 환경을 자동화합니다.

### 변경 사항

| 순서 | 파일 | 변경 내용 |
|------|------|----------|
| 1 | `compose.yml` (신규) | PostgreSQL 16 컨테이너 정의 |
| 2 | `build.gradle` | H2 제거, PostgreSQL 드라이버 + spring-boot-docker-compose 추가 |
| 3 | `application.properties` | 공통 JPA 설정 (dev 기본값) |
| 4 | `application-test.properties` (신규) | 테스트 프로파일 설정 |
| 5 | `Option.java` | `@Table(name = "options")` 추가 (PostgreSQL 호환) |
| 6 | `DatabaseCleaner.java` (신규) | TRUNCATE CASCADE 기반 테이블 초기화 |
| 7 | 모든 테스트 클래스 | `@ActiveProfiles("test")` 추가 |
| 8 | `CommonStepDefinitions.java` | DatabaseCleaner 사용으로 전환 |

### 실행

```bash
./gradlew cucumberTest    # Cucumber 인수 테스트
./gradlew test            # 전체 테스트
```

## 요구사항 3: Application 컨테이너화

### 목표

애플리케이션을 Docker 컨테이너로 빌드하고, Cucumber 테스트가 컨테이너에 HTTP 요청을 보내도록 전환합니다.

### 변경 사항

| 순서 | 파일 | 변경 내용 |
|------|------|----------|
| 1 | `Dockerfile` (신규) | Multi-stage build (JDK 빌드 → JRE 실행) |
| 2 | `.dockerignore` (신규) | 빌드 불필요 파일 제외 |
| 3 | `compose.yml` | app 서비스 추가 (e2e 프로파일, 28080 포트) |
| 4 | `application-e2e.properties` (신규) | E2E 프로파일 설정 |
| 5 | `E2eRestTemplateConfig.java` (신규) | RestTemplate (localhost:28080) |
| 6 | `CucumberSpringConfig.java` | WebEnvironment.NONE + e2e 프로파일 |
| 7 | StepDefinitions 3개 | TestRestTemplate → RestTemplate |
| 8 | `GiftAcceptanceTest.java` | WebEnvironment.NONE + e2e 프로파일 |
| 9 | `build.gradle` | dockerBuild/dockerUp/dockerDown 태스크 추가 |

### 실행

```bash
./gradlew dockerBuild                      # Docker 이미지 빌드
./gradlew dockerUp                         # 컨테이너 시작
curl http://localhost:28080/api/categories  # 응답 확인
./gradlew cucumberTest                     # Docker 환경에서 테스트
./gradlew dockerDown                       # 컨테이너 정리
```

## 실행 방법

### 요구사항

- Java 21
- Gradle 8.x (Wrapper 포함)
- Docker / Docker Compose

### 전체 테스트 실행

```bash
./gradlew test
```

### 테스트 유형별 실행

```bash
# Cucumber 인수 테스트
./gradlew cucumberTest

# 단위 테스트
./gradlew test --tests "gift.model.*"

# 서비스 통합 테스트
./gradlew test --tests "gift.application.*"

# API 인수 테스트
./gradlew test --tests "gift.ui.*"
```

## 결과물

### 테스트 커버리지

| 유형 | 개수 | 예시 |
|------|------|------|
| Domain 단위 | 5 | `OptionTest` |
| Service 통합 | 18 | `GiftServiceTest`, `ProductServiceTest` |
| Controller 인수 | 4 | `GiftAcceptanceTest` |
| Cucumber 인수 | 10 | `gift.feature`, `category.feature`, `product.feature` |
| Infrastructure | 2 | `FakeGiftDeliveryTest` |
| **총합** | **39** | |

### 문서

- [TEST_STRATEGY.md](TEST_STRATEGY.md) - 테스트 전략
- [AI_USAGE.md](AI_USAGE.md) - AI 활용 상세 기록
- [LEARNING.md](LEARNING.md) - 학습 과정에서 발견한 것들
- [.claude/skills/](/.claude/skills/) - Custom Skill 정의
