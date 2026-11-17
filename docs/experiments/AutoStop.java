import com.holub.life.*;
import com.holub.ui.MenuSite;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;

/**
 * 창을 화면에 띄우지 않고 실제 시계(30ms 간격)를 돌려, 소멸이나 정지에 도달하면 시계가 멈추는지 본다.
 * 시계를 켠 뒤 1초 뒤와 2초 뒤의 상태 표시를 읽는다. 두 번의 세대 수가 같으면 시계가 멈춘 것이다.
 */
public class AutoStop {
    static JComponent bar;
    static JFrame frame;

    public static void main(String[] a) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame();
            MenuSite.establish(frame);
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(Universe.instance(), BorderLayout.CENTER);
            bar = Universe.instance().statusBar();
            frame.getContentPane().add(bar, BorderLayout.SOUTH);
            frame.pack();
        });

        run("한 칸 (1세대 뒤 소멸)", new int[][]{{10, 10}});
        run("2x2 Block (처음부터 정지)", new int[][]{{10, 10}, {11, 10}, {10, 11}, {11, 11}});
        run("ㄱ자 3칸 (1세대 뒤 정지)", new int[][]{{10, 10}, {11, 10}, {10, 11}});
        run("가로 3칸 Blinker (반복)", new int[][]{{10, 10}, {11, 10}, {12, 10}});
        System.exit(0);
    }

    static void run(String name, int[][] cells) throws Exception {
        menu("Grid", "Clear");
        for (int[] c : cells) click(c[0], c[1]);
        Thread.sleep(1500);                                   // 예측이 끝날 시간
        SwingUtilities.invokeAndWait(() -> Clock.instance().startTicking(30));   // Go > Fast와 같다
        Thread.sleep(1000);
        String first = text();
        Thread.sleep(1000);
        String second = text();
        SwingUtilities.invokeAndWait(() -> Clock.instance().stop());
        System.out.println("[" + name + "]\n   1초 뒤: " + first + "\n   2초 뒤: " + second
                + "\n   → " + (first.equals(second) ? "시계가 멈췄다" : "시계가 계속 돈다"));
    }

    static String text() throws Exception {
        final String[] t = new String[1];
        SwingUtilities.invokeAndWait(() ->
                t[0] = ((JLabel) bar.getComponent(0)).getText() + " | " + ((JLabel) bar.getComponent(1)).getText());
        return t[0];
    }

    static void click(int x, int y) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Universe u = Universe.instance();
            int cell = u.getWidth() / 64;
            u.dispatchEvent(new MouseEvent(u, MouseEvent.MOUSE_PRESSED, 0, 0,
                    x * cell + 1, y * cell + 1, 1, false, MouseEvent.BUTTON1));
        });
    }

    static void menu(String menuName, String item) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JMenuBar mb = frame.getJMenuBar();
            for (int i = 0; i < mb.getMenuCount(); i++) {
                JMenu m = mb.getMenu(i);
                if (!m.getText().equals(menuName)) continue;
                for (int j = 0; j < m.getItemCount(); j++)
                    if (m.getItem(j) != null && m.getItem(j).getText().equals(item)) { m.getItem(j).doClick(0); return; }
            }
            throw new IllegalStateException("메뉴 없음: " + menuName + " > " + item);
        });
    }
}
