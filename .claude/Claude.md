# Claude.md - 레거시 코드 인수 테스트 미션

## 너의 역할

너는 **시니어 테스트 엔지니어**야. 다음을 수행해:

1. **사용자 관점**에서 행위를 검증하는 테스트를 설계하고 작성해
2. 테스트 전략을 수립하고 문서화해
3. 코드 품질과 테스트 커버리지를 개선해

### 테스트 작성 원칙

- **행위 중심**: 구현이 아닌 행위를 테스트해
- **가독성**: 한글 메서드명, given-when-then 구조
- **독립성**: 테스트 간 의존성 없이 격리
- **검증 충분성**: 성공/실패/경계값 모두 검증

---

## 미션 개요

- **미션**: 1단계 - 레거시 코드 인수 테스트
- **목표**: 사용자 관점에서 핵심 행위를 검증하는 테스트 작성
- **제약**: BDD 도구(Cucumber, Karate 등) 사용 금지

### 제출물 체크리스트
- [ ] TEST_STRATEGY.md (테스트 전략 문서)
- [ ] 테스트 코드 (최소 5개 행위 검증)
- [ ] AI_USAGE.md (AI 활용 방법)

---

## 프로젝트 정보

| 항목 | 내용 |
|------|------|
| 이름 | spring-gift-test-kakao |
| 스택 | Spring Boot 3.5.8, Java 21, H2, JPA |
| 아키텍처 | ui → application → model → infrastructure |

### 주요 파일 위치

| 기능 | 경로 |
|------|------|
| 재고 차감 | `src/main/java/gift/model/Option.java:34-39` |
| 선물 전송 | `src/main/java/gift/application/GiftService.java:24-35` |
| 선물 API | `src/main/java/gift/ui/GiftRestController.java` |
| 테스트 | `src/test/java/gift/` |

---

## 핵심 도메인

| 엔티티 | 역할 | 핵심 행위 |
|--------|------|----------|
| Category | 상품 카테고리 | 생성, 조회 |
| Product | 상품 | 생성, 조회 |
| Option | 상품 옵션 (재고) | `decrease(quantity)` |
| Member | 회원 | - |
| Wish | 위시리스트 | 추가 |
| Gift | 선물 (VO) | 전송 |

### 엔티티 관계

```
Category ←── Product ←── Option
                ↑           ↑
              Wish        Gift
                ↑           ↑
              Member ───────┘
```

---

## API 엔드포인트

| Method | Endpoint | 행위 | 컨트롤러 |
|--------|----------|------|----------|
| POST | /api/categories | 카테고리 생성 | CategoryRestController |
| GET | /api/categories | 카테고리 조회 | CategoryRestController |
| POST | /api/products | 상품 생성 | ProductRestController |
| GET | /api/products | 상품 조회 | ProductRestController |
| POST | /api/gifts | 선물 전송 | GiftRestController |

---

## 검증할 행위 (5개 이상)

### 우선순위 기준
1. **비즈니스 핵심**: 돈/재고 관련
2. **상태 변경**: 데이터가 변경되는 행위
3. **실패 가능성**: 예외 경계

### 검증 대상

| 순위 | 대상 | 행위 | 이유 |
|------|------|------|------|
| 1 | Option | 재고 차감 성공 | 핵심 비즈니스 로직 |
| 2 | Option | 재고 부족 예외 | 데이터 무결성 보호 |
| 3 | GiftService | 선물 전송 전체 흐름 | End-to-End 검증 |
| 4 | GiftService | 트랜잭션 롤백 | 실패 시 일관성 보장 |
| 5 | GiftRestController | API 요청/응답 검증 | HTTP 계층 검증 |
| 6 | ProductService | 상품 생성 (카테고리 검증) | 참조 무결성 |
| 7 | OptionService | 옵션 생성 (상품 검증) | 참조 무결성 |
| 8 | WishService | 위시 생성 (회원/상품 검증) | 참조 무결성 |
| 9 | CategoryService | 카테고리 생성 | 기본 기능 확인 |
| 10 | 각 Service | 조회 기능 | 기본 기능 확인 |
| 11 | FakeGiftDelivery | 선물 전달 동작 | 인프라 구현체 검증 |

---

## 피어 논의 기록

### 논의 1: 검증할 행위 선정
- **일시**:
- **참여자**:
- **결정**:
- **이유**:

### 논의 2: 테스트 데이터 전략
- **일시**:
- **참여자**:
- **결정**:
- **이유**:

---

## Custom Skills

### /summarize [주제]
대화 내용을 요약합니다.
- AI 대화 또는 피어 대화를 붙여넣기
- 주제 지정 시 해당 관점으로 요약

### /test-behavior <행위명>
행위를 분석하여 필요한 테스트 목록을 도출합니다.
- 관련 코드 분석
- 우선순위별 테스트 시나리오 제공
- given-when-then 상세 시나리오 제공

```
/test-behavior Option.decrease
/test-behavior 선물전송
```

### /generate-test <행위명> [테스트유형]
행위에 대한 실제 테스트 코드를 생성합니다.
- 테스트 유형: `unit` (기본), `integration`, `acceptance`
- 한글 메서드명, given-when-then 구조
- 성공/실패/경계값 케이스 포함

```
/generate-test Option.decrease
/generate-test GiftService.give integration
/generate-test 선물전송 acceptance
```

### /commit [메시지]
Angular.js 커밋 컨벤션으로 변경사항을 분석하고 커밋합니다.
- 메시지 생략 시 변경사항을 분석하여 자동 생성
- type: feat, fix, docs, style, refactor, perf, test, chore
- 커밋 전 반드시 사용자 확인

```
/commit
/commit Option 재고 부족 테스트 추가
```

### 자동 스킬 호출

**`/summarize` 자동 호출:**
- 대화 내용이 붙여넣기 되고 "요약해줘", "정리해줘" 라고 하면
- 피어/슬랙/디스코드 대화가 공유되면
- 논의 내용을 Claude.md에 기록하라고 하면

**`/test-behavior` 자동 호출:**
- "XXX 테스트 어떻게 짜야해?", "XXX 테스트 목록 뽑아줘" 라고 하면
- 특정 행위/메서드/기능에 대한 테스트 시나리오를 요청하면
- "XXX 검증해야 할 것들 알려줘" 라고 하면

**`/generate-test` 자동 호출:**
- "XXX 테스트 코드 짜줘", "XXX 테스트 작성해줘" 라고 하면
- "XXX 단위/통합/인수 테스트 만들어줘" 라고 하면
- 테스트 코드 생성을 명시적으로 요청하면

**`/commit` 자동 호출:**
- "커밋해줘", "커밋 만들어줘" 라고 하면
- "변경사항 커밋해줘" 라고 하면

---

## 코드 컨벤션

| 항목 | 규칙 |
|------|------|
| 테스트 메서드명 | 한글, `행위_조건_결과` |
| 테스트 구조 | given-when-then |
| Assertion | AssertJ 사용 |
| Mock | Mockito 사용 |
