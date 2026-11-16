<div align="center">

# Holub Life Game

**규칙을 바꾸고, 생명 게임의 결말을 예측하다.**

Java 기반 Life Game의 기능 확장 및 설계 개선 프로젝트<br>
Design Patterns · 2026

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square)
![UI](https://img.shields.io/badge/UI-Swing-5382A1?style=flat-square)
![Build](https://img.shields.io/badge/Build-Gradle-02303A?style=flat-square)
![Testing](https://img.shields.io/badge/Testing-JUnit-25A162?style=flat-square)

[진행 현황](#진행-현황) · [실행 방법](#실행-방법) · [설계](#설계) · [테스트](#테스트)

</div>

---

## 소개

[Allen Holub의 Life Game v1.03](https://holub.com/software/life/index.html)을 바탕으로, 기존 기능을 유지하면서 디자인 패턴을 적용해 확장하는 수업 프로젝트입니다.

현재는 **Conway와 HighLife 규칙을 메뉴에서 선택**하거나 **B/S 표기로 규칙을 직접 입력**할 수 있습니다. 앞으로 **현재 배치의 결말을 미리 계산하는 기능**을 추가할 예정입니다.

> 결말 예측은 아직 구현 전입니다. 아래 진행 현황에서 구현된 기능과 계획을 구분합니다.

## 진행 현황

| 작업 | 상태 | 관련 기록 |
| :--- | :---: | :--- |
| Gradle 프로젝트 구성 및 JUnit 환경 준비 | ✅ 완료 | 11/2 |
| 기존 규칙 동작의 회귀 테스트 9개 | ✅ 완료 | PR #2 |
| Conway / HighLife 규칙 선택 | ✅ 완료 | PR #3 |
| 판 전체 테스트 — 실제 64×64 판에서 여러 세대 진행 | ✅ 완료 | PR #7 |
| 알려진 문제 수정 — 판이 정지한 뒤 규칙 변경이 반영되지 않는 문제 | ✅ 완료 | [ADR 002](docs/decisions/002-정지한-판-건너뛰기.md) |
| 규칙 직접 입력 — B/S 표기, 예: `B36/S23` | ✅ 완료 | PR #9 |
| 결말 예측 — 소멸·정지·반복·미확정 | 예정 | — |
| 상태 표시 — 세대 수·세포 수·현재 규칙·예측 | 예정 | — |

**일정** · 코드 동결 11/23 · 제출 11/29

## 실행 방법

**JDK 21**이 필요합니다. 프로젝트 루트에서 Gradle Wrapper를 실행합니다.

```bash
# 프로그램 실행
./gradlew run

# 테스트 실행
./gradlew test
```

Windows에서는 `gradlew.bat run`, `gradlew.bat test`를 사용합니다.

## 설계

### 규칙 선택 · Strategy

원본은 `Resident` 안에 Conway 규칙이 고정되어 있었습니다. 생사 판정을 `Rule` 인터페이스로 분리해, 세포의 구현을 바꾸지 않고 규칙을 교체할 수 있도록 했습니다.

```java
// 변경 전 (Resident.java)
willBeAlive = (neighbors==3 || (amAlive && neighbors==2));

// 변경 후
willBeAlive = holder.current().nextState(amAlive, neighbors);
```

```text
Resident × 4,096
       │ 같은 보관 객체를 공유
       ▼
   RuleHolder ──▶ Rule
                  ├── ConwayRule        B3/S23
                  ├── HighLifeRule      B36/S23
                  └── BirthSurvivalRule B/S 표기 입력
```

| 규칙 | 탄생 조건 | 생존 조건 |
| :--- | :--- | :--- |
| Conway | 살아 있는 이웃 3개 | 살아 있는 이웃 2개 또는 3개 |
| HighLife | 살아 있는 이웃 3개 또는 6개 | 살아 있는 이웃 2개 또는 3개 |
| 직접 입력 | 살아 있는 이웃 수가 B 뒤의 숫자 중 하나 | 살아 있는 이웃 수가 S 뒤의 숫자 중 하나 |

`RuleHolder`는 Singleton으로 만들지 않고 생성자로 전달합니다. 실행 시에는 세포들이 하나를 공유하고, 테스트에서는 각각 독립된 객체를 사용할 수 있습니다.

대안 비교와 선택 근거는 **[ADR 001 — 규칙 전달 방식](docs/decisions/001-규칙-전달-방식.md)**에 정리했습니다.

<details>
<summary>규칙 선택 기능의 변경 범위</summary>

- **추가:** `Rule`, `ConwayRule`, `HighLifeRule`, `BirthSurvivalRule`, `RuleHolder`
- **수정:** `Resident`, `Universe`, `Neighborhood` (정지한 판 문제 수정, [ADR 002](docs/decisions/002-정지한-판-건너뛰기.md))
- **고치지 않은 클래스:** `Cell`, `Clock`

</details>

### 결말 예측 · 구현 예정

현재 배치와 규칙으로 이후 세대를 미리 계산해, 화면이 해당 상태에 도달하기 전에 결과를 보여 주는 기능입니다. 구체적인 설계는 진행 예정입니다.

| 계산 결과 | 표시할 예측 |
| :--- | :--- |
| 살아 있는 세포가 없음 | 소멸 |
| 직전 세대와 같은 배치 | 정지 |
| 이전에 나온 배치가 다시 나타남 | 반복 및 주기 |
| 계산 한도까지 결론을 얻지 못함 | 미확정 |

## 테스트

**JUnit 테스트 51개 통과.** 기존 동작의 회귀 테스트와 규칙별 판정, 규칙 공유 및 교체, 판 전체의 세대 진행을 검증합니다.

- 구조 변경 전에 작성한 `ResidentTest` 9개가 수정 없이 통과합니다.
- 판 전체 테스트는 실제 64×64 판에서 여러 세대의 결과를 확인합니다.
- 테스트 실행 후 HTML 리포트: `build/reports/tests/test/index.html`

<details>
<summary>화면에서 규칙 차이를 확인하는 방법</summary>

가로 3칸을 한 줄 띄워 두 줄 배치하고 Tick을 두 번 진행합니다. Conway에서는 전멸하고, HighLife에서는 3×3 배치가 됩니다.

</details>

## 프로젝트 구조

```text
holub-life/
├── src/main/java/com/holub/
│   ├── life/             게임 및 규칙 구현
│   └── ui, io, tools,
│       asynch/           원본 보조 코드
├── src/test/java/com/holub/
│                         JUnit 테스트
└── docs/decisions/       설계 결정 기록 (ADR)
```

## 개발 방식

- 브랜치에서 작업하고 Pull Request를 통해 머지합니다.
- 새 기능에는 JUnit 테스트를 함께 작성합니다.
- 커밋과 설계 결정 기록에 변경 내용과 이유를 남깁니다.

원본과의 비교 기준은 첫 커밋 `648d9bd`입니다.
