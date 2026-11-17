package com.holub.life;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** LiveCellCollector가 화면의 판(Neighborhood)에서 살아 있는 칸의 좌표를 그대로 모으는지 확인한다. */
class LiveCellCollectorTest {

    @Test
    void 클릭한_칸들의_좌표를_모은다() {
        Cell board = new Neighborhood(8, new Neighborhood(8, new Resident(new RuleHolder())));
        click(board, 0, 0);
        click(board, 7, 8);      // 구역 경계(8칸마다)를 넘는 칸
        click(board, 63, 63);

        LiveCellCollector collector = new LiveCellCollector();
        board.transfer(collector, new Point(0, 0), Cell.STORE);

        assertEquals(Set.of(new Point(0, 0), new Point(7, 8), new Point(63, 63)), collector.cells());
    }

    @Test
    void 빈_판이면_아무것도_모으지_않는다() {
        Cell board = new Neighborhood(8, new Neighborhood(8, new Resident(new RuleHolder())));

        LiveCellCollector collector = new LiveCellCollector();
        board.transfer(collector, new Point(0, 0), Cell.STORE);

        assertTrue(collector.cells().isEmpty());
    }

    private static void click(Cell board, int x, int y) {
        board.userClicked(new Point(x * 8 + 1, y * 8 + 1), new Rectangle(0, 0, 512, 512));
    }
}
