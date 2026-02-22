---
name: test-behavior
description: 행위를 분석하여 필요한 테스트 목록을 도출합니다.
argument-hint: "<행위명> (예: Option.decrease, 선물전송, 카테고리생성)"
---

# 테스트 시나리오 생성 Skill

사용자가 제공한 행위를 분석하여 필요한 테스트 목록을 도출합니다.

## 입력

$ARGUMENTS - 분석할 행위명

예시:
- `Option.decrease`
- `선물전송`
- `GiftService.give`
- `카테고리생성`

## 핵심 원칙

1. **프로덕션 코드 수정 금지**: 테스트가 실패하더라도 프로덕션 코드를 변경하지 않는다. 검증에 초점을 두고, 프로덕션 코드의 제약 안에서 테스트를 작성한다.
2. **테스트 유형은 상황에 맞게 판단**: 무조건 단위 테스트를 선택하지 않는다. 행위의 특성에 따라 적절한 테스트 유형을 제안한다.
   - 도메인 로직만 검증 → Unit
   - DB/트랜잭션 포함 → Integration
   - HTTP 전체 흐름 → Acceptance

## 분석 프로세스

1. **코드 분석**: 해당 행위와 관련된 코드를 찾아 분석
2. **의존성 파악**: 호출하는 메서드, 사용하는 Repository 등 확인
3. **예외 경로 확인**: 발생 가능한 예외 상황 도출
4. **경계값 식별**: 비즈니스 규칙의 경계 조건 확인

## 출력 형식

```markdown
## 행위 분석: [행위명]

### 관련 코드
- 파일: `경로:라인`
- 핵심 로직 설명

### 테스트 시나리오 (우선순위순)

| 우선순위 | 테스트 케이스 | 유형 | 검증 포인트 |
|---------|--------------|------|------------|
| **높** | 성공 케이스 | Unit/Integration | 상태 변경 확인 |
| **높** | 실패 케이스 | Unit | 예외 발생 확인 |
| 중 | 경계값 | Unit | 경계 조건 확인 |
| 낮 | 부가 케이스 | Unit | 추가 검증 |

### 상세 시나리오

#### 1. [테스트명]
- **Given**: 사전 조건
- **When**: 실행 행위
- **Then**: 기대 결과
- **검증 방법**: `assertThat(...)`

#### 2. [테스트명]
...

### 의존성 & Mock 대상
- Mock 필요: `XXXRepository`, `XXXService`
- 실제 사용: Domain 객체

### 테스트 데이터
- 필요한 픽스처 목록
- 데이터 의존성 순서
```

## 우선순위 기준

1. **높음**: 비즈니스 핵심 (돈/재고), 데이터 무결성
2. **중간**: 참조 무결성, 상태 변경
3. **낮음**: 조회, 단순 CRUD

## 예시

### 입력
```
/test-behavior Option.decrease
```

### 출력
```markdown
## 행위 분석: Option.decrease()

### 관련 코드
- 파일: `src/main/java/gift/model/Option.java:34-39`
- 재고 차감 로직, 재고 부족 시 IllegalStateException 발생

### 테스트 시나리오 (우선순위순)

| 우선순위 | 테스트 케이스 | 유형 | 검증 포인트 |
|---------|--------------|------|------------|
| **높** | 재고 충분 시 정상 차감 | Unit | quantity 감소 확인 |
| **높** | 재고 부족 시 예외 발생 | Unit | IllegalStateException |
| 중 | 재고와 요청량 동일 시 | Unit | quantity = 0 |
| 낮 | 0개 차감 요청 | Unit | 변화 없음 |

### 상세 시나리오

#### 1. 재고가_충분하면_정상_차감된다
- **Given**: Option(quantity=10)
- **When**: decrease(3)
- **Then**: quantity = 7
- **검증**: `assertThat(option.getQuantity()).isEqualTo(7)`

#### 2. 재고가_부족하면_예외가_발생한다
- **Given**: Option(quantity=2)
- **When**: decrease(5)
- **Then**: IllegalStateException
- **검증**: `assertThatThrownBy(() -> option.decrease(5)).isInstanceOf(IllegalStateException.class)`
```
