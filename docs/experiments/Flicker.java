import com.holub.life.*;
import com.holub.ui.MenuSite;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** 창을 키운 상태에서 클릭 한 번에 판(Universe)의 크기가 몇 번 바뀌는지 센다. */
public class Flicker {
    static int boundsChanges, invalidated;
    static JFrame frame;

    public static void main(String[] a) throws Exception {
        boolean withLine = a.length == 0;
        SwingUtilities.invokeAndWait(() -> {
            frame = new JFrame();
            MenuSite.establish(frame);
            frame.getContentPane().setLayout(new BorderLayout());
            frame.getContentPane().add(Universe.instance(), BorderLayout.CENTER);
            if (withLine) frame.getContentPane().add(Universe.instance().statusBar(), BorderLayout.SOUTH);
            frame.pack();
            Universe.instance().addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) { boundsChanges++; }
            });
        });
        measure("창이 기본 크기일 때");
        SwingUtilities.invokeAndWait(() -> { frame.setSize(1400, 900); frame.validate(); });
        Thread.sleep(500);
        measure("창을 1400x900으로 키웠을 때");
        System.exit(0);
    }

    static void measure(String what) throws Exception {
        Thread.sleep(3500);
        boundsChanges = 0; invalidated = 0;
        for (int i = 0; i < 5; i++) {
            final int x = 20 + i * 10;
            SwingUtilities.invokeAndWait(() -> {
                Universe u = Universe.instance();
                u.dispatchEvent(new MouseEvent(u, MouseEvent.MOUSE_PRESSED, 0, 0, x, 20, 1, false, MouseEvent.BUTTON1));
            });
            Thread.sleep(1500);
            // 화면에 보이는 창이면 Swing이 스스로 하는 일: 배치가 무효가 됐으면 다시 배치한다
            SwingUtilities.invokeAndWait(() -> {
                if (!frame.getContentPane().isValid()) invalidated++;
                frame.validate();
            });
            Thread.sleep(300);
        }
        System.out.println(what + ": 클릭 5번에 판의 크기가 바뀐 횟수 " + boundsChanges
                + ", 배치가 무효가 된 횟수 " + invalidated + " (판 크기 " + Universe.instance().getWidth() + "x" + Universe.instance().getHeight() + ")");
    }
}
