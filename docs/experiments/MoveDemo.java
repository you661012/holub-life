import com.holub.life.*;
import java.awt.Point;
import java.util.*;

/**
 * 미리 돌리면서 "전에 본 모양이 자리만 옮겨 다시 나왔는가"를 찾아본다. (이동 예측을 넣을지 따져 보려고 쓴 것. 기능으로 넣지는 않았다)
 * 판 전체가 한 덩어리로 옮겨 간 경우만 찾는다. 찾으면 그 뒤로 매 세대가 "주기만큼 앞의 세대를 같은 만큼 옮긴 것"과 같은지 확인해,
 * 그것이 마지막으로 맞은 세대를 적는다.
 */
public class MoveDemo {
    public static void main(String[] a) {
        show("Glider (왼쪽 위)", cells(1,0, 2,1, 0,2, 1,2, 2,2));
        show("LWSS 가로로 나는 우주선", cells(11,10, 14,10, 10,11, 10,12, 14,12, 10,13, 11,13, 12,13, 13,13));
        show("Glider + 멀리 Block", cells(1,0, 2,1, 0,2, 1,2, 2,2,  40,10, 41,10, 40,11, 41,11));
        show("Blinker", cells(10,10, 11,10, 12,10));
        show("Block", cells(10,10, 11,10, 10,11, 11,11));
    }

    static void show(String name, Set<Point> start) {
        Rule rule = new ConwayRule();
        Ending end = new EndingPredictor(500).predict(new Generation(start, 64), rule);

        List<Generation> gens = new ArrayList<>();
        Generation g = new Generation(start, 64);
        for (int n = 0; n <= 500; n++) { gens.add(g); g = g.next(rule); }

        // 자리만 옮겨 다시 나온 첫 경우를 찾는다
        String found = "판 전체의 이동은 찾지 못함";
        Map<Set<Point>, Integer> seenShape = new HashMap<>();
        Map<Integer, Point> origin = new HashMap<>();
        outer:
        for (int n = 0; n <= 500; n++) {
            Set<Point> live = gens.get(n).liveCells();
            if (live.isEmpty()) break;
            Point o = origin(live);
            Set<Point> shape = shape(live, o);
            origin.put(n, o);
            Integer first = seenShape.get(shape);
            if (first != null) {
                int dx = o.x - origin.get(first).x, dy = o.y - origin.get(first).y;
                if (dx != 0 || dy != 0) {
                    int period = n - first;
                    // 이 이동이 몇 세대까지 이어지는가
                    int until = n;
                    while (until + 1 <= 500 && movedBy(gens.get(until + 1 - period), gens.get(until + 1), dx, dy)) until++;
                    found = first + "세대의 모양이 " + n + "세대에 (" + dx + "," + dy + ")만큼 옮겨 다시 나옴 (주기 " + period
                          + "). 마지막으로 맞은 세대는 " + until;
                    break outer;
                }
                break;   // 같은 자리에 다시 나왔다 = 정지나 반복
            }
            seenShape.put(shape, n);
        }
        System.out.println("[" + name + "]\n   지금의 예측: " + end.kind() + ", " + end.after() + "세대 뒤, 주기 " + end.period()
                + "\n   이동: " + found);
    }

    static boolean movedBy(Generation before, Generation after, int dx, int dy) {
        Set<Point> moved = new HashSet<>();
        for (Point p : before.liveCells()) moved.add(new Point(p.x + dx, p.y + dy));
        return !moved.isEmpty() && moved.equals(after.liveCells());
    }
    static Point origin(Set<Point> live) {
        int mx = Integer.MAX_VALUE, my = Integer.MAX_VALUE;
        for (Point p : live) { mx = Math.min(mx, p.x); my = Math.min(my, p.y); }
        return new Point(mx, my);
    }
    static Set<Point> shape(Set<Point> live, Point o) {
        Set<Point> s = new HashSet<>();
        for (Point p : live) s.add(new Point(p.x - o.x, p.y - o.y));
        return s;
    }
    static Set<Point> cells(int... xy) {
        Set<Point> s = new HashSet<>();
        for (int i = 0; i < xy.length; i += 2) s.add(new Point(xy[i], xy[i + 1]));
        return s;
    }
}
