import com.holub.life.*;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/** 세대가 진행되는 도중에 다른 스레드가 판을 읽으면 어떤 판이 읽히는지 본다. */
public class Snapshot {
    static final int GENERATIONS = 300;

    public static void main(String[] args) throws Exception {
        for (int run = 1; run <= 3; run++) {
            System.out.println("잠금 없음 " + run + "회: " + run(false, run));
        }
        for (int run = 1; run <= 3; run++) {
            System.out.println("잠금 있음 " + run + "회: " + run(true, run));
        }
    }

    static String run(boolean locked, int seed) throws Exception {
        RuleHolder holder = new RuleHolder();
        Cell board = new Neighborhood(8, new Neighborhood(8, new Resident(holder)));
        Random r = new Random(seed);
        Set<Point> start = new HashSet<>();
        for (int y = 0; y < 64; y++)
            for (int x = 0; x < 64; x++)
                if (r.nextInt(100) < 30) {
                    board.userClicked(new Point(x * 8 + 1, y * 8 + 1), new Rectangle(0, 0, 512, 512));
                    start.add(new Point(x, y));
                }

        // 있을 수 있는 올바른 판: 0세대 ~ 300세대
        Set<Set<Point>> valid = new HashSet<>();
        Generation g = new Generation(start, 64);
        for (int n = 0; n <= GENERATIONS; n++) {
            valid.add(g.liveCells());
            g = g.next(holder.current());
        }

        Object lock = new Object();
        Thread clock = new Thread(() -> {
            for (int n = 0; n < GENERATIONS; n++) {
                if (locked) {
                    synchronized (lock) { tick(board); }
                } else {
                    tick(board);
                }
                // 실제 시계처럼 세대 사이에 쉰다 (두 경우 모두 같게)
                try { Thread.sleep(1); } catch (InterruptedException e) { return; }
            }
        });

        int reads = 0, broken = 0;
        clock.start();
        while (clock.isAlive()) {
            Set<Point> seen;
            if (locked) {
                synchronized (lock) { seen = read(board); }
            } else {
                seen = read(board);
            }
            reads++;
            if (!valid.contains(seen)) broken++;
        }
        clock.join();
        return "판을 " + reads + "번 읽음, 그중 어느 세대의 판도 아닌 것 " + broken + "번";
    }

    static void tick(Cell board) {
        if (board.figureNextState(Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY,
                                  Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY))
            board.transition();
    }

    static Set<Point> read(Cell board) {
        Collector c = new Collector();
        board.transfer(c, new Point(0, 0), Cell.STORE);
        return c.cells;
    }

    static class Collector implements Cell.Memento {
        final Set<Point> cells = new HashSet<>();
        public void markAsAlive(Point p) { cells.add(new Point(p)); }
        public boolean isAlive(Point p) { return cells.contains(p); }
        public void load(InputStream in) { throw new UnsupportedOperationException(); }
        public void flush(OutputStream out) { throw new UnsupportedOperationException(); }
    }
}
