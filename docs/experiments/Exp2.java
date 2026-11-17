import com.holub.life.*;
import com.holub.asynch.ConditionVariable;
import java.lang.reflect.Field;
import java.util.*;

public class Exp2 {
    public static void main(String[] a) throws Exception {
        Field nesting = Neighborhood.class.getDeclaredField("nestingLevel");
        nesting.setAccessible(true);
        Field rp = Neighborhood.class.getDeclaredField("readingPermitted");
        rp.setAccessible(true);
        ConditionVariable reading = (ConditionVariable) rp.get(null);
        int stuck = 0, trials = 20; List<Integer> ends = new ArrayList<>();
        for (int trial = 0; trial < trials; trial++) {
            nesting.setInt(null, -1); reading.set(true);
            Cell screen = Exp.newBoard(new RuleHolder(), Exp.random(1));
            Cell ahead  = Exp.newBoard(new RuleHolder(), Exp.random(1));
            Thread t1 = new Thread(() -> { for (int i = 0; i < 300; i++) Exp.step(screen); });
            Thread t2 = new Thread(() -> { for (int i = 0; i < 300; i++) Exp.step(ahead); });
            t1.start(); t2.start(); t1.join(); t2.join();
            ends.add(nesting.getInt(null));
            if (!reading.isTrue()) stuck++;
        }
        System.out.println("끝난 뒤 nestingLevel 값들: " + ends);
        System.out.println(trials + "번 중 " + stuck + "번, 끝난 뒤 readingPermitted가 false로 남았다 (false면 redraw가 그리지 않고 돌아간다)");
    }
}
