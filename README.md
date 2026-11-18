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

**Conway와 HighLife 규칙을 메뉴에서 선택**하거나 **B/S 표기로 규칙을 직접 입력**할 수 있습니다. 현재 배치의 **소멸·정지·반복 여부를 미리 계산**하고, 창 아래에 **세대 수 · 살아 있는 세포 수 · 현재 규칙 · 예측 결과**를 표시합니다. 예측한 결말이 소멸이나 정지이고 판이 그 세대에 도달하면 **자동 진행을 멈춥니다.**

## 진행 현황

| 작업 | 상태 | 관련 기록 |
| :--- | :---: | :--- |
| Gradle 프로젝트 구성 및 JUnit 환경 준비 | ✅ 완료 | 11/2 |
| 기존 규칙 동작의 회귀 테스트 9개 | ✅ 완료 | PR #2 |
| Conway / HighLife 규칙 선택 | ✅ 완료 | PR #3 |
| 판 전체 테스트 — 실제 64×64 판에서 여러 세대 진행 | ✅ 완료 | PR #7 |
| 알려진 문제 수정 — 판이 정지한 뒤 규칙 변경이 반영되지 않는 문제 | ✅ 완료 | [ADR 002](docs/decisions/002-정지한-판-건너뛰기.md) |
| 규칙 직접 입력 — B/S 표기, 예: `B36/S23` | ✅ 완료 | PR #9 |
| 결말 예측 — 소멸·정지·반복·미확정 | ✅ 완료 | [PR #10](https://github.com/you661012/holub-life/pull/10) · [ADR 003](docs/decisions/003-판을-미리-돌리는-방법.md) |
| 상태 표시 — 세대 수·세포 수·현재 규칙·예측 | ✅ 완료 | [PR #10](https://github.com/you661012/holub-life/pull/10) · [ADR 004](docs/decisions/004-규칙이-바뀌면-다시-예측하게-하는-방법.md), [ADR 005](docs/decisions/005-예측의-입력과-계산을-다루는-방법.md) |
| 자동 정지 — 예측한 소멸·정지 세대에 도달하면 시계를 멈춤 | ✅ 완료 | [PR #13](https://github.com/you661012/holub-life/pull/13) · [확장 2 문서](docs/features/확장2-결말-예측.md) |

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

### 결말 예측 · 상태 표시

현재 판과 규칙을 함께 읽어 둔 뒤, 별도 작업 스레드에서 500세대를 한도로 결말을 계산합니다. 실제 판과 예측기는 같은 `Rule` 인터페이스를 사용합니다.

창 아래 상태 표시는 두 줄입니다. **윗줄에는 세대 수·살아 있는 세포 수·현재 규칙**, **아랫줄에는 예측한 결말**을 표시합니다. 결말은 글자색으로도 구분합니다.

| 판정 | 의미 | 화면 표시 예시 | 글자색 |
| :--- | :--- | :--- | :--- |
| 소멸 | 빈 판이 되고 다음 세대에도 비어 있음 | `extinct` | 빨강 |
| 정지 | 직전 세대와 같은 배치 | `still` | 파랑 |
| 반복 | 이전에 나온 배치가 다시 나타남 | `repeating (period 2)` | 초록 |
| 미확정 | 500세대까지 돌렸지만 결말을 확인하지 못함 | `undecided within 500 generations` | 회색 |

계산 중에는 `calculating...`을 회색으로 표시합니다. 소멸·정지·반복에 아직 도달하지 않았다면 남은 세대 수도 보여 줍니다. 클릭·Clear·Load·규칙 변경 시 다시 예측하며, 이전 요청의 결과는 화면에 반영하지 않습니다.

예측한 결말이 소멸이나 정지이고 판이 그 세대에 도달하면 시계를 멈춥니다. 더 진행해도 판이 변하지 않기 때문입니다. 판을 직접 살펴 판단하는 것은 아닙니다. 반복은 판이 계속 움직이므로 멈추지 않습니다. 미확정이나 계산 중에도 멈추지 않습니다. 미확정은 판이나 규칙을 바꾸기 전에는 다시 예측하지 않습니다. 한 세대씩 넘기는 Tick은 그대로 쓸 수 있습니다.

Glider처럼 자리만 옮겨 가는 모양을 "이동"으로 따로 알려 주지는 않습니다. 그런 모양도 다른 배치와 똑같이 한도(500세대)까지 미리 돌려서 예측하고, 그 안에 소멸·정지·반복을 확인하지 못하면 미확정으로 표시합니다. 검토한 내용은 [확장 2 문서](docs/features/확장2-결말-예측.md)의 한계 절에 있습니다.

설계와 대안 비교는 [ADR 003 — 판을 미리 돌리는 방법](docs/decisions/003-판을-미리-돌리는-방법.md), [ADR 004 — 규칙 변경 알림](docs/decisions/004-규칙이-바뀌면-다시-예측하게-하는-방법.md), [ADR 005 — 예측의 입력과 계산](docs/decisions/005-예측의-입력과-계산을-다루는-방법.md)에 정리했습니다.

<details>
<summary>결말 예측과 상태 표시의 변경 범위</summary>

- **추가:** `Generation`, `EndingPredictor`, `Ending`, `LiveCellCollector`, `PredictionRequest`, `PredictionWorker`, `StatusLine`
- **수정:** `Universe` (예측 요청, 상태 갱신, 자동 정지), `Life` (상태 표시줄 배치), `Resident` (`transition()` 반환값 수정)
- **규칙 이름 표시:** `ConwayRule`, `HighLifeRule`, `BirthSurvivalRule`에 `toString()` 추가

</details>

## 테스트

**JUnit 테스트 113개 통과.** 기존 동작과 규칙 교체뿐 아니라 결말 예측, 비동기 요청 처리, 상태 표시를 검증합니다.

- 구조 변경 전에 작성한 `ResidentTest` 9개가 수정 없이 통과합니다.
- 판 전체 테스트는 실제 64×64 판에서 여러 세대의 결과를 확인합니다.
- `B0`를 포함한 직접 입력 규칙에서도 예측용 세대 계산과 실제 판의 결과를 비교합니다.
- 테스트 실행 후 HTML 리포트: `build/reports/tests/test/index.html`

<details>
<summary>화면에서 규칙 차이를 확인하는 방법</summary>

가로 3칸을 한 줄 띄워 두 줄 배치하고 Tick을 두 번 진행합니다. Conway에서는 전멸하고, HighLife에서는 3×3 배치가 됩니다.

</details>

## 프로젝트 구조

```text
holub-life/
├── src/main/java/com/holub/
│   ├── life/             게임·규칙·예측·상태 표시
│   └── ui, io, tools,
│       asynch/           원본 보조 코드
├── src/test/java/com/holub/
│                         JUnit 테스트
└── docs/
    ├── decisions/       설계 결정 기록 (ADR)
    ├── experiments/     실험 코드와 실행 방법
    ├── features/        기능별 설계·구현 기록
    └── findings.md      코드 검토와 발견 사항
```

## 개발 방식

- 브랜치에서 작업하고 Pull Request를 통해 머지합니다.
- 새 기능에는 JUnit 테스트를 함께 작성합니다.
- 커밋과 설계 결정 기록에 변경 내용과 이유를 남깁니다.

원본과의 비교 기준은 첫 커밋 `648d9bd`입니다.
