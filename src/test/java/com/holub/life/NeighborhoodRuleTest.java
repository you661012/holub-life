package com.holub.life;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 실제 Neighborhood 판(64×64)에서 규칙이 맞게 동작하는지 확인한다.
 * 지금까지의 테스트는 Resident 하나, Rule 하나씩만 보았다. 여기서는 판 전체를 만들어 여러 세대를 진행한다.
 */
class NeighborhoodRuleTest {

    @Test
    void 가로_3칸_두_줄은_Conway에서_1세대에_4칸이_되고_2세대에_전멸한다() {
        // ── 준비: Conway 판에 가로 3칸을 한 줄 띄워 두 줄 놓는다 ──
        RuleHolder holder = new RuleHolder();            // 기본값은 Conway
        Cell board = newBoard(holder);

        click(board, 10, 10);  click(board, 11, 10);  click(board, 12, 10);   // 윗줄
        click(board, 10, 12);  click(board, 11, 12);  click(board, 12, 12);   // 아랫줄 (y=11은 비워 둠)

        // ── 1세대: 가운데 열에 4칸 (위 2칸, 아래 2칸. 가운데 (11,11)은 이웃이 6개라 Conway에서는 비어 있음) ──
        step(board, 1);
        assertEquals(
                Set.of(new Point(11, 9), new Point(11, 10), new Point(11, 12), new Point(11, 13)),
                liveCells(board));

        // ── 2세대: 전멸 ──
        step(board, 1);
        assertEquals(0, liveCells(board).size());
    }

    @Test
    void 가로_3칸_두_줄은_HighLife에서_1세대에_세로_5칸이_되고_2세대에_3x3이_된다() {
        // ── 준비: 위 테스트와 같은 배치. 세포를 놓기 전에 규칙만 HighLife로 정한다 ──
        RuleHolder holder = new RuleHolder();
        holder.change(new HighLifeRule());               // 처음부터 끝까지 HighLife (도중에 바꾸지 않는다)
        Cell board = newBoard(holder);

        click(board, 10, 10);  click(board, 11, 10);  click(board, 12, 10);   // 윗줄
        click(board, 10, 12);  click(board, 11, 12);  click(board, 12, 12);   // 아랫줄 (y=11은 비워 둠)

        // ── 1세대: 가운데 열에 세로 5칸 (가운데 (11,11)은 이웃이 6개라 HighLife에서는 태어남) ──
        step(board, 1);
        assertEquals(
                Set.of(new Point(11, 9), new Point(11, 10), new Point(11, 11), new Point(11, 12), new Point(11, 13)),
                liveCells(board));

        // ── 2세대: 3×3 (x 10~12, y 10~12) ──
        step(board, 1);
        assertEquals(
                Set.of(new Point(10, 10), new Point(11, 10), new Point(12, 10),
                       new Point(10, 11), new Point(11, 11), new Point(12, 11),
                       new Point(10, 12), new Point(11, 12), new Point(12, 12)),
                liveCells(board));
    }

    @Test
    void Conway로_돌던_판의_규칙을_도중에_HighLife로_바꾸면_다음_세대부터_반영된다() {
        // ── 준비: Conway 판에 Ship(6칸)과, 멀리 떨어진 곳에 Blinker(3칸)를 놓는다 ──
        RuleHolder holder = new RuleHolder();            // 기본값은 Conway
        Cell board = newBoard(holder);

        // Ship: Conway에서는 변하지 않는 모양. 가운데 (11,11)은 비어 있고 이웃이 6개다.
        click(board, 10, 10);  click(board, 11, 10);
        click(board, 10, 11);                         click(board, 12, 11);
                               click(board, 11, 12);  click(board, 12, 12);

        // Blinker: 두 규칙 모두에서 계속 깜빡인다. 판을 계속 움직이게 하려고 놓는다.
        click(board, 41, 42);  click(board, 42, 42);  click(board, 43, 42);

        // ── Conway로 3세대: Ship은 그대로 ──
        step(board, 3);
        assertEquals(6 + 3, liveCells(board).size());    // Ship 6 + Blinker 3

        // ── 여기서 규칙을 바꾼다 ──
        holder.change(new HighLifeRule());

        // ── 바꾼 뒤: Ship이 7칸 → 4칸 → 0칸 (Blinker 3칸은 계속 남는다) ──
        step(board, 1);
        assertEquals(7 + 3, liveCells(board).size());    // 가운데 (11,11)이 태어남

        step(board, 1);
        assertEquals(4 + 3, liveCells(board).size());    // 네 귀퉁이만 남음

        step(board, 1);
        assertEquals(0 + 3, liveCells(board).size());    // Ship 자리는 전멸, Blinker만 남음
    }

    // ── 안전망: Neighborhood를 고치기 전에 만들었다. 고친 뒤에도 원본과 같게 동작하는지 지킨다 ──

    @Test
    void 무작위_배치_시드1은_60세대까지_세포_수가_원본과_같다() {
        assertCellCountsMatchOriginal(1,   1230, 640, 525, 473);
    }

    @Test
    void 무작위_배치_시드2는_60세대까지_세포_수가_원본과_같다() {
        assertCellCountsMatchOriginal(2,   1256, 623, 444, 421);
    }

    @Test
    void 무작위_배치_시드3은_60세대까지_세포_수가_원본과_같다() {
        assertCellCountsMatchOriginal(3,   1233, 770, 513, 548);
    }

    @Test
    void 구역_경계에_걸친_Blinker는_가로와_세로를_번갈아_반복한다() {
        RuleHolder holder = new RuleHolder();            // 기본값은 Conway
        Cell board = newBoard(holder);

        // 구역은 8칸마다 나뉜다. x=7과 x=8 사이, y=7과 y=8 사이가 경계라서 이 Blinker는 구역 네 개에 걸친다.
        click(board, 7, 7);  click(board, 8, 7);  click(board, 9, 7);

        Set<Point> horizontal = Set.of(new Point(7, 7), new Point(8, 7), new Point(9, 7));
        Set<Point> vertical   = Set.of(new Point(8, 6), new Point(8, 7), new Point(8, 8));

        step(board, 1);  assertEquals(vertical,   liveCells(board));
        step(board, 1);  assertEquals(horizontal, liveCells(board));
        step(board, 1);  assertEquals(vertical,   liveCells(board));
        step(board, 1);  assertEquals(horizontal, liveCells(board));
    }

    // ── 결함 재현: 판 전체가 조용할 때 ──

    @Test
    void 조용해진_판에서도_규칙을_바꾸면_다음_세대부터_반영된다() {
        // ── 준비: 위 테스트와 같지만 Blinker가 없다. Ship만 있으면 판 전체가 변하지 않는다 ──
        RuleHolder holder = new RuleHolder();            // 기본값은 Conway
        Cell board = newBoard(holder);

        click(board, 10, 10);  click(board, 11, 10);
        click(board, 10, 11);                         click(board, 12, 11);
                               click(board, 11, 12);  click(board, 12, 12);

        // ── Conway로 3세대: Ship은 그대로. 판에 변하는 세포가 하나도 없다 ──
        step(board, 3);
        assertEquals(6, liveCells(board).size());

        // ── 여기서 규칙을 바꾼다 ──
        holder.change(new HighLifeRule());

        // ── 바꾼 뒤: 올바르게 동작한다면 위 테스트처럼 7칸 → 4칸 → 0칸 ──
        step(board, 1);
        assertEquals(7, liveCells(board).size());

        step(board, 1);
        assertEquals(4, liveCells(board).size());

        step(board, 1);
        assertEquals(0, liveCells(board).size());
    }

    // ── 직접 입력한 규칙: Conway도 HighLife도 아닌 Seeds (B2/S) ──

    @Test
    void 직접_입력한_규칙_B2_S로도_판이_규칙대로_진행한다() {
        // ── 준비: 규칙을 문자열로 만들어 넣고, 세포 두 칸을 나란히 놓는다 ──
        RuleHolder holder = new RuleHolder();
        holder.change(new BirthSurvivalRule("B2/S"));    // 이웃이 2개면 태어나고, 살아 있는 세포는 항상 죽는다
        Cell board = newBoard(holder);

        click(board, 10, 10);  click(board, 11, 10);

        // ── 1세대: 두 칸은 죽고, 둘 모두와 맞닿은 위 두 칸과 아래 두 칸이 태어난다 ──
        step(board, 1);
        assertEquals(
                Set.of(new Point(10, 9),  new Point(11, 9),
                       new Point(10, 11), new Point(11, 11)),
                liveCells(board));

        // ── 2세대: 네 칸은 죽고, 그중 정확히 두 칸과 맞닿은 여섯 칸이 태어난다 (가운데 두 칸은 이웃이 4개라 비어 있다) ──
        step(board, 1);
        assertEquals(
                Set.of(new Point(10, 8),  new Point(11, 8),
                       new Point(9, 10),  new Point(12, 10),
                       new Point(10, 12), new Point(11, 12)),
                liveCells(board));
    }


    // ───────────────────────── 헬퍼 ─────────────────────────

    /** 화면 크기. 64칸 × 8픽셀 = 512. userClicked가 픽셀 좌표를 받기 때문에 필요하다. */
    private static final Rectangle SURFACE = new Rectangle(0, 0, 512, 512);

    /** Universe가 만드는 것과 같은 구조의 판: 8×8 Neighborhood 안에 8×8 Neighborhood, 그 안에 Resident. */
    private Cell newBoard(RuleHolder holder) {
        return new Neighborhood(8, new Neighborhood(8, new Resident(holder)));
    }

    /** 세포 좌표 (x, y)를 클릭한다. 죽어 있으면 살아나고, 살아 있으면 죽는다. (x는 열, y는 행, 0~63) */
    private void click(Cell board, int x, int y) {
        board.userClicked(new Point(x * 8 + 1, y * 8 + 1), SURFACE);
    }

    /**
     * 시드로 정해지는 무작위 배치(칸마다 30% 확률로 세포를 놓음)를 Conway로 60세대 돌리며 세포 수를 확인한다.
     * 기대값은 11/11에 수정 전 원본 소스를 같은 방법으로 돌려 얻은 값이다.
     */
    private void assertCellCountsMatchOriginal(long seed, int atStart, int after20, int after40, int after60) {
        RuleHolder holder = new RuleHolder();            // 기본값은 Conway
        Cell board = newBoard(holder);

        Random random = new Random(seed);                // 시드가 같으면 항상 같은 배치가 나온다
        for (int y = 0; y < 64; y++)
            for (int x = 0; x < 64; x++)
                if (random.nextInt(100) < 30)
                    click(board, x, y);

        assertEquals(atStart, liveCells(board).size());

        step(board, 20);
        assertEquals(after20, liveCells(board).size());

        step(board, 20);
        assertEquals(after40, liveCells(board).size());

        step(board, 20);
        assertEquals(after60, liveCells(board).size());
    }

    /** generations 세대만큼 진행한다. Universe의 tick()과 같은 방식: 바뀔 것이 있을 때만 transition(). */
    private void step(Cell board, int generations) {
        for (int i = 0; i < generations; i++) {
            if (board.figureNextState(
                    Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY,
                    Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY)) {
                board.transition();
            }
        }
    }

    /** 지금 살아 있는 세포의 좌표를 모두 돌려준다. */
    private Set<Point> liveCells(Cell board) {
        LiveCellCollector collector = new LiveCellCollector();
        board.transfer(collector, new Point(0, 0), Cell.STORE);   // STORE: 판 → collector 방향 (읽기)
        return collector.cells;
    }

    /**
     * 살아 있는 세포의 좌표를 모으는 객체.
     * 원본의 Store 기능이 쓰는 Cell.Memento 인터페이스를 구현해서, 판이 살아 있는 Resident마다 markAsAlive를 호출하게 한다.
     */
    private static class LiveCellCollector implements Cell.Memento {
        final Set<Point> cells = new HashSet<>();

        public void markAsAlive(Point location) {
            cells.add(new Point(location));   // 복사해서 담는다. Neighborhood가 Point 객체 하나를 옮겨 가며 재사용하기 때문이다.
        }

        public boolean isAlive(Point location) {
            return cells.contains(location);
        }

        // 파일 저장·읽기용 메서드. 이 테스트에서는 쓰지 않는다.
        public void load(InputStream in)    { throw new UnsupportedOperationException(); }
        public void flush(OutputStream out) { throw new UnsupportedOperationException(); }
    }
}
