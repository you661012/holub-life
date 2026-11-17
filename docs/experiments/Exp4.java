import com.holub.life.*;
import java.awt.Point;
import java.util.*;

/** 예측기 테스트의 기대값을 실제 Neighborhood 판(방법 가)에서 뽑는다. */
public class Exp4 {
    static void show(String name, Set<Point> start, Rule rule) {
        RuleHolder holder = new RuleHolder();
        holder.change(rule);
        System.out.println(name + " → " + Exp.predict(Exp.methodA(start, holder)));
    }
    public static void main(String[] a) {
        Rule conway = new ConwayRule(), highLife = new HighLifeRule();
        show("빈 판 / Conway", Exp.pts(), conway);
        show("한 칸 / Conway", Exp.pts(10,10), conway);
        show("Block / Conway", Exp.pts(10,10, 11,10, 10,11, 11,11), conway);
        show("Blinker / Conway", Exp.pts(10,10, 11,10, 12,10), conway);
        show("ㄱ자 3칸 / Conway", Exp.pts(10,10, 11,10, 10,11), conway);
        show("가로 4칸 / Conway", Exp.pts(10,10, 11,10, 12,10, 13,10), conway);
        show("가로 5칸 / Conway", Exp.pts(10,10, 11,10, 12,10, 13,10, 14,10), conway);
        show("가로 3칸 두 줄 / Conway", Exp.pts(10,10, 11,10, 12,10, 10,12, 11,12, 12,12), conway);
        show("가로 3칸 두 줄 / HighLife", Exp.pts(10,10, 11,10, 12,10, 10,12, 11,12, 12,12), highLife);
        show("Glider / Conway", Exp.pts(1,0, 2,1, 0,2, 1,2, 2,2), conway);
        show("Blinker / Seeds B2/S", Exp.pts(10,10, 11,10, 12,10), new BirthSurvivalRule("B2/S"));
    }
}
