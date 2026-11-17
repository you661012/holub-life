# 원본 코드에서 발견한 것

원본(Holub Life v1.03, 첫 커밋 `648d9bd`)을 읽거나 돌리다가 이상하게 동작하는 것을 보면 여기에 적는다.

- **확인한 것과 추정한 것을 구분해서 쓴다.**
- "어떻게 확인했나"에는 JUnit 테스트 이름, 화면에서 본 것, 저장소 밖 실험 중 무엇인지 적는다.
- 고치지 않기로 했으면 그 이유도 적는다.
- 원본에 이미 적용된 패턴을 조사한 것도 이 파일에 둔다 (아래 "원본에 이미 적용된 패턴").

## 목록

| # | 한 줄 요약 | 확인 방법 | 처리 |
|---|---|---|---|
| 1 | `Resident.transition()`이 `Cell`의 주석과 반대 값을 돌려준다 | JUnit (`ResidentTransitionTest` 4개, `NeighborhoodRuleTest` 1개) | 고쳤다 (11/17) |

### 1. `Resident.transition()`이 `Cell`의 주석과 반대 값을 돌려준다

- 무엇이 이상한가: `Cell.transition()`의 주석은 "상태가 바뀌었으면 true"라고 적고 있다. 원본 `Resident.transition()`은 `isStable()`을 그대로 돌려준다. `isStable()`은 이번 세대에 안 바뀔 때 true이므로, 바뀌면 false가 나온다. `Neighborhood.transition()`은 안의 칸 중 하나라도 true면 true를 돌려주고, `Universe`의 `tick()`은 판의 `transition()`이 true일 때만 `refreshNow()`를 부른다.
- 평소에 드러나지 않는 이유: 판에 안 바뀌는 칸이 하나라도 있으면 true가 나온다. 모든 칸이 한꺼번에 바뀌는 세대에만 false가 된다.
- 재현 방법: 규칙을 `B0/S`로 하고 빈 판에서 세대를 넘긴다. `B0/S`는 이웃이 0개인 빈 칸에서도 태어나고 살아 있는 칸은 모두 죽는 규칙이라, 판이 0칸 → 4096칸 → 0칸으로 세대마다 전부 바뀐다. 이때 판의 `transition()`이 매번 false였다.
- 어떻게 확인했나: JUnit. 고치기 전에 새 테스트 5개가 실패했고 고친 뒤 통과한다. `ResidentTransitionTest`는 세포 하나가 태어날 때, 죽을 때 true이고 그대로일 때 false인지 본다. `NeighborhoodRuleTest`의 `모든_칸이_한꺼번에_바뀌는_세대에도_판의_transition은_true를_돌려준다`는 위 재현 방법 그대로다. 기존 테스트는 `transition()` 뒤의 세포 상태만 보고 돌려주는 값은 보지 않아서 이 문제를 잡지 못했다. 화면 쪽은 두 가지로 확인했다 (11/17). 첫째, 고치기 전과 후의 코드를 각각 사본으로 꺼내 창을 띄우지 않은 채 `Universe`와 `Clock`을 만들고 세대를 네 번 넘기며 세어 봤다. 고치기 전에는 `refreshNow()`가 한 번도 실행되지 않았고 그려진 칸이 0개였다. 고친 뒤에는 세대마다 한 번씩 실행됐고 4096칸이 살아 있는 칸, 죽은 칸으로 번갈아 그려졌다. 둘째, 고친 프로그램을 실제로 실행해 Rule > Custom에 `B0/S`를 넣고 Go > Agonizing으로 돌렸을 때 판 전체가 세대마다 찼다 비었다 하는 것을 눈으로 봤다. 고치기 전의 프로그램을 실제 화면에서 돌려 본 적은 없다. 세어 본 방법은 `docs/experiments/README.md`의 "화면 쪽 확인"에 있다.
- 원인 (확인): `Resident.transition()`의 `boolean changed = isStable();`
- 처리: 고쳤다. `boolean changed = !isStable();` 한 곳이다. 다른 길도 있었다. `Universe`의 `tick()`이 돌려주는 값을 보지 않고 항상 그리게 하는 것, 그리고 고치지 않고 지원하지 않는 경우로 적어 두는 것이다. `Resident`를 고친 이유는 `Cell` 인터페이스가 약속한 값을 `Resident`가 지키도록 원인에서 바로잡을 수 있고, 그 약속을 JUnit으로 확인할 수 있어서다. `tick()`을 고치는 쪽은 `transition()`의 뜻이 틀린 채로 남는다. 이 값을 쓰는 곳은 `Neighborhood.transition()`(가장자리 표시 `activeEdges`)과 `Universe`의 `tick()` 둘이다. `activeEdges`는 ADR 002에서 건너뛰는 조건을 없앤 뒤로 다음 세대를 계산할지 정하는 데 쓰이지 않는다 (코드를 읽은 것이다. Load 직후의 동작은 확인하지 않았다).
- 확장 기능에 주는 영향: 규칙 직접 입력(확장 1)으로 `B0`가 든 규칙을 넣으면 세대가 넘어가도 화면을 다시 그리라는 호출이 빠질 수 있었다. 결말 예측(확장 2)의 계산은 이 값을 쓰지 않는다.

---

## 원본에 이미 적용된 패턴 (11/17 조사)

확장 2를 시작하기 전에 원본 소스 14개를 읽고 강의 자료와 맞춰 봤다.
줄 번호는 모두 원본(`648d9bd`) 기준이다. `git show 648d9bd:src/main/java/com/holub/life/Universe.java`처럼 열면 그 줄이 나온다.

### 조사한 방법과 한계

- 강의 PDF에서 글자를 뽑아 쪽 번호와 맞췄다. 슬라이드의 그림은 읽지 못했다 (예: `13 Composite` 6쪽은 그림뿐이다).
- 원본 소스는 첫 커밋에서 읽었다. `MenuSite`(1227줄)는 구조와 주요 부분만 읽었다.
- Holub의 책은 보지 않았다. 근거는 소스의 주석과 코드뿐이다.

### 강의 자료가 다루는 패턴

- 전용 강의가 있는 것: Strategy(2), Observer(3), Command(4), Decorator(5), Factory Method·Abstract Factory(6), Adapter(7), Facade(8),
  Singleton(9), State(10), Template Method(11), Iterator(12), Composite(13), Builder(14), Mediator(15), Proxy(16), Visitor(17),
  CoR(18), Bridge(19), Prototype(20)
- 여러 패턴을 묶은 강의: 21 Pattern Hatching, 22 HolubSQL, 23 MVC, 24 Lexi Case Study
- 이름만 나오는 것: Flyweight, Memento, Interpreter (`0 Introduction` 13쪽 분류표. Flyweight는 21번 15쪽, Memento는 12번 27쪽에 한 줄씩 더 있다)
- GoF 밖이지만 수업에 있는 것: Active Object (`4 Command` 21쪽)

### Holub이 주석에 이름을 적은 패턴

| 패턴 | 수업 | 누가 어느 역할인가 | 근거 (원본 줄) |
|---|---|---|---|
| Composite | 13 | Component `Cell` / Composite `Neighborhood` / Leaf `Resident` / Client `Universe` | `Universe` 23 |
| Singleton | 9 | `Universe`, `Clock`, `MenuSite`, `Cell.DUMMY` | `Universe` 20·31, `Clock` 47, `MenuSite` 20·168, `Cell` 129 |
| Observer | 3 | Subject `Clock` (구독자 목록은 `Publisher`가 보관) / Observer `Clock.Listener` / ConcreteObserver `Universe` 안의 익명 클래스 | `Clock` 14·133, `Universe` 142 |
| Mediator | 15 | Mediator `Universe`. Swing 이벤트를 받아 가장 바깥 `Neighborhood`에 요청으로 넘긴다 | `Universe` 19 |
| Facade | 8 | Facade `MenuSite` / 하위 시스템은 Swing 메뉴 클래스들 | `Clock` 164 |
| Prototype | 20 | Prototype `Cell.create()` / ConcretePrototype `Resident`·`Neighborhood` / Client `Neighborhood` 생성자 | `Neighborhood` 61·72·75, `Cell` 74 |
| Visitor | 17 | Visitor `Publisher.Distributor` / Element `Publisher.Node`의 `accept` / ObjectStructure `Publisher` | `Publisher` 67·101 |
| Strategy | 2 | 위와 같은 `Distributor`를 클래스 주석에서는 Strategy라고 부른다 | `Publisher` 8 |
| Memento | 이름만 | Memento `Cell.Memento`·`NeighborhoodState` / 만드는 쪽 `Neighborhood` / 보관하는 쪽 `Universe`의 `doStore()`·`doLoad()` | `Cell` 96, `Neighborhood` 468·481, `Universe` 166·188 |
| Flyweight | 이름만 | 이웃 8칸과 화면 크기를 `Resident`가 필드로 갖지 않고 매개변수로 받는다 | `Neighborhood` 107, `Universe` 42 |
| Active Object | 4 (21쪽) | `refreshNow()`가 `Runnable`을 Swing 이벤트 큐에 넣는다 | `Universe` 228 |

수업과 모양이 다른 곳 (코드에서 확인한 사실):

- Singleton 넷의 구현이 서로 다르다. `Universe`는 `static final` 필드에 미리 만들어 둔다 (9번 38쪽의 Option 2, eager instantiation).
  `Clock`은 처음 부를 때 만들고 `instance()`가 `synchronized`다 (Option 1). `MenuSite`는 `getInstance()` 없이 메서드가 전부 `static`이다.
  `Cell.DUMMY`는 익명 클래스의 인스턴스 하나를 상수로 둔 것이다.
- Visitor의 Element가 `Node` 한 종류뿐이다. 수업 9쪽의 구조는 ConcreteElement마다 `visit` 연산이 따로 있다.
- Mediator에서 `Cell`은 `Universe`를 모른다. 요청이 `Universe`에서 `Cell` 쪽으로만 간다.
- `MouseAdapter`·`ComponentAdapter`·`WindowAdapter`는 수업 7번의 Adapter 패턴이 아니다. 빈 메서드를 미리 채워 둔 Swing 클래스다.

### 주석에는 없지만 구조가 맞아 보이는 것 (해석)

Holub이 이름을 적지 않았다. 코드를 읽고 맞춘 것이라 틀릴 수 있다.

- Command (4): `MenuSite.addLine`에 넘기는 `ActionListener` (`Universe` 106~140, `Clock` 88~110). 요청을 객체에 담아 메뉴 항목에 맡긴다.
- Template Method의 hook (11번 15쪽): `Universe.paint()` 재정의 (`Universe` 214). 슬라이드는 `JFrame`의 `paint()`를 hook의 예로 든다 (`Universe`는 `JPanel`이다).
- Null Object: `Cell.DUMMY` (`Cell` 137). 아무 일도 하지 않는 `Cell`이다. GoF 패턴은 아니고 수업 13번 15쪽의 `NullIterator`와 같은 생각이다.
- Iterator (12): `java.util.Iterator`를 쓰기만 한다. 직접 구현한 것은 없다.

### 수업에 있지만 원본에 없는 것

Decorator, Factory Method, Abstract Factory, Builder, Proxy, CoR, Bridge, State.
MVC(23)도 적용되어 있지 않다. `Cell`이 `redraw()`로 스스로 그리므로 Model과 View가 한 클래스에 있다.

### 확장 2와 닿는 사실

- `create()`는 상태를 복사하지 않는다. 원본 `Resident.create()`는 `new Resident()`를 돌려준다 (`Resident` 107).
  Prototype으로 "지금 판의 사본"을 얻을 수는 없다.
- 판의 상태를 꺼내는 기존 통로는 Memento다. `transfer(…, Cell.STORE)`로 읽는다. 테스트의 `LiveCellCollector`가 이 길을 쓴다.
- 세대가 넘어가는 순간을 아는 기존 통로는 Observer다. `Clock.addClockListener`로 구독한다.
- `Neighborhood`의 `static` 필드 둘(`readingPermitted` 43, `nestingLevel` 293)은 판을 하나 더 만들면 두 판이 함께 쓴다.

---

## 강의의 패턴을 이 프로젝트에 쓸 수 있는가 (11/17 검토)

검토 메모다. "후보"로 적은 것은 아직 정하지 않았고 돌려 보지도 않았다.
`22 HolubSQL` 강의가 한 프로젝트 안에서 패턴 여섯 개가 각각 어떤 문제를 푸는지 보여 주는 것을 보고, 같은 눈으로 이 프로젝트를 다시 봤다.

### 이미 쓰고 있는 것

| 강의 | 이 프로젝트의 코드 | 문서에 적혀 있나 |
|---|---|---|
| Strategy (`2 Strategy` 32쪽, `22 HolubSQL` 39~48쪽) | `Rule`과 구현체 셋. `Rule`을 부르는 쪽은 `Resident`와 `Generation` 둘이다 | README, ADR 001 |
| Prototype (`20 Prototype`) | `Resident.create()`가 같은 `RuleHolder`를 넘긴다. 그래서 4096칸이 `RuleHolder` 하나를 함께 본다 | ADR 001 |
| Singleton 강의의 `volatile` (`9 Singleton` 24~37쪽) | `RuleHolder.current`가 `volatile`이다. 세대 진행과 메뉴가 서로 다른 스레드에서 돈다 | ADR 001 |
| Facade (`8 Facade`) | Rule 메뉴를 원본 `MenuSite.addLine`으로 추가했다 | 없음 |
| OCP (`SOLID Part 1` 19~28쪽) | 확장 2는 지금까지(11/17) 기존 클래스를 고치지 않고 `Generation`, `EndingPredictor`, `Ending`을 추가했다. 화면에 연결하면 `Universe`나 `Life`는 고치게 된다 | 없음 |
| SRP (`SOLID Part 1` 8~17쪽) | 다음 세대 계산은 `Generation`, 결말 판정은 `EndingPredictor`, 판정 결과는 `Ending`이 맡는다 | 없음 |
| DIP (`6 Factory` 22쪽) | `Generation.next(Rule)`은 `Rule` 인터페이스만 안다. 어느 구현체인지 모른다 | 없음 |
| MVC의 기준 (`23 MVC` 16쪽: Model은 JUnit으로 쉽게 시험된다) | `GenerationTest` 9개, `GenerationMatchesBoardTest` 5개, `EndingPredictorTest` 12개가 화면 없이 돈다 (11/17 기준) | 없음 |

과제 지시문은 "설계 패턴/설계 원칙의 적용 이유"를 쓰라고 하므로 원칙도 근거가 된다.
지시문의 "모델을 UI와 분리하여 독립적으로 테스팅"은 표의 마지막 줄에 해당한다.
원본 전체가 MVC로 나뉜 것은 아니다. `Cell`이 `redraw()`로 스스로 그린다.

### HolubSQL의 Strategy와 다른 점

`22 HolubSQL` 41쪽에서 `ConcreteTable.select(Selector where)`는 Strategy를 메서드의 매개변수로 받는다.
이 프로젝트는 `RuleHolder`를 `Resident`의 생성자로 넘긴다. 매개변수로 넘기는 방식은 ADR 001의 대안 C였고,
규칙을 `Cell` 인터페이스에 드러내지 않기로 해서(ADR 001 결정 1) 고르지 않았다.
HolubSQL은 조회할 때마다 조건이 다르고 이 프로젝트는 판 전체가 규칙 하나를 계속 쓴다는 차이도 있다 (이 비교는 해석이다).

### 화면 연결에서 쓸 수 있는 후보 (아직 정하지 않았다)

(덧붙임: 아래 첫 번째인 규칙 변경 알림은 같은 날 `Universe`의 메서드 하나에 모으는 쪽으로 정했다. Observer는 고르지 않았다. ADR 004에 있다.)

- Observer (`3 Observer`, `15 Mediator` 13~15쪽): 규칙이 바뀌면 예측을 다시 해야 한다. 지금 `RuleHolder.change()`는 아무에게도 알리지 않는다.
  길은 둘이다. `RuleHolder`가 바뀐 것을 알리게 한다(원본 `Publisher`를 쓸 수 있다). 또는 `Universe`가 규칙을 바꾼 뒤 예측도 직접 부른다(원본에서 `Universe`가 하는 Mediator 방식).
  `15 Mediator` 15쪽이 이 둘을 비교한다. 세대가 넘어가는 순간은 원본의 Observer(`Clock.addClockListener`)로 알 수 있다.
- Active Object (`4 Command` 21쪽): 예측 계산 때문에 화면이 굼뜨면, 계산은 다른 곳에서 하고 결과만 Swing 이벤트 큐에 넣는다. 원본 `refreshNow()`가 쓰는 방식이다.
  실제로 굼뜬지는 화면에 붙여 봐야 안다.
- Iterator (`13 Composite` 12~16쪽, `22 HolubSQL` 22~27쪽): 판을 읽는 다른 길이다. `Cell`에 메서드를 추가해야 해서 ADR 001 결정 1과 어긋나고,
  원본의 Memento 통로(`transfer(…, Cell.STORE)`)가 이미 있다.

### 쓰지 않는 것

Command(실행 취소), Factory(규칙 객체 생성), State, Template Method, CoR, Builder, Bridge, Proxy, Decorator.
이 프로젝트에 이 패턴들이 풀어 줄 문제가 지금은 없다. Command와 Factory는 앞서 범위 밖으로 정했다.
`21 Pattern Hatching` 53쪽 "Pattern Pitfalls"도 패턴에는 비용(간접 호출, 복잡도)이 있으니 필요한 만큼만 유연하게 설계하라고 한다.

---

## 적는 형식 (복사해서 쓴다)

### N. 제목

- 무엇이 이상한가:
- 재현 방법:
- 어떻게 확인했나:
- 원인 (확인 / 추정):
- 처리 (고쳤다면 커밋·PR, 고치지 않았다면 이유):
- 확장 기능에 주는 영향:
