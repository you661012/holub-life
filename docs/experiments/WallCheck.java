import com.holub.life.*;
import java.awt.Point;
import java.util.*;

/**
 * 날아가는 모양(Glider, LWSS) 하나를 여러 방향과 위치에 놓고, 판 끝에 닿은 뒤 어떤 결말로 끝나는지 센다. (Conway, 한도 2000)
 * 확장 2 문서 4-1절의 "이동은 알아보지 않는다"가 기대는 "끝에 닿으면 결말이 나온다"를 넓게 확인하려고 썼다.
 */
public class WallCheck {
    public static void main(String[] a) {
        int[] glider = {1,0, 2,1, 0,2, 1,2, 2,2};
        int[] lwss = {1,0, 4,0, 0,1, 0,2, 4,2, 0,3, 1,3, 2,3, 3,3};
        Map<String, Integer> tally = new TreeMap<>();
        List<String> notStill = new ArrayList<>();
        for (String name : new String[]{"Glider", "LWSS"}) {
            int[] shape = name.equals("Glider") ? glider : lwss;
            // d의 세 비트: 좌우 뒤집기, 상하 뒤집기, x와 y 바꾸기 → 8방향
            for (int d = 0; d < 8; d++)
                for (int ox = 5; ox <= 55; ox += 5)
                    for (int oy = 5; oy <= 55; oy += 10) {
                        Set<Point> start = place(shape, ox, oy, (d & 1) != 0, (d & 2) != 0, (d & 4) != 0);
                        Ending e = new EndingPredictor(2000).predict(new Generation(start, 64), new ConwayRule());
                        String key = name + " " + e.kind() + (e.kind() == Ending.Kind.REPEATING ? " 주기 " + e.period() : "");
                        tally.merge(key, 1, Integer::sum);
                        if (e.kind() != Ending.Kind.STILL)
                            notStill.add(name + " 방향 " + d + " (" + ox + "," + oy + ") → " + e.kind() + ", " + e.after() + "세대 뒤");
                    }
        }
        tally.forEach((k, v) -> System.out.println(k + " : " + v + "가지"));
        notStill.forEach(System.out::println);
    }

    static Set<Point> place(int[] xy, int ox, int oy, boolean flipX, boolean flipY, boolean swap) {
        Set<Point> s = new HashSet<>();
        for (int i = 0; i < xy.length; i += 2) {
            int x = xy[i], y = xy[i + 1];
            if (swap) { int t = x; x = y; y = t; }
            if (flipX) x = 4 - x;
            if (flipY) y = 4 - y;
            s.add(new Point(ox + x, oy + y));
        }
        return s;
    }
}
