package com.holub.life;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

/**
 * 한 세대에 살아 있는 칸들의 좌표.
 * Rule을 받으면 다음 세대를 새 Generation으로 돌려준다. 화면이나 Neighborhood 판 없이 계산한다.
 * (Strategy 패턴에서 Rule을 불러 쓰는 쪽. Resident에 이어 두 번째다)
 */
public class Generation {

    private final Set<Point> liveCells;   // 살아 있는 칸들의 좌표. 만든 뒤에는 바뀌지 않는다
    private final int width;              // 판의 가로·세로 칸 수. 화면의 판은 64

    /**
     * @param liveCells 살아 있는 칸들의 좌표 (x, y 모두 0 ~ width-1)
     * @param width     판의 가로·세로 칸 수
     * @throws IllegalArgumentException 판 밖의 좌표가 들어 있을 때
     */
    public Generation(Set<Point> liveCells, int width) {
        Set<Point> copies = new HashSet<>();
        for (Point cell : liveCells) {
            if (cell.x < 0 || cell.x >= width || cell.y < 0 || cell.y >= width) {
                throw new IllegalArgumentException("판 밖의 좌표입니다: (" + cell.x + ", " + cell.y + ")");
            }
            // 집합만이 아니라 좌표 객체도 복사한다. Point는 x, y를 고칠 수 있는 객체라서,
            // 그대로 들고 있으면 넘겨준 쪽이 나중에 Point를 고칠 때 이 Generation도 바뀐다.
            copies.add(new Point(cell));
        }
        this.liveCells = copies;
        this.width = width;
    }

    /** 다음 세대를 돌려준다. 이 Generation은 바뀌지 않는다. */
    public Generation next(Rule rule) {
        Set<Point> nextCells = new HashSet<>();

        // 판의 모든 칸을 본다. 살아 있는 칸 주변만 보면 빠르지만, 이웃이 0개여도 태어나는 규칙(B0)에서 틀린다.
        // 실제 판도 세대마다 모든 Resident가 계산한다.
        for (int y = 0; y < width; y++) {            // 판 안의 칸만 본다. 판 밖에는 칸이 생기지 않는다
            for (int x = 0; x < width; x++) {
                Point cell = new Point(x, y);
                boolean alive = liveCells.contains(cell);

                if (rule.nextState(alive, liveNeighbors(x, y))) {
                    nextCells.add(cell);
                }
            }
        }
        return new Generation(nextCells, width);
    }

    /** (x, y)를 둘러싼 여덟 칸 중 살아 있는 칸의 수. 판 밖의 칸은 liveCells에 없으므로 세지 않는다. */
    private int liveNeighbors(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                boolean isMyself = (dx == 0 && dy == 0);
                if (!isMyself && liveCells.contains(new Point(x + dx, y + dy))) {
                    count++;
                }
            }
        }
        return count;
    }

    /** 살아 있는 칸들의 좌표. 복사본을 돌려주므로 받은 쪽이 고쳐도 이 Generation은 바뀌지 않는다. */
    public Set<Point> liveCells() {
        Set<Point> copies = new HashSet<>();
        for (Point cell : liveCells) {
            copies.add(new Point(cell));
        }
        return copies;
    }

    /** 살아 있는 칸의 수. */
    public int liveCount() {
        return liveCells.size();
    }

    /** 살아 있는 칸이 하나도 없는가. */
    public boolean isEmpty() {
        return liveCells.isEmpty();
    }

    /** 판 크기와 살아 있는 칸들이 같으면 같은 Generation이다. */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Generation)) {
            return false;
        }
        Generation that = (Generation) other;
        return width == that.width && liveCells.equals(that.liveCells);
    }

    @Override
    public int hashCode() {
        return liveCells.hashCode() + width;
    }
}
