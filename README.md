# Holub Life Game 확장 — 설계패턴 2026 텀프로젝트

Allen Holub의 Life Game(Java/Swing)에 설계패턴을 적용해 기능을 확장하고 설계를 개선하는 과제입니다.

- 원본: Allen Holub, Life v1.03 — https://holub.com/software/life/index.html
- 이 저장소의 첫 커밋(`648d9bd`)이 원본 소스 그대로입니다.

## 설계 개선 및 기능 확장

| | 무엇을 하는가 | 적용한 패턴 | 상태 |
|---|---|---|---|
| **확장 1 — 규칙 선택** | 생존 규칙을 실행 중에 바꾼다 (Conway, HighLife, 직접 입력) | Strategy | 메뉴 선택 완료 · 직접 입력 예정 |
| **확장 2 — 결말 예측** | 지금 배치와 규칙으로 앞을 미리 계산해 결말(소멸 / 정지 / 반복 / 미확정)을 보여 준다 | 설계 중 | 예정 |

## 확장 1 — 규칙 선택 (Strategy)

**원본의 문제**: 생존 규칙이 `Resident` 클래스 안에 숫자로 고정되어 있어, 다른 규칙으로 실행하려면 소스를 고쳐야 했습니다.

```java
// 변경 전 (Resident.java)
willBeAlive = (neighbors==3 || (amAlive && neighbors==2));

// 변경 후
willBeAlive = holder.current().nextState(amAlive, neighbors);
```

**바뀐 구조**

```
Resident × 4,096  ──▶  RuleHolder (1개)  ──▶  Rule (인터페이스)
                       현재 규칙을 보관          ├─ ConwayRule    (B3/S23)
                                                └─ HighLifeRule  (B36/S23)
```

- 규칙 판정은 `Rule` 인터페이스의 구현체가 맡습니다. 규칙을 추가할 때 `Resident`를 고치지 않습니다.
- 모든 `Resident`가 `RuleHolder` 하나를 함께 참조합니다. 메뉴에서 규칙을 바꾸면 `RuleHolder` 안의 `Rule`만 교체됩니다.

**설계 결정** (대안 비교와 이유는 [docs/decisions/001-규칙-전달-방식.md](docs/decisions/001-규칙-전달-방식.md))

| 결정 | 고르지 않은 대안 |
|---|---|
| 규칙에 관한 것을 `Cell` 인터페이스에 드러내지 않는다 | `figureNextState`의 매개변수로 전달 / `Cell`에 `setRule` 추가 |
| `RuleHolder`를 Singleton으로 만들지 않고 생성자로 전달한다 | `RuleHolder.instance()` |

**변경 범위**

- 새로 만든 클래스: `Rule`, `ConwayRule`, `HighLifeRule`, `RuleHolder`
- 고친 클래스: `Resident`, `Universe`
- 고치지 않은 클래스: `Cell`, `Neighborhood`, `Clock`

**확인**

- JUnit 테스트 34개 통과. 구조를 바꾸기 전에 작성한 `ResidentTest` 9개가 수정 없이 통과합니다.
- 화면에서: 가로 3칸을 한 줄 띄워 두 줄 놓고 Tick을 두 번 누르면, Conway는 전멸하고 HighLife는 3×3이 됩니다.

## 확장 2 — 결말 예측 (예정)

Life Game은 같은 배치와 같은 규칙이면 항상 같은 순서로 진행합니다. 화면과 별도로 지금 배치를 앞으로 미리 돌려, 화면이 도달하기 전에 결말을 알려 줍니다.

| 미리 돌리다 만난 것 | 예측 |
|---|---|
| 세포 0개 | 소멸 |
| 직전 세대와 같은 모양 | 정지 |
| 몇 세대 전과 같은 모양 | 반복 (주기) |
| 한도까지 결론 없음 | 미확정 |

## 진행 상황

| 순서 | 내용 | 상태 |
|---|---|---|
| — | 원본을 Gradle 프로젝트로 구성, JUnit 환경 준비 (11/2) | 완료 |
| — | 기존 규칙 동작의 회귀 테스트 9개 (PR #2) | 완료 |
| — | 확장 1: Rule 메뉴로 Conway / HighLife 선택 (PR #3) | 완료 |
| 1 | 판 전체 테스트 (실제 64×64 판에서 여러 세대 진행) | 진행 중 |
| 2 | 알려진 문제 수정: 판이 정지한 뒤에는 규칙을 바꿔도 반영되지 않음 | 예정 |
| 3 | 확장 1: 규칙 직접 입력 (B/S 표기, 예: `B36/S23`) | 예정 |
| 4 | 확장 2: 결말 예측 | 예정 |
| 5 | 창 아래 상태 표시 한 줄 (세대 수, 세포 수, 현재 규칙, 예측) | 예정 |

일정: 코드 동결 11/23 · 제출 11/29

## 팀원이 합류하면 맡을 수 있는 작업 (후보)

위 표의 1~5번은 이미 진행 중이므로 제외했습니다. 아래는 그 작업과 동시에 진행할 수 있는 것들이고, 서로 겹치지 않는 순서로 나눴습니다.

**가. 바로 시작할 수 있는 작업** (소스 코드를 고치지 않음)

| 작업 | 결과물 |
|---|---|
| UML 다이어그램 (원본과 확장 1) | 기존 기능의 유스케이스, 원본의 클래스 다이어그램, 확장 1(규칙 선택)의 변경 전 / 후 |
| 기존 기능 시연 | Clear, Load, Store, Halt, Tick, 속도 4단계가 그대로 동작하는 영상 |

확장 2의 다이어그램(클래스, 시퀀스)은 설계가 정해진 뒤에 그릴 수 있습니다.

**나. 기존 기능 테스트** (테스트 코드만 추가)

| 작업 | 결과물 |
|---|---|
| 원본 기능의 JUnit 테스트 보강 | Clear, 속도 선택(Go 메뉴) 등 아직 테스트가 없는 기능의 테스트 |

**다. 새 기능** (기존 파일을 고치므로 시작 전에 먼저 상의)

| 작업 | 주로 고치는 파일 | 겹치는 곳 |
|---|---|---|
| 시작 / 멈춤 / 한 세대 진행 버튼 | `Clock`, `Universe` | `Universe`는 5번 작업에서도 고침 |
| 방금 태어난 세포와 죽은 세포를 다른 색으로 표시 | `Resident` | `Resident`는 확장 1에서 고친 파일 |
| 원본 버그 수정: Load로 불러온 세포가 구역 경계(8칸마다)에서 잘못 진행함 | `Neighborhood` | 2번 작업에서 `Neighborhood`를 고칠 수 있음 |

## 작업 방식

- `main`에 직접 커밋하지 않습니다. 브랜치 → Pull Request → 머지.
- 커밋 메시지는 한국어로, 무엇을 왜 했는지 적습니다.
- 새 기능에는 JUnit 테스트를 함께 넣습니다.

## 실행 방법

필요한 것: JDK 21

```
./gradlew run     # 프로그램 실행
./gradlew test    # 테스트 실행 (결과: build/reports/tests/test/index.html)
```

## 폴더 구조

```
src/main/java/com/holub/   소스 (life: 게임 본체 / ui, io, tools, asynch: 원본 보조 코드)
src/test/java/com/holub/   JUnit 테스트
docs/decisions/            설계 결정 기록 (ADR)
```
