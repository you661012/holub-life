# 실험 코드

설계 결정의 근거로 쓴 숫자와 결과를 다시 확인할 수 있게 남긴 코드다.
JUnit 테스트가 아니고 Gradle이 빌드하지도 않는다 (`src/` 밖에 있다). 2026-11-17에 쓴 것이다.

## 돌리는 방법

`holub-life` 폴더에서 실행한다. 컴파일한 것은 저장소 밖(`/tmp/life-exp`)에 둔다.

```bash
./gradlew classes
```

```bash
javac -encoding UTF-8 -cp build/classes/java/main -d /tmp/life-exp docs/experiments/*.java
```

```bash
java -cp build/classes/java/main:/tmp/life-exp Exp
```

마지막 줄의 `Exp` 자리에 아래 표의 클래스 이름을 넣는다.

## 파일

| 클래스 | 무엇을 보는가 | 이 결과를 쓴 문서 |
|---|---|---|
| `Exp` | 판을 하나 더 만드는 방법(가)과 좌표 집합으로 계산하는 방법(나)을 같은 배치에 돌린다. 결말 비교, 300세대 위치 비교, 걸린 시간, 판 둘을 두 스레드에서 돌렸을 때 `nestingLevel` | ADR 003 3절 |
| `Exp2` | 두 스레드로 돌린 뒤 `nestingLevel`과 `readingPermitted`가 어떤 값으로 남는가 | ADR 003 3절 |
| `Exp3` | `Generation`만 500세대 돌리는 시간. 처음 잰 것이고 `Exp`의 시간과 조건이 달랐다 | ADR 003 6절 (바로잡기 전의 숫자) |
| `Fair` | 같은 조건으로 다시 잰 시간. 같은 배치에서 500세대를 진행만 시킨다 | ADR 003 "포기한 것", 6절 |
| `Exp4` | 예측기 테스트의 기대값을 실제 `Neighborhood` 판에서 뽑는다 (가로 5칸, HighLife, Glider 등) | `EndingPredictorTest`, 확장 2 문서 4-1절 |
| `B0` | 빈 판을 `B0/S`로 돌릴 때 실제 판의 칸 수와 예측기의 답 | 확장 2 문서 4-1절 |
| `Refresh` | 빈 판을 `B0/S`로 돌릴 때 판의 `transition()`이 돌려주는 값 | `docs/findings.md` 1번 |
| `RuleChangeDemo` | 규칙이 바뀐 것을 화면 한 줄에 전하는 세 방법 (자리마다 적기, Observer, 메서드 하나에 모으기) | ADR 004 3절 |
| `StaticFieldDemo` | 그냥 필드와 `static` 필드를 두 스레드에서 10만 번씩 올렸을 때의 차이 | ADR 003 4절 |

`screen-check/Screen.java`는 위 명령으로 컴파일되지 않는다. 아래 "화면 쪽 확인"에 따로 적었다.

## 조건과 한계

- 잰 환경: Apple M3, 터미널의 `java`는 OpenJDK 26.0.1 (프로젝트 빌드는 Temurin 21).
- 시간은 실행할 때마다 달라진다. `Fair`는 몇 번 미리 돌린 뒤 다섯 번을 재서 가장 빠른 값을 찍는다. 평소에 걸리는 시간은 이보다 길 수 있다.
- `Exp`의 나(좌표 집합)는 실험용으로 따로 짠 코드다. 저장소의 `Generation`과 다른 코드다.
- `Exp`의 `predict()`는 빈 판을 바로 소멸로 본다. `B0`가 든 규칙에서는 틀린다. 저장소의 `EndingPredictor`는 이것을 고쳤다. `Exp`와 `Exp4`는 Conway, HighLife, `B2/S`로만 돌렸으므로 결과에는 영향이 없다.
- 스레드 실험(`Exp`의 4번, `Exp2`, `StaticFieldDemo`)은 실행마다 결과가 달라진다. 정상인 값이 나오는 실행이 있어도 경쟁 문제가 없다는 뜻은 아니다. 문서에 적은 실행에서는 `nestingLevel`이 20번 중 20번, `readingPermitted`가 20번 중 12번 틀어졌다.
- `B0`와 `Refresh`는 지금 코드(고친 뒤)로 돌리면 문서에 적은 "고치기 전"의 결과가 나오지 않는다. 고치기 전의 결과를 보려면 해당 커밋 앞의 코드로 돌려야 한다 (`B0`는 `e42f9eb` 앞, `Refresh`는 `8efe048` 앞).
- ADR 002의 측정(2026-11-16, `Rule.nextState()`가 불린 횟수와 세대당 시간)에 쓴 코드는 남아 있지 않다.

## 화면 쪽 확인 (`screen-check/Screen.java`)

`Resident.transition()`을 고치기 전과 후에 화면을 다시 그리라는 호출이 실제로 가는지 세어 본 것이다 (`docs/findings.md` 1번).
창을 화면에 띄우지 않고 `Life.java`가 하는 것처럼 창과 `Universe`를 만든 뒤 `Clock.instance().tick()`을 네 번 부른다.

이 파일은 저장소의 코드로는 컴파일되지 않는다. 세는 변수를 넣은 사본이 있어야 한다. 사본은 이렇게 만들었다.

1. 보고 싶은 커밋을 저장소 밖 폴더에 꺼낸다 (`git archive 커밋 | tar -x -C 폴더`).
2. 사본의 `RuleHolder`에서 기본 규칙을 `new BirthSurvivalRule("B0/S")`로 바꾼다.
3. 사본의 `Universe`에 `public static volatile int refreshRuns`를 추가하고, `refreshNow()` 안의 `run()` 첫 줄에서 1을 더한다.
4. 사본의 `Resident`에 `public static volatile int drawnAlive, drawnDead`를 추가하고, `redraw()` 첫 줄에서 살아 있으면 `drawnAlive`, 아니면 `drawnDead`에 1을 더한다.
5. 사본에서 `./gradlew classes`를 한 뒤, 그 `build/classes/java/main`을 classpath로 `Screen.java`를 컴파일하고 `java -Dapple.awt.UIElement=true`로 실행한다.

결과: 고치기 전(`25082e4`)은 세대마다 `refreshNow` 0번, 그려진 칸 0개. 고친 뒤(`8efe048`)는 세대마다 1번, 4096칸이 살아 있는 칸과 죽은 칸으로 번갈아 그려졌다.
