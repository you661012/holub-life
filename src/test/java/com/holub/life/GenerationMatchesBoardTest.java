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
 * Generation의 계산이 실제 Neighborhood 판(64×64)의 진행과 같은지 확인한다.
 * 세대를 진행하는 코드가 두 벌(Neighborhood·Resident와 Generation)이므로, 둘이 어긋나면 여기서 잡힌다.
 * 같은 배치를 양쪽에 놓고 세대마다 살아 있는 칸의 위치를 전부 비교한다.
 */
class GenerationMatchesBoardTest {

    private static final int WIDTH = 64;
    private static final Rectangle SURFACE = new Rectangle(0, 0, 512, 512);   // 판 전체 영역 (칸 하나가 8픽셀)

    @Test
    void 무작위_판에서_Conway로_300세대_내내_실제_판과_같다() {
        assertSameAsBoard(randomCells(1), new ConwayRule(), 300);
        assertSameAsBoard(randomCells(2), new ConwayRule(), 300);
        assertSameAsBoard(randomCells(3), new ConwayRule(), 300);
    }

    @Test
    void 무작위_판에서_HighLife로_300세대_내내_실제_판과_같다() {
        assertSameAsBoard(randomCells(1), new HighLifeRule(), 300);
    }

    @Test
    void 직접_입력한_규칙으로도_실제_판과_같다() {
        assertSameAsBoard(randomCells(1), new BirthSurvivalRule("B2/S"), 100);      // Seeds
    }

    @Test
    void 이웃이_0개여도_태어나는_규칙에서도_실제_판과_같다() {
        // B0: 주변에 아무것도 없는 빈 칸에서도 태어난다. 살아 있는 칸 주변만 계산하면 틀리는 경우다.
        assertSameAsBoard(randomCells(1), new BirthSurvivalRule("B0/S"), 20);
    }

    @Test
    void Glider가_판_끝에_부딪혀_멈출_때까지_실제_판과_같다() {
        // 왼쪽 위에서 오른쪽 아래로 가는 Glider. 판 끝에 닿으면 4칸으로 멈춘다.
        Set<Point> glider = Set.of(new Point(1, 0), new Point(2, 1),
                                   new Point(0, 2), new Point(1, 2), new Point(2, 2));

        Generation last = assertSameAsBoard(glider, new ConwayRule(), 260);

        assertEquals(4, last.liveCells().size());
        assertEquals(last, last.next(new ConwayRule()));      // 더는 변하지 않는다
    }

    /**
     * start 배치를 실제 판과 Generation에 똑같이 놓고, generations 세대 동안 매 세대 살아 있는 칸이 같은지 확인한다.
     * @return 마지막 세대의 Generation
     */
    private Generation assertSameAsBoard(Set<Point> start, Rule rule, int generations) {
        RuleHolder holder = new RuleHolder();
        holder.change(rule);
        Cell board = newBoard(holder);
        for (Point cell : start) {
            click(board, cell.x, cell.y);
        }
        Generation generation = new Generation(start, WIDTH);

        for (int n = 0; n < generations; n++) {
            assertEquals(liveCells(board), generation.liveCells(), n + "세대에서 달라졌다");
            step(board);
            generation = generation.next(rule);
        }
        assertEquals(liveCells(board), generation.liveCells(), generations + "세대에서 달라졌다");
        return generation;
    }

    /** 시드로 정해지는 무작위 배치 (칸마다 30% 확률). NeighborhoodRuleTest의 무작위 판과 같은 방법이다. */
    private Set<Point> randomCells(long seed) {
        Set<Point> cells = new HashSet<>();
        Random random = new Random(seed);
        for (int y = 0; y < WIDTH; y++)
            for (int x = 0; x < WIDTH; x++)
                if (random.nextInt(100) < 30)
                    cells.add(new Point(x, y));
        return cells;
    }

    // ── 아래 헬퍼는 NeighborhoodRuleTest의 것과 같다 ──

    /** 실제 프로그램(Universe)과 같은 크기의 판: 8×8 구역이 8×8개 = 64×64칸. */
    private Cell newBoard(RuleHolder holder) {
        return new Neighborhood(8, new Neighborhood(8, new Resident(holder)));
    }

    /** 세포 좌표 (x, y)를 클릭한다. */
    private void click(Cell board, int x, int y) {
        board.userClicked(new Point(x * 8 + 1, y * 8 + 1), SURFACE);
    }

    /** 한 세대 진행한다. Universe의 tick()과 같은 방식: 바뀔 것이 있을 때만 transition(). */
    private void step(Cell board) {
        if (board.figureNextState(
                Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY,
                Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY)) {
            board.transition();
        }
    }

    /** 지금 살아 있는 세포의 좌표를 모두 돌려준다. */
    private Set<Point> liveCells(Cell board) {
        LiveCellCollector collector = new LiveCellCollector();
        board.transfer(collector, new Point(0, 0), Cell.STORE);   // STORE: 판 → collector 방향 (읽기)
        return collector.cells;
    }

    /** 살아 있는 세포의 좌표를 모으는 객체. 원본의 Store 기능이 쓰는 Cell.Memento 인터페이스를 구현한다. */
    private static class LiveCellCollector implements Cell.Memento {
        final Set<Point> cells = new HashSet<>();

        public void markAsAlive(Point location) {
            cells.add(new Point(location));
        }

        public boolean isAlive(Point location) {
            return cells.contains(location);
        }

        public void load(InputStream in)    { throw new UnsupportedOperationException(); }
        public void flush(OutputStream out) { throw new UnsupportedOperationException(); }
    }
}
