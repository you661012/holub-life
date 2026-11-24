// 세대마다 Rule.nextState()가 불린 횟수(= 계산한 칸 수)를 센다. run.sh가 건너뛰기를 되살린 사본에서 돌린다.
import com.holub.life.*;
import java.awt.*;
public class SkipCount {
    static int calls = 0;
    public static void main(String[] a) {
        RuleHolder h = new RuleHolder();
        h.change(new Rule() { public boolean nextState(boolean alive, int n) { calls++; return n == 3 || (alive && n == 2); } });
        Cell board = new Neighborhood(8, new Neighborhood(8, new Resident(h)));
        Rectangle all = new Rectangle(0, 0, 512, 512);
        int[][] cells = a[0].equals("blinker") ? new int[][]{{10,10},{11,10},{12,10}}
                      : a[0].equals("blinker+ship") ? new int[][]{{10,10},{11,10},{12,10}, {50,50},{51,50},{50,51},{52,51},{51,52},{52,52}}
                      : new int[][]{{50,50},{51,50},{50,51},{52,51},{51,52},{52,52}};
        for (int[] c : cells) board.userClicked(new Point(c[0]*8+1, c[1]*8+1), all);
        StringBuilder sb = new StringBuilder();
        for (int g = 1; g <= 8; g++) {
            calls = 0;
            Cell D = Cell.DUMMY;
            if (board.figureNextState(D, D, D, D, D, D, D, D)) board.transition();
            sb.append(calls).append(g < 8 ? ", " : "");
        }
        System.out.println(a[0] + " → 세대별 계산 칸 수: " + sb);
    }
}
