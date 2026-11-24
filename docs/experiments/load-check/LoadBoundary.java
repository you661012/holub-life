import com.holub.life.*;
import java.awt.Point;

// Load와 같은 경로(transfer(…, Cell.LOAD))로 가로 3칸을 놓고 세대마다 세포 수를 센다 (11/24).
// 구역 경계에 걸친 (7,7)~(9,7)과 구역 안쪽 (10,10)~(12,10)을 비교한다. 가로 3칸은 언제나 3칸이어야 한다.
public class LoadBoundary {
    public static void main(String[] args) {
        for (int base : new int[]{7, 10}) {
            Cell board = new Neighborhood(8, new Neighborhood(8, new Resident()));
            LiveCellCollector saved = new LiveCellCollector();     // 저장된 판 대신 쓴다
            for (int dx = 0; dx < 3; dx++) saved.markAsAlive(new Point(base + dx, base));
            board.transfer(saved, new Point(0, 0), Cell.LOAD);

            StringBuilder line = new StringBuilder("(" + base + "," + base + ")에서 시작: ");
            for (int g = 0; g < 4; g++) {
                LiveCellCollector now = new LiveCellCollector();
                board.transfer(now, new Point(0, 0), Cell.STORE);
                line.append(now.cells().size()).append(' ');
                if (board.figureNextState(Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY,
                                          Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY))
                    board.transition();
            }
            System.out.println(line);
        }
    }
}
