import com.holub.life.*;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.util.*;

/** 빈 판을 B0/S로 돌리면 실제 판과 예측기가 각각 뭐라고 하는지 본다. */
public class B0 {
    static class Collector implements Cell.Memento {
        final Set<Point> cells = new HashSet<>();
        public void markAsAlive(Point p)    { cells.add(new Point(p)); }
        public boolean isAlive(Point p)     { return cells.contains(p); }
        public void load(InputStream in)    { throw new UnsupportedOperationException(); }
        public void flush(OutputStream out) { throw new UnsupportedOperationException(); }
    }
    static int count(Cell board) { Collector c = new Collector(); board.transfer(c, new Point(0, 0), Cell.STORE); return c.cells.size(); }
    static void step(Cell board) {
        if (board.figureNextState(Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY))
            board.transition();
    }
    public static void main(String[] a) {
        Rule rule = new BirthSurvivalRule("B0/S");
        RuleHolder holder = new RuleHolder();
        holder.change(rule);
        Cell board = new Neighborhood(8, new Neighborhood(8, new Resident(holder)));
        System.out.print("실제 판 (빈 판, B0/S) 세대별 살아 있는 칸 수: " + count(board));
        for (int i = 0; i < 4; i++) { step(board); System.out.print(" → " + count(board)); }
        System.out.println();

        Ending e = new EndingPredictor(500).predict(new Generation(new HashSet<>(), 64), rule);
        System.out.println("예측기의 답: " + e.kind() + ", after=" + e.after() + ", period=" + e.period());
    }
}
