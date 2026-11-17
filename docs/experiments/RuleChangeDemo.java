import com.holub.life.*;
import java.awt.Point;
import java.util.*;

/** 저장소 밖 실험. 규칙이 바뀐 것을 화면 한 줄(예측)에 알리는 두 방법. */
public class RuleChangeDemo {
    static final EndingPredictor predictor = new EndingPredictor(500);
    static final Generation board = twoRows();      // 가로 3칸 두 줄

    static Generation twoRows() {
        Set<Point> c = new HashSet<>();
        for (int x = 10; x <= 12; x++) { c.add(new Point(x, 10)); c.add(new Point(x, 12)); }
        return new Generation(c, 64);
    }

    static String text(Rule rule) {
        Ending e = predictor.predict(board, rule);
        String what = e.kind() == Ending.Kind.EXTINCT ? e.after() + "세대 뒤 소멸"
                    : e.kind() == Ending.Kind.STILL   ? e.after() + "세대 뒤 정지"
                    : e.kind() == Ending.Kind.REPEATING ? e.after() + "세대 뒤부터 반복(주기 " + e.period() + ")"
                    : "미확정";
        return rule.getClass().getSimpleName() + " | 예측: " + what;
    }

    // ───── 방법 1: 규칙을 바꾸는 자리마다 "한 줄을 다시 써라"를 직접 부른다 ─────
    static class Direct {
        final RuleHolder holder = new RuleHolder();
        String statusLine = text(holder.current());

        void updateStatusLine() { statusLine = text(holder.current()); }

        void menuConway()   { holder.change(new ConwayRule());   updateStatusLine(); }
        void menuHighLife() { holder.change(new HighLifeRule()); updateStatusLine(); }
        void menuCustom(String notation) {
            holder.change(new BirthSurvivalRule(notation));
            // updateStatusLine();   ← 메뉴를 나중에 추가하면서 이 한 줄을 빠뜨렸다고 하자
        }
    }

    // ───── 방법 2: RuleHolder가 "규칙이 바뀌었다"를 구독자에게 알린다 (Observer) ─────
    interface RuleListener { void ruleChanged(Rule newRule); }

    static class NotifyingRuleHolder extends RuleHolder {
        private final List<RuleListener> listeners = new ArrayList<>();
        void addListener(RuleListener l) { listeners.add(l); }
        @Override public void change(Rule rule) {
            super.change(rule);
            for (RuleListener l : listeners) l.ruleChanged(rule);
        }
    }

    static class WithObserver {
        final NotifyingRuleHolder holder = new NotifyingRuleHolder();
        String statusLine = text(holder.current());

        WithObserver() { holder.addListener(newRule -> statusLine = text(newRule)); }   // 구독은 한 번만

        void menuConway()   { holder.change(new ConwayRule()); }
        void menuHighLife() { holder.change(new HighLifeRule()); }
        void menuCustom(String notation) { holder.change(new BirthSurvivalRule(notation)); }
    }

    // ───── 방법 3: 규칙을 바꾸는 일과 다시 쓰는 일을 메서드 하나에 모은다 ─────
    static class OneMethod {
        private final RuleHolder holder = new RuleHolder();
        String statusLine = text(holder.current());

        private void changeRule(Rule rule) {      // 규칙을 바꾸는 길은 이 메서드 하나뿐이다
            holder.change(rule);
            statusLine = text(holder.current());
        }

        void menuConway()   { changeRule(new ConwayRule()); }
        void menuHighLife() { changeRule(new HighLifeRule()); }
        void menuCustom(String notation) { changeRule(new BirthSurvivalRule(notation)); }   // 나중에 추가한 메뉴도 changeRule만 부른다
    }

    static void show(String action, RuleHolder holder, String statusLine) {
        boolean right = statusLine.equals(text(holder.current()));
        System.out.printf("  %-22s 화면 한 줄: %-48s %s%n", action, statusLine, right ? "" : "← 틀림 (실제 규칙은 " + holder.current().getClass().getSimpleName() + ")");
    }

    public static void main(String[] a) {
        System.out.println("[방법 1] 바꾸는 자리마다 직접 부른다");
        Direct d = new Direct();
        show("처음", d.holder, d.statusLine);
        d.menuHighLife();     show("HighLife 메뉴", d.holder, d.statusLine);
        d.menuCustom("B3/S23"); show("직접 입력 B3/S23 메뉴", d.holder, d.statusLine);

        System.out.println("[방법 2] RuleHolder가 알린다 (Observer)");
        WithObserver o = new WithObserver();
        show("처음", o.holder, o.statusLine);
        o.menuHighLife();     show("HighLife 메뉴", o.holder, o.statusLine);
        o.menuCustom("B3/S23"); show("직접 입력 B3/S23 메뉴", o.holder, o.statusLine);
        System.out.println("[방법 3] 메서드 하나에 모은다");
        OneMethod m = new OneMethod();
        show("처음", m.holder, m.statusLine);
        m.menuHighLife();       show("HighLife 메뉴", m.holder, m.statusLine);
        m.menuCustom("B3/S23"); show("직접 입력 B3/S23 메뉴", m.holder, m.statusLine);
    }
}
