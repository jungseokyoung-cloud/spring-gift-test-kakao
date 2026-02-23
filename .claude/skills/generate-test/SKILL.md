---
name: generate-test
description: 행위에 대한 테스트 코드를 생성합니다.
argument-hint: "<행위명> [테스트유형] (예: Option.decrease unit, 선물전송 integration)"
---

# 테스트 코드 생성 Skill

행위를 분석하여 실제 테스트 코드를 생성합니다.

## 입력

$ARGUMENTS - 행위명과 테스트 유형 (선택)

**테스트 유형:**
- `unit` - 단위 테스트 (기본값)
- `integration` - 통합 테스트 (@SpringBootTest)
- `acceptance` - 인수 테스트 (TestRestTemplate)

**예시:**
- `/generate-test Option.decrease` → 단위 테스트
- `/generate-test Option.decrease unit` → 단위 테스트
- `/generate-test GiftService.give integration` → 통합 테스트
- `/generate-test 선물전송 acceptance` → 인수 테스트

## 핵심 원칙

1. **프로덕션 코드 수정 금지**: 테스트가 실패하더라도 프로덕션 코드를 변경하지 않는다. 생성자가 없으면 리플렉션을, 접근이 제한되면 우회 방법을 사용한다. 검증에 초점을 둔다.
2. **구현이 아닌 행동을 검증한다**: 구현 의존 테스트는 리팩토링 시 함께 깨진다. 행동 중심 테스트는 내부 변경에 강하다.
3. **테스트 유형은 상황에 맞게 판단**: 테스트 유형이 명시되지 않으면 행위의 특성을 분석하여 적절한 유형을 선택한다.
   - 도메인 로직만 검증 → Unit
   - DB/트랜잭션 포함 → Integration
   - HTTP 전체 흐름 → Acceptance

## 인수 테스트 품질 기준

생성된 인수 테스트는 다음을 모두 만족해야 한다:

1. **행동 중심**: 구현이 아닌 사용자 관점의 시나리오인가?
2. **외부 관찰 가능**: 사용자가 볼 수 있는 결과를 검증하는가?
3. **계약 보호**: 핵심 비즈니스 규칙을 보호하는가?
4. **실패 의미 전달**: 실패 시 비즈니스 의미를 바로 알 수 있는가?
5. **리팩토링 내성**: 내부 구현이 바뀌어도 유지되는가?

## 코드 생성 규칙

### 공통 규칙
- **메서드명**: 한글, `[상황]_[행동]하면_[결과]한다` 형식
- **구조**: given-when-then 주석 포함
- **Assertion**: AssertJ 사용
- **Mock**: Mockito 사용 (필요 시)

### 테스트 이름은 설계 도구

테스트 이름은 단순 작명이 아니라, 어떤 행동을 검증하는지 드러내는 설계 도구다.
응답 코드만이 아닌 행동의 결과를 이름에 담는다.

| | 예시 |
|---|---|
| 나쁜 예 | `선물_전송_API_성공` |
| 좋은 예 | `선물이_전송되면_재고가_감소한다` |

### 계층별 템플릿

#### Unit Test (Domain)
```java
class [Entity]Test {

    private [Entity] [entity];

    @BeforeEach
    void setUp() {
        // 픽스처 초기화
    }

    @Test
    void 행위_조건_결과() {
        // given

        // when

        // then
        assertThat(...).isEqualTo(...);
    }
}
```

#### Integration Test (Service)
```java
@SpringBootTest
class [Service]Test {

    @Autowired
    private [Service] [service];

    @Autowired
    private [Repository] [repository];

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
    }

    @Test
    void 행위_조건_결과() {
        // given

        // when

        // then
    }
}
```

#### Acceptance Test (Controller)
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class [Feature]AcceptanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private [Repository] [repository];

    @BeforeEach
    void setUp() {
        [repository].deleteAll();
        // 테스트 데이터 준비
    }

    @Test
    void [상황]_[행동]하면_[결과]한다() {
        // given
        HttpHeaders headers = new HttpHeaders();
        [Request] request = new [Request](...);

        // when
        ResponseEntity<[Response]> response = restTemplate.exchange(
            "/api/...",
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            [Response].class
        );

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        // 응답만이 아닌, 행동의 결과를 검증한다
    }
}
```

## 출력 형식

```markdown
## 생성된 테스트 코드: [행위명]

### 테스트 유형: [Unit/Integration/Acceptance]

### 파일 위치
`src/test/java/gift/[package]/[ClassName]Test.java`

### 코드

\`\`\`java
// 전체 테스트 클래스 코드
\`\`\`

### 포함된 테스트 케이스
1. 성공 케이스: `행위_조건_성공결과()`
2. 실패 케이스: `행위_조건_실패결과()`
3. 경계값: `행위_경계조건_결과()`

### 실행 방법
\`\`\`bash
./gradlew test --tests "[ClassName]Test"
\`\`\`
```

## 검증 포인트 자동 포함

| 테스트 유형 | 검증 대상 |
|------------|----------|
| Unit | 상태 변경, 예외 발생 |
| Integration | DB 반영, 트랜잭션, Mock 호출 |
| Acceptance | HTTP 상태, 행동의 결과 (상태 변경, 데이터 반영 등) |

## @DirtiesContext 사용 금지

`@DirtiesContext`는 스프링 컨텍스트를 재생성하여 테스트 속도를 크게 저하시킨다.
`@DirtiesContext` 없이 테스트가 격리되도록 설계한다.

- `@Transactional` 롤백으로 데이터 격리
- `@BeforeEach`에서 `deleteAll()`로 데이터 초기화
- 테스트별 고유 데이터 사용으로 충돌 방지

## 예시

### 입력
```
/generate-test Option.decrease unit
```

### 출력
```java
package gift.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptionTest {

    private Category category;
    private Product product;
    private Option option;

    @BeforeEach
    void setUp() {
        category = new Category("테스트 카테고리");
        product = new Product("테스트 상품", 10000, "http://image.url", category);
    }

    @Test
    void 재고가_충분하면_정상_차감된다() {
        // given
        option = new Option("기본 옵션", 10, product);

        // when
        option.decrease(3);

        // then
        assertThat(option.getQuantity()).isEqualTo(7);
    }

    @Test
    void 재고가_부족하면_예외가_발생한다() {
        // given
        option = new Option("기본 옵션", 2, product);

        // when & then
        assertThatThrownBy(() -> option.decrease(5))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 재고와_요청량이_같으면_재고가_0이_된다() {
        // given
        option = new Option("기본 옵션", 5, product);

        // when
        option.decrease(5);

        // then
        assertThat(option.getQuantity()).isZero();
    }
}
```
