# 학습 과정에서 발견한 것들

## 1. AI의 한계: 리플렉션 workaround

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

## 2. 프롬프트 설계의 중요성

Claude.md에 역할과 규칙을 명확히 정의했습니다:

```markdown
## 너의 역할
너는 **시니어 테스트 엔지니어**야.

### 테스트 작성 원칙
- **행위 중심**: 구현이 아닌 행위를 테스트해
- **가독성**: 한글 메서드명, given-when-then 구조
```

## 3. Skill 자동 호출 설정

Claude.md에 자동 트리거 조건을 추가했습니다:

```markdown
**`/test-behavior` 자동 호출:**
- "XXX 테스트 어떻게 짜야해?"
- "XXX 테스트 목록 뽑아줘"

**`/generate-test` 자동 호출:**
- "XXX 테스트 코드 짜줘"
```

## 4. Cucumber BDD 도입 과정

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

## 5. PostgreSQL + Docker Compose 전환

H2 in-memory DB에서 PostgreSQL로 전환하면서 배운 점들:

- **spring-boot-docker-compose**: `compose.yml`을 감지하여 컨테이너를 자동 시작하고 DataSource를 자동 설정
- **@ActiveProfiles("test")**: 프로파일로 dev/test 설정 분리 (ddl-auto, show-sql 등)
- **DatabaseCleaner**: `deleteAll()` 대신 `TRUNCATE CASCADE`로 테스트 격리 — FK 제약을 무시하고 빠르게 초기화
- **@Table(name = "options")**: PostgreSQL 예약어 충돌 방지를 위해 테이블명 변경

## 6. Application 컨테이너화

- **Multi-stage build**: 빌드(JDK)와 실행(JRE) 이미지 분리로 크기 최소화
- **WebEnvironment.NONE**: 내장 서버 없이 외부 Docker 컨테이너에 요청
- **TestRestTemplate vs RestTemplate**: TestRestTemplate은 내장 서버용, RestTemplate은 외부 서버용
- **Docker Compose profiles**: `profiles: ["e2e"]`로 E2E 때만 app 컨테이너 실행
