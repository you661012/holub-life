import com.holub.life.*;
import java.awt.Point;
import java.util.*;

public class Exp3 {
    static long run(Set<Point> start) {
        long t = System.nanoTime();
        Generation g = new Generation(start, 64);
        Rule rule = new ConwayRule();
        for (int i = 0; i < 500; i++) g = g.next(rule);
        return System.nanoTime() - t;
    }
    public static void main(String[] a) {
        Set<Point> dense = Exp.random(1);
        Set<Point> blinker = new HashSet<>(List.of(new Point(10,10), new Point(11,10), new Point(12,10)));
        for (int i = 0; i < 3; i++) { run(dense); run(blinker); }
        long d = Long.MAX_VALUE, b = Long.MAX_VALUE;
        for (int i = 0; i < 5; i++) { d = Math.min(d, run(dense)); b = Math.min(b, run(blinker)); }
        System.out.printf("Generation 500세대: 무작위 30%% 판 %.0f ms, Blinker 하나 %.0f ms%n", d / 1e6, b / 1e6);
    }
}
