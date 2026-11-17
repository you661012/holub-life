import com.holub.life.*;
import java.awt.Point;
import java.util.*;

/** 같은 조건으로 잰다: 같은 시작 배치에서 500세대를 진행만 한다 (판 읽기·결말 판정 없음). 5번 중 가장 빠른 값. */
public class Fair {
    static long board(Set<Point> start) {
        Cell b = Exp.newBoard(new RuleHolder(), start);
        long t = System.nanoTime();
        for (int i = 0; i < 500; i++) Exp.step(b);
        return System.nanoTime() - t;
    }
    static long generation(Set<Point> start) {
        Generation g = new Generation(start, 64);
        Rule rule = new ConwayRule();
        long t = System.nanoTime();
        for (int i = 0; i < 500; i++) g = g.next(rule);
        return System.nanoTime() - t;
    }
    static long predict(Set<Point> start) {
        Generation g = new Generation(start, 64);
        long t = System.nanoTime();
        new EndingPredictor(500).predict(g, new ConwayRule());
        return System.nanoTime() - t;
    }
    public static void main(String[] a) {
        Map<String, Set<Point>> cases = new LinkedHashMap<>();
        cases.put("Blinker 하나", Exp.pts(10, 10, 11, 10, 12, 10));
        cases.put("무작위 30% (시드 1)", Exp.random(1));
        for (Map.Entry<String, Set<Point>> c : cases.entrySet()) {
            for (int i = 0; i < 3; i++) { board(c.getValue()); generation(c.getValue()); }
            long b = Long.MAX_VALUE, g = Long.MAX_VALUE, p = Long.MAX_VALUE;
            for (int i = 0; i < 5; i++) { b = Math.min(b, board(c.getValue())); g = Math.min(g, generation(c.getValue())); p = Math.min(p, predict(c.getValue())); }
            System.out.printf("%s: Neighborhood 판 %.0f ms / Generation %.0f ms / EndingPredictor.predict(한도 500) %.0f ms%n", c.getKey(), b / 1e6, g / 1e6, p / 1e6);
        }
    }
}
