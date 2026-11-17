import com.holub.life.*;
import java.awt.Point;
import java.util.*;
public class PredictTime {
    public static void main(String[] a) {
        Map<String, Set<Point>> boards = new LinkedHashMap<>();
        boards.put("Blinker", Set.of(new Point(10,10), new Point(11,10), new Point(12,10)));
        boards.put("Glider", Set.of(new Point(1,0), new Point(2,1), new Point(0,2), new Point(1,2), new Point(2,2)));
        Set<Point> rnd = new HashSet<>(); Random r = new Random(1);
        for (int y=0;y<64;y++) for (int x=0;x<64;x++) if (r.nextInt(100)<30) rnd.add(new Point(x,y));
        boards.put("무작위 30%", rnd);
        EndingPredictor p = new EndingPredictor(500);
        for (var e : boards.entrySet()) {
            Generation g = new Generation(e.getValue(), 64);
            for (int i=0;i<3;i++) p.predict(g, new ConwayRule());   // 데우기
            long[] t = new long[5]; Ending end = null;
            for (int i=0;i<5;i++) { long s=System.nanoTime(); end=p.predict(g,new ConwayRule()); t[i]=(System.nanoTime()-s)/1_000_000; }
            Arrays.sort(t);
            System.out.println(e.getKey()+": "+end.kind()+" "+end.after()+"세대 뒤, 가운데 값 "+t[2]+"ms");
        }
    }
}
