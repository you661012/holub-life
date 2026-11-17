import com.holub.life.*;
import com.holub.ui.MenuSite;
import javax.swing.*; import java.awt.*;
/** 가장 긴 글자를 넣었을 때 두 줄이 창 너비 안에 들어가는지 잰다. */
public class Fit { public static void main(String[] a) throws Exception {
  SwingUtilities.invokeAndWait(() -> {
    JFrame f = new JFrame(); MenuSite.establish(f);
    f.getContentPane().setLayout(new BorderLayout());
    f.getContentPane().add(Universe.instance(), BorderLayout.CENTER);
    JComponent bar = Universe.instance().statusBar();
    f.getContentPane().add(bar, BorderLayout.SOUTH); f.pack();
    JLabel top = (JLabel) bar.getComponent(0), bottom = (JLabel) bar.getComponent(1);
    System.out.println("글자 칸의 너비 " + top.getWidth() + "px, 아랫줄 글자 크기 " + bottom.getFont().getSize() + "pt, 두 줄의 높이 " + bar.getHeight() + "px");
    String[] tops = {"Generation 123456 | Cells 4096 | Rule B012345678/S012345678"};
    String[] bottoms = {"Ending: repeats (period 123) in 499 generations", "Ending: undecided within 500 generations", "Ending: still in 244 generations"};
    for (String s : tops) System.out.println("윗줄 " + top.getFontMetrics(top.getFont()).stringWidth(s) + "px  " + s);
    for (String s : bottoms) System.out.println("아랫줄 " + bottom.getFontMetrics(bottom.getFont()).stringWidth(s) + "px  " + s);
  });
  System.exit(0);
}}
