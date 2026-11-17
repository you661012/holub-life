import com.holub.life.*;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;

/** 저장소 밖 실험. 방법 가(판을 하나 더 만든다)와 방법 나(좌표 집합으로 계산한다)를 같은 판에 돌려 본다. */
public class Exp {
    static final Rectangle SURFACE = new Rectangle(0, 0, 512, 512);
    static final int LIMIT = 500;

    // ───────── 방법 가: 원본 Neighborhood 판을 하나 더 만들어 돌린다 ─────────
    static Cell newBoard(RuleHolder holder, Set<Point> start) {
        Cell board = new Neighborhood(8, new Neighborhood(8, new Resident(holder)));
        for (Point p : start)
            board.userClicked(new Point(p.x * 8 + 1, p.y * 8 + 1), SURFACE);
        return board;
    }

    static void step(Cell board) {
        if (board.figureNextState(Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY,
                                  Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY))
            board.transition();
    }

    static Set<Point> live(Cell board) {
        Collector c = new Collector();
        board.transfer(c, new Point(0, 0), Cell.STORE);
        return c.cells;
    }

    static class Collector implements Cell.Memento {
        final Set<Point> cells = new HashSet<>();
        public void markAsAlive(Point p)    { cells.add(new Point(p)); }
        public boolean isAlive(Point p)     { return cells.contains(p); }
        public void load(InputStream in)    { throw new UnsupportedOperationException(); }
        public void flush(OutputStream out) { throw new UnsupportedOperationException(); }
    }

    // ───────── 방법 나: 살아 있는 칸의 좌표 집합과 Rule만으로 다음 세대를 계산한다 ─────────
    static Set<Point> next(Set<Point> cells, Rule rule, boolean wall) {
        Map<Point, Integer> neighbors = new HashMap<>();
        for (Point p : cells)
            for (int dy = -1; dy <= 1; dy++)
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0) continue;
                    int x = p.x + dx, y = p.y + dy;
                    if (wall && (x < 0 || x > 63 || y < 0 || y > 63)) continue;   // 판 밖은 없는 칸
                    neighbors.merge(new Point(x, y), 1, Integer::sum);
                }
        Set<Point> result = new HashSet<>();
        for (Map.Entry<Point, Integer> e : neighbors.entrySet())
            if (rule.nextState(cells.contains(e.getKey()), e.getValue()))
                result.add(e.getKey());
        for (Point p : cells)                                   // 이웃이 0개인 살아 있는 칸 (B/S에 0이 있을 때)
            if (!neighbors.containsKey(p) && rule.nextState(true, 0))
                result.add(p);
        return result;
    }

    // ───────── 결말 판정 (두 방법이 같이 쓴다) ─────────
    interface Stepper { Set<Point> cells(); void step(); }

    static String predict(Stepper s) {
        Map<Set<Point>, Integer> seen = new HashMap<>();
        for (int gen = 0; gen <= LIMIT; gen++) {
            Set<Point> now = s.cells();
            if (now.isEmpty()) return gen + "세대 뒤 소멸";
            Integer first = seen.get(now);
            if (first != null) {
                int period = gen - first;
                return period == 1 ? first + "세대 뒤 정지 (" + now.size() + "칸)"
                                   : first + "세대 뒤부터 반복, 주기 " + period;
            }
            seen.put(now, gen);
            s.step();
        }
        return LIMIT + "세대 안에 결말 없음 (미확정)";
    }

    static Stepper methodA(Set<Point> start, RuleHolder holder) {
        Cell board = newBoard(holder, start);
        return new Stepper() {
            public Set<Point> cells() { return live(board); }
            public void step()        { Exp.step(board); }
        };
    }

    static Stepper methodB(Set<Point> start, Rule rule, boolean wall) {
        return new Stepper() {
            Set<Point> cells = new HashSet<>(start);
            public Set<Point> cells() { return cells; }
            public void step()        { cells = next(cells, rule, wall); }
        };
    }

    static Set<Point> pts(int... xy) {
        Set<Point> s = new HashSet<>();
        for (int i = 0; i < xy.length; i += 2) s.add(new Point(xy[i], xy[i + 1]));
        return s;
    }

    static Set<Point> random(long seed) {
        Set<Point> s = new HashSet<>();
        Random r = new Random(seed);
        for (int y = 0; y < 64; y++)
            for (int x = 0; x < 64; x++)
                if (r.nextInt(100) < 30) s.add(new Point(x, y));
        return s;
    }

    public static void main(String[] args) throws Exception {
        Rule conway = new ConwayRule();
        Map<String, Set<Point>> cases = new LinkedHashMap<>();
        cases.put("한 칸",            pts(10, 10));
        cases.put("Block (2x2)",      pts(10, 10, 11, 10, 10, 11, 11, 11));
        cases.put("Blinker (가로 3)", pts(10, 10, 11, 10, 12, 10));
        cases.put("Glider (왼쪽 위)", pts(1, 0, 2, 1, 0, 2, 1, 2, 2, 2));
        cases.put("무작위 시드 1",    random(1));
        cases.put("무작위 시드 2",    random(2));

        System.out.println("== 1. 같은 판, 세 가지 계산 ==");
        for (Map.Entry<String, Set<Point>> c : cases.entrySet()) {
            System.out.println("[" + c.getKey() + "]");
            System.out.println("   가 (판을 하나 더)        : " + predict(methodA(c.getValue(), new RuleHolder())));
            System.out.println("   나 (좌표 집합, 벽 있음)  : " + predict(methodB(c.getValue(), conway, true)));
            System.out.println("   나 (좌표 집합, 벽 없음)  : " + predict(methodB(c.getValue(), conway, false)));
        }

        System.out.println("\n== 2. 가와 나(벽 있음)가 세대마다 칸 위치까지 같은가 (시드 1~3, 300세대) ==");
        for (long seed = 1; seed <= 3; seed++) {
            Stepper a = methodA(random(seed), new RuleHolder());
            Stepper b = methodB(random(seed), conway, true);
            int firstDiff = -1;
            for (int gen = 0; gen <= 300 && firstDiff < 0; gen++) {
                if (!a.cells().equals(b.cells())) firstDiff = gen;
                a.step(); b.step();
            }
            System.out.println("   시드 " + seed + ": " + (firstDiff < 0 ? "301개 세대 모두 같음" : firstDiff + "세대에서 달라짐"));
        }

        System.out.println("\n== 3. 걸리는 시간 (무작위 시드 1, 500세대 미리 돌리기, 5번 중 가장 빠른 값) ==");
        for (int warm = 0; warm < 3; warm++) { predict(methodA(random(1), new RuleHolder())); predict(methodB(random(1), conway, true)); }
        long bestA = Long.MAX_VALUE, bestB = Long.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            long t = System.nanoTime(); predict(methodA(random(1), new RuleHolder())); bestA = Math.min(bestA, System.nanoTime() - t);
            t = System.nanoTime();      predict(methodB(random(1), conway, true));     bestB = Math.min(bestB, System.nanoTime() - t);
        }
        System.out.printf("   가: %.1f ms%n   나: %.1f ms%n", bestA / 1e6, bestB / 1e6);

        System.out.println("\n== 4. 가: 판 두 개를 서로 다른 스레드에서 동시에 돌리면 (Neighborhood의 static 필드) ==");
        Field nesting = Neighborhood.class.getDeclaredField("nestingLevel");
        nesting.setAccessible(true);
        System.out.println("   시작 전 nestingLevel = " + nesting.getInt(null) + " (정상값 -1)");
        int broken = 0, trials = 20;
        for (int trial = 0; trial < trials; trial++) {
            nesting.setInt(null, -1);
            Cell screen = newBoard(new RuleHolder(), random(1));     // 화면의 판 역할
            Cell ahead  = newBoard(new RuleHolder(), random(1));     // 미리 돌리는 판 역할
            Thread t1 = new Thread(() -> { for (int i = 0; i < 300; i++) step(screen); });
            Thread t2 = new Thread(() -> { for (int i = 0; i < 300; i++) step(ahead); });
            t1.start(); t2.start(); t1.join(); t2.join();
            if (nesting.getInt(null) != -1) broken++;
        }
        System.out.println("   " + trials + "번 중 " + broken + "번, 끝난 뒤 nestingLevel이 -1이 아니었다");
        nesting.setInt(null, -1);

        Cell one = newBoard(new RuleHolder(), random(1));
        Cell two = newBoard(new RuleHolder(), random(1));
        for (int i = 0; i < 300; i++) { step(one); step(two); }
        System.out.println("   같은 스레드에서 번갈아 돌리면: nestingLevel = " + nesting.getInt(null)
                + ", 두 판이 같은가 = " + live(one).equals(live(two)));
    }
}
