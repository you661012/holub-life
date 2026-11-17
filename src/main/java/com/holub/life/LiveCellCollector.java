package com.holub.life;

import java.awt.Point;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 판에서 살아 있는 칸의 좌표를 모은다.
 * 원본의 Store 기능이 쓰는 Cell.Memento 인터페이스를 구현한다. 판에 transfer(…, Cell.STORE)를 부르면
 * 판이 살아 있는 Resident마다 markAsAlive를 불러 준다. 판을 읽기만 하고 되돌려 넣는 데는 쓰지 않는다.
 */
public class LiveCellCollector implements Cell.Memento {

    private final Set<Point> cells = new HashSet<>();

    /** 모은 좌표들. */
    public Set<Point> cells() {
        return cells;
    }

    @Override
    public void markAsAlive(Point location) {
        cells.add(new Point(location));   // 복사해서 담는다. Neighborhood가 Point 객체 하나를 옮겨 가며 다시 쓰기 때문이다
    }

    @Override
    public boolean isAlive(Point location) {
        return cells.contains(location);
    }

    // 파일 저장·읽기용 메서드. 파일로 저장하는 것은 원본의 NeighborhoodState가 맡는다
    @Override
    public void load(InputStream in) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flush(OutputStream out) {
        throw new UnsupportedOperationException();
    }
}
