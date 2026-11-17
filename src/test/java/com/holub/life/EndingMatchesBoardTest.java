package com.holub.life;

import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 직접 입력할 수 있는 특이한 규칙들에서도 예측한 결말이 실제 판(Neighborhood)의 진행과 맞는지 확인한다.
 * 예측기의 답을 그대로 믿지 않고, 같은 배치를 실제 판에서 돌려 그 세대의 판이 정말 그런지 본다.
 */
class EndingMatchesBoardTest {

    private static final int LIMIT = 60;

    // 빈 판에서도 태어나는 규칙(B0), 아무것도 태어나지 않는 규칙, 한번 살면 죽지 않는 규칙 등
    private static final String[] RULES = {
            "B0/S", "B0/S8", "B0/S012345678", "B/S", "B/S012345678",
            "B012345678/S012345678", "B012345678/S", "B1/S", "B2/S", "B3/S012345678", "B3/S23", "B36/S23"
    };

    @Test
    void 빈_판에서_예측한_결말이_실제_판과_맞는다() {
        for (String notation : RULES) {
            assertEndingMatchesBoard(notation, new HashSet<>());
        }
    }

    @Test
    void 가로_3칸에서_예측한_결말이_실제_판과_맞는다() {
        for (String notation : RULES) {
            assertEndingMatchesBoard(notation, Set.of(new Point(10, 10), new Point(11, 10), new Point(12, 10)));
        }
    }

    @Test
    void 무작위_판에서_예측한_결말이_실제_판과_맞는다() {
        Set<Point> start = new HashSet<>();
        Random r = new Random(1);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                if (r.nextInt(100) < 30) {
                    start.add(new Point(x, y));
                }
            }
        }
        for (String notation : RULES) {
            assertEndingMatchesBoard(notation, start);
        }
    }

    private static void assertEndingMatchesBoard(String notation, Set<Point> start) {
        Rule rule = new BirthSurvivalRule(notation);
        Ending ending = new EndingPredictor(LIMIT).predict(new Generation(start, 64), rule);

        // 같은 배치를 실제 판에서 돌려 세대마다 살아 있는 칸을 적어 둔다
        RuleHolder holder = new RuleHolder();
        holder.change(rule);
        Cell board = new Neighborhood(8, new Neighborhood(8, new Resident(holder)));
        for (Point cell : start) {
            board.userClicked(new Point(cell.x * 8 + 1, cell.y * 8 + 1), new Rectangle(0, 0, 512, 512));
        }
        List<Set<Point>> actual = new ArrayList<>();
        for (int n = 0; n <= LIMIT + 2; n++) {
            actual.add(liveCells(board));
            if (board.figureNextState(Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY,
                                      Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY)) {
                board.transition();
            }
        }

        String where = notation + ", 시작 " + start.size() + "칸";
        int after = ending.after();
        switch (ending.kind()) {
            case EXTINCT:     // 그 세대에 비고, 다음 세대에도 비어 있다
                assertTrue(actual.get(after).isEmpty(), where);
                assertTrue(actual.get(after + 1).isEmpty(), where);
                break;
            case STILL:       // 그 세대와 다음 세대가 같고, 비어 있지 않다
                assertEquals(actual.get(after), actual.get(after + 1), where);
                assertFalse(actual.get(after).isEmpty(), where);
                break;
            case REPEATING:   // 주기만큼 뒤에 같은 모양이고, 바로 다음 세대와는 다르다
                assertEquals(actual.get(after), actual.get(after + ending.period()), where);
                assertNotEquals(actual.get(after), actual.get(after + 1), where);
                break;
            default:          // UNDECIDED: 한도까지 같은 모양이 한 번도 다시 나오지 않았다
                assertEquals(LIMIT + 1, new HashSet<>(actual.subList(0, LIMIT + 1)).size(), where);
        }
    }

    private static Set<Point> liveCells(Cell board) {
        LiveCellCollector collector = new LiveCellCollector();
        board.transfer(collector, new Point(0, 0), Cell.STORE);
        return collector.cells();
    }
}
