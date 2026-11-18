import com.holub.life.*;
import java.awt.Point;
import java.util.*;

/**
 * 한도 500세대 안에서는 미확정인데 그 뒤에 정지나 소멸로 끝나는 판이 있는지 찾아본다. (Conway)
 * 자동 정지는 미확정에서 멈추지 않으므로, 그런 판이 있으면 판이 멈춰도 시계는 계속 돈다. 확장 2 문서 4-1절의 자동 정지 표.
 */
public class LateEnding {
    public static void main(String[] a) {
        System.out.println("[무작위 판. 기존 판 테스트와 같은 방식으로 칸을 고른다]");
        for (int pct : new int[]{30, 10, 5})
            for (int seed = 1; seed <= 40; seed++) {
                Random r = new Random(seed);
                Set<Point> s = new HashSet<>();
                for (int y = 0; y < 64; y++)
                    for (int x = 0; x < 64; x++)
                        if (r.nextInt(100) < pct) s.add(new Point(x, y));
                check(pct + "% 시드 " + seed, s, 5000, true);
            }

        System.out.println("[오래 사는 것으로 알려진 작은 배치]");
        Map<String, int[]> m = new LinkedHashMap<>();
        m.put("R-pentomino", new int[]{1,0, 2,0, 0,1, 1,1, 1,2});
        m.put("Acorn", new int[]{1,0, 3,1, 0,2, 1,2, 4,2, 5,2, 6,2});
        m.put("Diehard", new int[]{6,0, 0,1, 1,1, 1,2, 5,2, 6,2, 7,2});
        m.put("Pi-heptomino", new int[]{0,0, 1,0, 2,0, 0,1, 2,1, 0,2, 2,2});
        m.put("B-heptomino", new int[]{0,0, 2,0, 3,0, 0,1, 1,1, 2,1, 1,2});
        for (var en : m.entrySet())
            for (int[] o : new int[][]{{30, 30}, {10, 10}, {50, 20}}) {
                Set<Point> s = new HashSet<>();
                int[] xy = en.getValue();
                for (int i = 0; i < xy.length; i += 2) s.add(new Point(o[0] + xy[i], o[1] + xy[i + 1]));
                check(en.getKey() + " (" + o[0] + "," + o[1] + ")", s, 20000, false);
            }
    }

    // onlyUndecided가 참이면 500세대 안에 미확정인 판만 출력한다
    static void check(String name, Set<Point> s, int longLimit, boolean onlyUndecided) {
        Generation g = new Generation(s, 64);
        Ending e500 = new EndingPredictor(500).predict(g, new ConwayRule());
        if (onlyUndecided && e500.kind() != Ending.Kind.UNDECIDED) return;
        Ending eLong = new EndingPredictor(longLimit).predict(g, new ConwayRule());
        System.out.println(name + ": 한도 500 → " + e500.kind() + " | 한도 " + longLimit + " → " + eLong.kind()
                + ", " + eLong.after() + "세대 뒤, 주기 " + eLong.period());
    }
}
