package com.holub.life;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EndingPredictor가 시작 배치와 규칙만 보고 결말(소멸 / 정지 / 반복 / 미확정)을 맞히는지 확인한다.
 * "몇 세대 뒤"의 기대값 중 손으로 따지기 어려운 것(가로 5칸, HighLife, Glider)은
 * 같은 배치를 실제 Neighborhood 판에서 돌려 얻은 값이다.
 */
class EndingPredictorTest {

    private static final int WIDTH = 64;
    private final EndingPredictor predictor = new EndingPredictor(500);   // 500세대까지 미리 돌려 본다

    // ── 소멸 ──

    @Test
    void 빈_판은_이미_소멸이다() {
        Ending ending = predictor.predict(generation(), new ConwayRule());

        assertEquals(Ending.Kind.EXTINCT, ending.kind());
        assertEquals(0, ending.after());
    }

    @Test
    void 한_칸은_1세대_뒤_소멸한다() {
        Ending ending = predictor.predict(generation(10, 10), new ConwayRule());

        assertEquals(Ending.Kind.EXTINCT, ending.kind());
        assertEquals(1, ending.after());
    }

    // ── 정지 ──

    @Test
    void Block은_처음부터_정지다() {
        Ending ending = predictor.predict(generation(10, 10,  11, 10,
                                                     10, 11,  11, 11), new ConwayRule());

        assertEquals(Ending.Kind.STILL, ending.kind());
        assertEquals(0, ending.after());
    }

    @Test
    void ㄱ자_3칸은_1세대_뒤_Block이_되어_정지한다() {
        // 빈 자리 (11,11)의 이웃이 3개라 태어나고, 있던 세 칸은 이웃이 2개씩이라 살아남는다 → Block
        Ending ending = predictor.predict(generation(10, 10,  11, 10,
                                                     10, 11), new ConwayRule());

        assertEquals(Ending.Kind.STILL, ending.kind());
        assertEquals(1, ending.after());
    }

    @Test
    void Glider는_247세대_뒤_판_끝에서_정지한다() {
        // 왼쪽 위에서 오른쪽 아래로 가는 Glider. 판 끝에 닿아 4칸으로 멈춘다.
        Ending ending = predictor.predict(generation(1, 0,  2, 1,  0, 2,  1, 2,  2, 2), new ConwayRule());

        assertEquals(Ending.Kind.STILL, ending.kind());
        assertEquals(247, ending.after());
    }

    // ── 반복 ──

    @Test
    void Blinker는_처음부터_주기_2로_반복한다() {
        Ending ending = predictor.predict(generation(10, 10,  11, 10,  12, 10), new ConwayRule());

        assertEquals(Ending.Kind.REPEATING, ending.kind());
        assertEquals(0, ending.after());
        assertEquals(2, ending.period());
    }

    @Test
    void 가로_5칸은_6세대_뒤부터_주기_2로_반복한다() {
        Ending ending = predictor.predict(generation(10, 10,  11, 10,  12, 10,  13, 10,  14, 10), new ConwayRule());

        assertEquals(Ending.Kind.REPEATING, ending.kind());
        assertEquals(6, ending.after());
        assertEquals(2, ending.period());
    }

    // ── 규칙에 따라 ──

    @Test
    void 같은_배치라도_규칙에_따라_결말이_다르다() {
        // 가로 3칸을 한 줄 띄워 두 줄. Conway에서는 2세대에 전멸한다 (NeighborhoodRuleTest가 실제 판에서 확인한 것).
        Generation start = generation(10, 10,  11, 10,  12, 10,
                                      10, 12,  11, 12,  12, 12);

        Ending byConway   = predictor.predict(start, new ConwayRule());
        Ending byHighLife = predictor.predict(start, new HighLifeRule());

        assertEquals(Ending.Kind.EXTINCT, byConway.kind());
        assertEquals(2, byConway.after());

        assertEquals(Ending.Kind.EXTINCT, byHighLife.kind());
        assertEquals(11, byHighLife.after());
    }

    // ── 미확정 ──

    @Test
    void 한도_안에_결말이_없으면_미확정이다() {
        // Glider는 247세대 뒤에야 멈춘다. 100세대까지만 돌려 보면 알 수 없다.
        Ending ending = new EndingPredictor(100).predict(generation(1, 0,  2, 1,  0, 2,  1, 2,  2, 2), new ConwayRule());

        assertEquals(Ending.Kind.UNDECIDED, ending.kind());
        assertEquals(100, ending.after());
    }

    @Test
    void 정지는_그다음_세대까지_돌려_봐야_알_수_있다() {
        // Glider는 247세대에 멈춘 모양이 된다. 멈췄다는 것은 248세대가 247세대와 같다는 것을 봐야 안다.
        Generation glider = generation(1, 0,  2, 1,  0, 2,  1, 2,  2, 2);

        assertEquals(Ending.Kind.UNDECIDED, new EndingPredictor(247).predict(glider, new ConwayRule()).kind());
        assertEquals(Ending.Kind.STILL,     new EndingPredictor(248).predict(glider, new ConwayRule()).kind());
    }

    /** (x, y)를 차례로 적어 Generation을 만든다. generation(10, 10, 11, 10)은 (10,10)과 (11,10)이 살아 있는 판. */
    private static Generation generation(int... xy) {
        Set<Point> cells = new HashSet<>();
        for (int i = 0; i < xy.length; i += 2) {
            cells.add(new Point(xy[i], xy[i + 1]));
        }
        return new Generation(cells, WIDTH);
    }
}
