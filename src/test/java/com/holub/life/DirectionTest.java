package com.holub.life;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 첫 번째 테스트: 빌드/테스트 환경이 동작하는지 확인하기 위한 가장 단순한 모델 클래스 테스트.
 * Direction은 UI와 무관한 순수 Java 클래스라 JUnit으로 바로 검증할 수 있다.
 */
class DirectionTest {

    @Test
    void 새로_만든_Direction은_아무_방향도_갖지_않는다() {
        Direction d = new Direction();
        assertTrue(d.equals(Direction.NONE));
        assertFalse(d.the(Direction.NORTH));
    }

    @Test
    void add한_방향만_the로_참이_된다() {
        Direction d = new Direction();
        d.add(Direction.NORTH);

        assertTrue(d.the(Direction.NORTH));
        assertFalse(d.the(Direction.SOUTH));
    }

    @Test
    void 여러_방향을_add하면_모두_포함된다() {
        Direction d = new Direction();
        d.add(Direction.NORTH);
        d.add(Direction.WEST);

        assertTrue(d.the(Direction.NORTH));
        assertTrue(d.the(Direction.WEST));
        assertFalse(d.the(Direction.NORTHWEST));  // 모서리(NORTHWEST)는 N+W와 다른 별개의 비트
    }

    @Test
    void clear하면_NONE으로_돌아간다() {
        Direction d = new Direction(Direction.ALL);
        d.clear();
        assertTrue(d.equals(Direction.NONE));
    }

    @Test
    void 상수_Direction은_수정할_수_없다() {
        assertThrows(UnsupportedOperationException.class,
                     () -> Direction.NORTH.add(Direction.SOUTH));
        assertThrows(UnsupportedOperationException.class,
                     () -> Direction.NONE.clear());
    }
}
