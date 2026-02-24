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

### 1. AI의 한계: 리플렉션 workaround

Request DTO에 생성자가 없어서 AI가 리플렉션으로 우회했습니다.

```java
// AI가 사용한 workaround (23개 인스턴스)
private void setField(Object obj, String fieldName, Object value) throws Exception {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
}
```

**교훈**: AI가 생성한 코드도 반드시 리뷰해야 합니다. "테스트가 통과한다"와 "올바른 테스트다"는 다릅니다.

### 2. 프롬프트 설계의 중요성

Claude.md에 역할과 규칙을 명확히 정의했습니다:

```markdown
## 너의 역할
너는 **시니어 테스트 엔지니어**야.

### 테스트 작성 원칙
- **행위 중심**: 구현이 아닌 행위를 테스트해
- **가독성**: 한글 메서드명, given-when-then 구조
```

### 3. Skill 자동 호출 설정

Claude.md에 자동 트리거 조건을 추가했습니다:

```markdown
**`/test-behavior` 자동 호출:**
- "XXX 테스트 어떻게 짜야해?"
- "XXX 테스트 목록 뽑아줘"

**`/generate-test` 자동 호출:**
- "XXX 테스트 코드 짜줘"
```

### 4. Cucumber BDD 도입 과정

Feature 파일로 스펙을 먼저 정의하고, 프로덕션 코드를 수정하는 BDD 흐름을 경험했습니다.

**BDD 흐름:**
```
1. Feature 파일에 올바른 기대값 작성 (400, 404)
2. 테스트 실패 확인 (프로덕션 코드가 500 반환)
3. GlobalExceptionHandler 추가하여 테스트 통과
```

**Step Definitions 분리 패턴:**

```
CommonStepDefinitions     ← 공통 (@Before, 응답 상태코드)
AcceptanceTestContext     ← 시나리오 간 공유 상태 (@ScenarioScope)
GiftStepDefinitions       ← gift.feature 전용
CategoryStepDefinitions   ← category.feature 전용
ProductStepDefinitions    ← product.feature 전용
```

**Repository vs HTTP 접근 방식:**

| 기준 | Repository | HTTP |
|------|-----------|------|
| 리팩토링 내성 | 낮음 (내부 변경에 영향) | 높음 (API 계약만 유지하면 통과) |
| 외부 관찰 가능 | 아니오 | 예 |
| 적합한 용도 | 데이터 정리 (@Before) | Given/When/Then 행위 검증 |

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

## 실행 방법

### 요구사항

- Java 21
- Gradle 8.x (Wrapper 포함)

### 전체 테스트 실행

```bash
./gradlew test
```

### 테스트 유형별 실행

```bash
# Cucumber 인수 테스트
./gradlew test --tests "gift.acceptance.CucumberTest"

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
| Infrastructure | 2 | `FakeGiftDeliveryTest` |
| **총합** | **29** | |

### 문서

- [TEST_STRATEGY.md](TEST_STRATEGY.md) - 테스트 전략
- [AI_USAGE.md](AI_USAGE.md) - AI 활용 상세 기록
- [.claude/skills/](/.claude/skills/) - Custom Skill 정의
