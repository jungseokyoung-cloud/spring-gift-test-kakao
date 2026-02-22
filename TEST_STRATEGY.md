# TEST_STRATEGY.md - 테스트 전략 문서

## 1. 검증할 행위 목록: 어떤 행위를 선택했는가? 기준은?

### 선정 기준
1. **상태 변경 우선**: 데이터가 변경되는 행위를 높은 우선순위로 설정
2. **의존성 최소화**: 의존성이 가장 없는 Unit Test부터 시작
3. **비즈니스 핵심**: 돈/재고와 관련된 중요 로직
4. **사용자 관점**: 실제 사용자가 수행하는 행위

### 검증 대상 행위 (우선순위순)

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

## 2. 테스트 데이터 전략: 어떻게 준비하고 정리하는가?

### 2.1 계층별 데이터 준비 방식

| 테스트 계층 | 데이터 준비 방식 | 도구 |
|------------|-----------------|------|
| Domain (Unit) | 생성자 직접 호출 | - |
| Service (Unit) | Mock 객체 | Mockito |
| Service (Integration) | @BeforeEach + Repository | JPA |
| Controller (Acceptance) | API 호출 | TestRestTemplate |

### 2.2 테스트 픽스처 구조

```java
// 단위 테스트: 직접 생성
Product product = new Product("상품", 10000, "url", category);
Option option = new Option("기본", 10, product);

// 통합 테스트: Repository 저장
@BeforeEach
void setUp() {
    category = categoryRepository.save(new Category("테스트"));
    product = productRepository.save(new Product("상품", 10000, "url", category));
    option = optionRepository.save(new Option("기본", 10, product));
}

// 인수 테스트: API 호출
ResponseEntity<Category> response = restTemplate.postForEntity(
    "/api/categories", new CreateCategoryRequest("테스트"), Category.class);
```

### 2.3 데이터 정리 (격리) 전략

| 방식 | 적용 대상 | 설명 |
|------|----------|------|
| `@Transactional` | 통합 테스트 | 테스트 후 자동 롤백 |
| `@DirtiesContext` | 컨텍스트 오염 시 | 스프링 컨텍스트 재생성 |
| `@BeforeEach` 초기화 | 모든 테스트 | 테스트 픽스처 재설정 |


| 테스트 유형            | 권장 격리 방식                          |
|------------------------|------------------------------------------|
| Domain 단위 테스트     | `@BeforeEach`                           |
| Service 통합 테스트    | `@Transactional` + `@BeforeEach`        |
| Controller 인수 테스트 | `@BeforeEach` + 수동 정리 또는 `@Sql`   |

**DirtiesContext** 를 사용하게 된다면 반드시 사용자한테 알림을 보낼 수 있도록 해.

### 2.4 데이터 의존성 순서

```
Category → Product → Option → Member → Gift
   ↓          ↓         ↓        ↓
 (선행)     (선행)    (선행)   (선행)
```

---

## 3. 검증 전략: 무엇을 어떻게 검증하는가?

### 3.1 계층별 검증 전략

| 계층 | 검증 대상 | 검증 방법 |
|------|----------|----------|
| Domain | 상태 변경 | `assertThat(option.getQuantity()).isEqualTo(7)` |
| Domain | 예외 발생 | `assertThatThrownBy().isInstanceOf(...)` |
| Service | Mock 호출 | `verify(repository).save(any())` |
| Service | 반환값 | `assertThat(result).isNotNull()` |
| Controller | HTTP 상태 | `assertThat(response.getStatusCode()).isEqualTo(OK)` |
| Controller | 응답 본문 | `assertThat(response.getBody().getName()).isEqualTo(...)` |

### 3.2 행위별 검증 포인트

**Option.decrease() - 재고 차감**
```java
// 성공 케이스
assertThat(option.getQuantity()).isEqualTo(기대값);

// 실패 케이스 (재고 부족)
assertThatThrownBy(() -> option.decrease(초과량))
    .isInstanceOf(IllegalStateException.class);

// 경계값 (재고 = 요청)
option.decrease(전체재고);
assertThat(option.getQuantity()).isZero();
```

**GiftService.give() - 선물 전송**
```java
// 성공: 재고 감소 + 배송 호출
verify(option).decrease(quantity);
verify(giftDelivery).deliver(any(Gift.class));

// 실패: 트랜잭션 롤백 확인
assertThat(option.getQuantity()).isEqualTo(초기값);
```

**인수 테스트 - API 검증**
```java
// HTTP 응답 검증
assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

// "다음 행동"으로 이전 행동 검증
ResponseEntity<List> list = restTemplate.getForEntity("/api/products", List.class);
assertThat(list.getBody()).hasSize(1);
```

### 3.3 "다음 행동" 검증 패턴

| 이전 행동 | 다음 행동 (검증용) | 검증 내용 |
|----------|-------------------|----------|
| 카테고리 생성 | 카테고리 조회 | 생성된 카테고리 존재 |
| 상품 등록 | 상품 조회 | 등록된 상품 존재 |
| 선물 전송 | 옵션 재고 조회 | 재고 감소 확인 |

---

## 4. 주요 의사결정: 논의 과정에서 중요한 선택과 이유

### 4.1 테스트 피라미드 구성

| 결정 | 선택 | 이유 |
|------|------|------|
| 기반 | Domain 단위 테스트 | 빠른 피드백, 핵심 로직 검증 |
| 중간 | Service 통합 테스트 | 트랜잭션, 실제 DB 동작 |
| 상단 | Controller 인수 테스트 | 사용자 관점 E2E |

### 4.2 Mock vs 실제 객체

| 대상 | 결정 | 이유 |
|------|------|------|
| Repository | Mock (단위) / 실제 (통합) | 단위는 빠르게, 통합은 실제로 |
| GiftDelivery | Mock | 외부 시스템 의존 제거 |
| Domain 객체 | 실제 | 비즈니스 로직 검증 필요 |

### 4.3 테스트 데이터 준비 방식

| 결정 | 선택 | 이유 |
|------|------|------|
| 인수 테스트 | API 호출 우선 | 사용자 관점, 실제 흐름 |
| 보조 | Repository 직접 | API 미구현 (옵션, 회원) |

### 4.4 테스트 네이밍

| 결정 | 선택 | 이유 |
|------|------|------|
| 언어 | 한글 | 가독성, 문서화 효과 |
| 형식 | `행위_조건_결과` | 명확한 의도 전달 |

```java
void 재고가_충분하면_정상_차감된다()
void 재고가_부족하면_예외가_발생한다()
void 선물_전송_시_재고가_감소한다()
```

### 4.5 검증 범위

| 결정 | 선택 | 이유 |
|------|------|------|
| 성공 케이스 | 필수 | 기본 동작 확인 |
| 실패 케이스 | 비즈니스 중요도 기준 | 모든 케이스 불필요 |
| 경계값 | 재고 관련만 | 돈과 직결 |

---

## 5. 피어 논의 기록

### 논의 1: 검증할 행위 선정
- **일시**:
- **참여자**:
- **결정**: Option.decrease()를 최우선으로
- **이유**: 재고 = 돈, 비즈니스 핵심

### 논의 2: 테스트 데이터 전략
- **일시**:
- **참여자**:
- **결정**: 계층별 분리 (Unit → Mock → Integration)
- **이유**: 빠른 피드백 + 실제 동작 검증 균형

### 논의 3: Mock vs 실제 객체
- **일시**:
- **참여자**:
- **결정**: Repository는 Mock, Domain은 실제
- **이유**: DB 접근 느림, 비즈니스 로직은 실제 검증
