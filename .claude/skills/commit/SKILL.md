---
name: commit
description: Angular.js 커밋 컨벤션으로 변경사항을 분석하고 커밋합니다.
argument-hint: "[메시지] (생략 시 자동 생성)"
---

# Angular.js 커밋 컨벤션 커밋 Skill

코드 변경사항을 분석하여 Angular.js 커밋 컨벤션에 맞는 커밋 메시지를 생성하고 커밋합니다.

## 입력

$ARGUMENTS - 커밋 메시지 힌트 (선택). 생략 시 변경사항을 분석하여 자동 생성.

## 실행 절차

### 1단계: 변경사항 확인

다음 명령어를 **병렬**로 실행하여 현재 상태를 파악한다:

- `git status` (untracked 파일 포함)
- `git diff` (unstaged 변경사항)
- `git diff --cached` (staged 변경사항)
- `git log --oneline -5` (최근 커밋 참고)

### 2단계: 변경사항 분석

변경된 파일과 diff 내용을 읽고 다음을 판단한다:

- **type**: 변경의 성격
- **scope**: 변경이 영향을 미치는 모듈/영역
- **subject**: 변경 내용 요약

### 3단계: 커밋 메시지 생성

Angular.js 커밋 컨벤션 형식으로 메시지를 생성한다.

### 4단계: 사용자 확인

생성된 커밋 메시지를 사용자에게 보여주고 확인을 받는다.
반드시 AskUserQuestion 도구를 사용하여 확인을 받는다.

### 5단계: 커밋 실행

사용자가 승인하면 다음을 수행한다:

1. 변경된 파일을 개별적으로 `git add` (민감 파일 제외)
2. 커밋 메시지로 `git commit` 실행
3. `git status`로 결과 확인

## Angular.js 커밋 컨벤션

### 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### type (필수)

| type | 설명 |
|------|------|
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 변경 |
| style | 코드 의미에 영향 없는 변경 (포맷팅, 세미콜론 등) |
| refactor | 버그 수정도 기능 추가도 아닌 코드 변경 |
| perf | 성능 개선 |
| test | 테스트 추가 또는 수정 |
| chore | 빌드 프로세스, 보조 도구, 라이브러리 변경 |

### scope (선택)

변경이 영향을 미치는 영역. 이 프로젝트의 scope 예시:

| scope | 대상 |
|-------|------|
| option | Option 도메인 |
| product | Product 도메인 |
| category | Category 도메인 |
| gift | Gift / GiftService |
| wish | Wish 도메인 |
| member | Member 도메인 |
| config | 설정 파일 |

### subject (필수)

- 명령형 현재 시제 사용 ("change" O, "changed" X, "changes" X)
- 첫 글자 소문자
- 끝에 마침표 없음
- 한글 허용

### body (선택)

- 변경의 동기와 이전 동작과의 차이를 설명
- 명령형 현재 시제 사용

### footer (선택)

- Breaking Changes 기재
- 관련 이슈 참조 (e.g., `Closes #123`)

## 커밋 메시지 예시

```
feat(option): Option.decrease 재고 차감 기능 추가
```

```
test(option): Option.decrease 단위 테스트 추가

재고 차감 성공/실패/경계값 케이스 검증
```

```
fix(gift): 선물 전송 시 재고 부족 예외 처리 누락 수정
```

```
refactor(product): 상품 생성 로직을 서비스 계층으로 이동
```

```
docs: 테스트 전략 문서 추가
```

```
chore: Gradle 의존성 업데이트
```

## 주의사항

- `.env`, `credentials`, `secret` 등 민감 파일은 절대 커밋하지 않는다
- 커밋 전 반드시 사용자 확인을 받는다
- `git add .` 또는 `git add -A` 대신 파일을 개별 지정한다
- `--amend`는 사용자가 명시적으로 요청한 경우에만 사용한다
- 커밋 메시지는 HEREDOC으로 전달한다

## Co-Authored-By

커밋 메시지 마지막에 항상 다음을 추가한다:

```
Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
```
