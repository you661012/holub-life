import com.holub.life.*;
import com.holub.ui.MenuSite;
import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;

/** 창을 화면에 띄우지 않고 Life가 하는 것처럼 만든 뒤, 클릭·세대 진행·메뉴를 흉내 내며 창 아래 한 줄을 읽는다. */
public class StatusCheck {
    static JLabel label;
    static JFrame frame;

    public static void main(String[] a) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame();
            MenuSite.establish(frame);
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(Universe.instance(), BorderLayout.CENTER);
            label = (JLabel) Universe.instance().statusLabel();
            frame.getContentPane().add(label, BorderLayout.SOUTH);
            frame.pack();
        });
        show("처음");

        int[][] glider = {{1,0},{2,1},{0,2},{1,2},{2,2}};
        for (int[] c : glider) click(c[0], c[1]);
        show("Glider 5칸을 클릭");

        for (int i = 0; i < 3; i++) Clock.instance().tick();
        show("3세대 진행");

        menu("Rule", "HighLife (B36/S23)");
        show("Rule > HighLife");

        for (int i = 0; i < 250; i++) Clock.instance().tick();
        show("250세대 더 진행");

        menu("Grid", "Clear");
        show("Grid > Clear");
        System.exit(0);
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
            JMenuBar bar = frame.getJMenuBar();
            for (int i = 0; i < bar.getMenuCount(); i++) {
                JMenu m = bar.getMenu(i);
                if (!m.getText().equals(menuName)) continue;
                for (int j = 0; j < m.getItemCount(); j++)
                    if (m.getItem(j) != null && m.getItem(j).getText().equals(item)) { m.getItem(j).doClick(0); return; }
            }
            throw new IllegalStateException("메뉴 없음: " + menuName + " > " + item);
        });
    }

    static void show(String what) throws Exception {
        final String[] first = new String[1];
        SwingUtilities.invokeAndWait(() -> first[0] = label.getText());
        Thread.sleep(3000);                                   // 작업자가 계산을 끝낼 시간
        final String[] later = new String[1];
        SwingUtilities.invokeAndWait(() -> later[0] = label.getText());
        System.out.println("[" + what + "]\n   바로: " + first[0] + "\n   3초 뒤: " + later[0]);
    }
}
