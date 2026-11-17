import com.holub.life.*;
import com.holub.ui.MenuSite;
import javax.swing.*;
import java.awt.*;

/** Life.java가 하는 것처럼 창과 Universe를 만들고(화면에 띄우지는 않는다), 시계를 손으로 네 번 넘기며 화면 쪽 코드가 무엇을 하는지 센다. */
public class Screen {
    public static void main(String[] a) throws Exception {
        JFrame frame = new JFrame("check");
        MenuSite.establish(frame);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(Universe.instance(), BorderLayout.CENTER);
        frame.pack();                                   // 화면에 띄우지 않고 그릴 준비만 한다
        SwingUtilities.invokeAndWait(() -> {});
        System.out.println("Universe.getGraphics() = " + (Universe.instance().getGraphics() == null ? "null (그릴 수 없음)" : "있음"));
        for (int gen = 1; gen <= 4; gen++) {
            int r0 = Universe.refreshRuns, a0 = Resident.drawnAlive, d0 = Resident.drawnDead;
            Clock.instance().tick();                    // Go 메뉴의 Tick과 같은 호출
            SwingUtilities.invokeAndWait(() -> {});     // 화면 쪽 스레드가 밀린 일을 끝낼 때까지 기다린다
            System.out.println(gen + "세대: refreshNow 실행 " + (Universe.refreshRuns - r0) + "번, 그려진 칸: 살아 있는 칸 "
                    + (Resident.drawnAlive - a0) + " / 죽은 칸 " + (Resident.drawnDead - d0));
        }
        System.exit(0);
    }
}
