package com.holub.life;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 창 아래 두 줄의 글자. 옛 예측을 버리는지(변경 번호), 진행한 세대만큼 남은 세대를 줄이는지 확인한다.
 * StatusLine은 Swing을 쓰지 않으므로 화면 없이 확인할 수 있다.
 */
class StatusLineTest {

    @Test
    void 윗줄은_세대_세포_규칙_아랫줄은_예측한_결말() {
        StatusLine line = new StatusLine();
        line.update(12, 34, "B3/S23", 1);
        line.accept(request(12, 1), new Ending(Ending.Kind.STILL, 5, 1));

        assertEquals("Generation 12 | Cells 34 | Rule B3/S23", line.stateText());
        assertEquals("Ending: still in 5 generations", line.endingText());
    }

    @Test
    void 결과가_오기_전에는_계산_중이다() {
        StatusLine line = new StatusLine();
        line.update(0, 3, "B3/S23", 1);

        assertEquals("Generation 0 | Cells 3 | Rule B3/S23", line.stateText());
        assertEquals("Ending: calculating...", line.endingText());
    }

    @Test
    void 변경_번호가_다른_결과는_버린다() {
        StatusLine line = new StatusLine();
        line.update(0, 3, "B3/S23", 2);                                     // 그사이 한 번 더 클릭했다
        line.accept(request(0, 1), new Ending(Ending.Kind.EXTINCT, 1, 0));  // 클릭 전의 판으로 한 예측

        assertEquals("Ending: calculating...", line.endingText());
    }

    @Test
    void 새로_바뀌면_옛_결말_대신_계산_중으로_보인다() {
        StatusLine line = new StatusLine();
        line.update(0, 3, "B3/S23", 1);
        line.accept(request(0, 1), new Ending(Ending.Kind.EXTINCT, 1, 0));
        line.update(0, 4, "B3/S23", 2);                                     // 클릭

        assertEquals("Ending: calculating...", line.endingText());
    }

    @Test
    void 세대만_진행했으면_남은_세대를_줄인다() {
        StatusLine line = new StatusLine();
        line.update(13, 3, "B3/S23", 1);                                    // 결과가 13세대에 도착
        line.accept(request(10, 1), new Ending(Ending.Kind.EXTINCT, 5, 0)); // 10세대에서 "5세대 뒤 소멸"

        assertEquals("Ending: extinct in 2 generations", line.endingText());
    }

    @Test
    void 결과를_받은_뒤에_세대가_진행해도_남은_세대를_줄인다() {
        StatusLine line = new StatusLine();
        line.update(10, 3, "B3/S23", 1);
        line.accept(request(10, 1), new Ending(Ending.Kind.EXTINCT, 5, 0));
        line.update(14, 1, "B3/S23", 1);

        assertEquals("Ending: extinct in 1 generation", line.endingText());
    }

    @Test
    void 그_세대를_지났으면_도달한_상태로_보인다() {
        StatusLine line = new StatusLine();
        line.update(20, 0, "B3/S23", 1);
        line.accept(request(10, 1), new Ending(Ending.Kind.EXTINCT, 5, 0));

        assertEquals("Ending: extinct", line.endingText());
    }

    @Test
    void 정지와_반복의_글자() {
        StatusLine line = new StatusLine();
        line.update(0, 6, "B3/S23", 1);

        line.accept(request(0, 1), new Ending(Ending.Kind.STILL, 0, 1));
        assertEquals("Ending: still", line.endingText());

        line.accept(request(0, 1), new Ending(Ending.Kind.REPEATING, 6, 2));
        assertEquals("Ending: repeats (period 2) in 6 generations", line.endingText());

        line.accept(request(0, 1), new Ending(Ending.Kind.REPEATING, 0, 2));
        assertEquals("Ending: repeating (period 2)", line.endingText());
    }

    @Test
    void 미확정은_한도를_보여_주고_세대가_지나도_줄이지_않는다() {
        StatusLine line = new StatusLine();
        line.update(0, 1230, "B3/S23", 1);
        line.accept(request(0, 1), new Ending(Ending.Kind.UNDECIDED, 500, 0));
        line.update(30, 900, "B3/S23", 1);

        assertEquals("Ending: undecided within 500 generations", line.endingText());
    }

    @Test
    void 늦게_도착한_옛_세대_정보는_무시한다() {
        // 세대 진행(시계 스레드)이 읽은 정보가 클릭(Swing 스레드)보다 늦게 화면에 반영되는 경우
        StatusLine line = new StatusLine();
        line.update(5, 4, "B3/S23", 2);                                     // 클릭: 변경 번호 2
        line.update(5, 3, "B3/S23", 1);                                     // 클릭 전에 읽은 정보가 늦게 왔다
        line.accept(request(5, 2), new Ending(Ending.Kind.STILL, 0, 1));

        assertEquals("Generation 5 | Cells 4 | Rule B3/S23", line.stateText());
        assertEquals("Ending: still", line.endingText());
    }

    // ── 아랫줄의 글자색 ──

    @Test
    void 결말의_종류마다_글자색이_다르다() {
        StatusLine line = new StatusLine();
        line.update(0, 6, "B3/S23", 1);

        line.accept(request(0, 1), new Ending(Ending.Kind.EXTINCT, 3, 0));
        assertEquals(StatusLine.EXTINCT_COLOR, line.endingColor());

        line.accept(request(0, 1), new Ending(Ending.Kind.STILL, 3, 1));
        assertEquals(StatusLine.STILL_COLOR, line.endingColor());

        line.accept(request(0, 1), new Ending(Ending.Kind.REPEATING, 3, 2));
        assertEquals(StatusLine.REPEATING_COLOR, line.endingColor());

        line.accept(request(0, 1), new Ending(Ending.Kind.UNDECIDED, 500, 0));
        assertEquals(StatusLine.UNKNOWN_COLOR, line.endingColor());
    }

    @Test
    void 계산_중일_때의_글자색은_미확정과_같다() {
        StatusLine line = new StatusLine();
        line.update(0, 3, "B3/S23", 1);

        assertEquals(StatusLine.UNKNOWN_COLOR, line.endingColor());
    }

    @Test
    void 네_가지_색은_서로_다르다() {
        assertEquals(4, java.util.Set.of(StatusLine.EXTINCT_COLOR, StatusLine.STILL_COLOR,
                                          StatusLine.REPEATING_COLOR, StatusLine.UNKNOWN_COLOR).size());
    }

    private static PredictionRequest request(long generation, long change) {
        return new PredictionRequest(new Generation(Set.of(), 64), new ConwayRule(), generation, change);
    }
}
