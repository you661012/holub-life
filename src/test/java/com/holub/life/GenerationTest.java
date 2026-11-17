package com.holub.life;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Generation(한 세대에 살아 있는 칸들의 좌표)이 Rule을 받아 다음 세대를 맞게 만드는지 확인한다.
 * 화면도 Neighborhood 판도 쓰지 않는다. 좌표와 규칙만으로 계산한다.
 */
class GenerationTest {

    private static final int WIDTH = 64;   // 화면의 판과 같은 크기 (64×64)

    @Test
    void 한_칸만_있으면_다음_세대에_사라진다() {
        Generation now = new Generation(cells(10, 10), WIDTH);

        Generation next = now.next(new ConwayRule());

        assertEquals(Set.of(), next.liveCells());          // 이웃이 0개라 죽는다
    }

    @Test
    void Block은_다음_세대에도_그대로다() {
        Generation block = new Generation(cells(10, 10,  11, 10,
                                                10, 11,  11, 11), WIDTH);

        Generation next = block.next(new ConwayRule());

        assertEquals(block, next);                          // 네 칸 모두 이웃이 3개라 살아남고, 새로 태어나는 칸은 없다
    }

    @Test
    void Blinker는_한_세대_뒤_세로가_되고_두_세대_뒤_제자리로_온다() {
        Generation horizontal = new Generation(cells(10, 10,  11, 10,  12, 10), WIDTH);   // 가로 3칸
        Generation vertical   = new Generation(cells(11, 9,   11, 10,  11, 11), WIDTH);   // 세로 3칸

        Generation first  = horizontal.next(new ConwayRule());
        Generation second = first.next(new ConwayRule());

        assertEquals(vertical, first);
        assertEquals(horizontal, second);
    }

    @Test
    void 같은_배치라도_규칙이_다르면_다음_세대가_다르다() {
        // 가로 3칸을 한 줄 띄워 두 줄. NeighborhoodRuleTest가 실제 판에서 확인한 것과 같은 배치, 같은 기대값이다.
        Generation start = new Generation(cells(10, 10,  11, 10,  12, 10,
                                                10, 12,  11, 12,  12, 12), WIDTH);

        Generation byConway   = start.next(new ConwayRule());
        Generation byHighLife = start.next(new HighLifeRule());

        // 가운데 (11,11)은 이웃이 6개다. Conway에서는 비어 있고 HighLife에서는 태어난다.
        assertEquals(cells(11, 9,  11, 10,          11, 12,  11, 13), byConway.liveCells());
        assertEquals(cells(11, 9,  11, 10,  11, 11,  11, 12,  11, 13), byHighLife.liveCells());
    }

    @Test
    void 판_끝에서는_판_밖을_죽은_칸으로_본다() {
        // 맨 윗줄(y=0)에 가로 3칸. 판 안이었다면 세로 3칸 (2,-1),(2,0),(2,1)이 되지만 (2,-1)은 판 밖이다.
        Generation onTopEdge = new Generation(cells(1, 0,  2, 0,  3, 0), WIDTH);

        Generation next = onTopEdge.next(new ConwayRule());

        assertEquals(cells(2, 0,  2, 1), next.liveCells());
    }

    @Test
    void next는_원래_Generation을_바꾸지_않는다() {
        Generation now = new Generation(cells(10, 10,  11, 10,  12, 10), WIDTH);

        now.next(new ConwayRule());

        assertEquals(cells(10, 10,  11, 10,  12, 10), now.liveCells());
    }

    @Test
    void 넘겨준_좌표를_나중에_고쳐도_Generation은_바뀌지_않는다() {
        // java.awt.Point는 x, y를 고칠 수 있는 객체다. 넘겨준 쪽이 나중에 그 Point를 고치는 경우.
        Point mine = new Point(10, 10);
        Set<Point> given = new HashSet<>();
        given.add(mine);
        Generation now = new Generation(given, WIDTH);

        mine.x = 20;

        assertEquals(cells(10, 10), now.liveCells());
    }

    @Test
    void liveCells로_꺼낸_좌표를_고쳐도_Generation은_바뀌지_않는다() {
        Generation now = new Generation(cells(10, 10), WIDTH);

        Point taken = now.liveCells().iterator().next();
        taken.x = 20;

        assertEquals(cells(10, 10), now.liveCells());
    }

    @Test
    void 판_밖의_좌표로는_만들_수_없다() {
        assertThrows(IllegalArgumentException.class, () -> new Generation(cells(64, 10), WIDTH));   // x는 0~63
        assertThrows(IllegalArgumentException.class, () -> new Generation(cells(10, -1), WIDTH));   // y는 0~63
    }

    /** (x, y)를 차례로 적어 좌표 집합을 만든다. cells(10, 10, 11, 10)은 (10,10)과 (11,10). */
    private static Set<Point> cells(int... xy) {
        Set<Point> result = new HashSet<>();
        for (int i = 0; i < xy.length; i += 2) {
            result.add(new Point(xy[i], xy[i + 1]));
        }
        return result;
    }
}
