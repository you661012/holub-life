package com.holub.life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Resident.transition()이 돌려주는 값을 확인한다.
 * Cell 인터페이스의 주석은 transition()이 "상태가 바뀌었으면 true"를 돌려준다고 적고 있다.
 * (ResidentTest는 transition() 뒤에 세포가 살아 있는지만 보고, 돌려주는 값은 보지 않는다.)
 */
class ResidentTransitionTest {

    @Test
    void 죽은_세포가_태어나면_transition은_true를_돌려준다() {
        assertTrue(transitionResult(false, 3));     // 죽어 있고 이웃 3개 → 태어난다 (Conway)
    }

    @Test
    void 살아_있는_세포가_죽으면_transition은_true를_돌려준다() {
        assertTrue(transitionResult(true, 0));      // 살아 있고 이웃 0개 → 죽는다
    }

    @Test
    void 살아_있는_세포가_그대로_살아_있으면_transition은_false를_돌려준다() {
        assertFalse(transitionResult(true, 2));     // 살아 있고 이웃 2개 → 그대로 산다
    }

    @Test
    void 죽은_세포가_그대로_죽어_있으면_transition은_false를_돌려준다() {
        assertFalse(transitionResult(false, 0));    // 죽어 있고 이웃 0개 → 그대로 죽어 있다
    }

    /** 살아 있는 이웃이 liveNeighbors개일 때 한 세대를 넘기고, 그때 transition()이 돌려준 값을 돌려준다. (규칙은 Conway) */
    private boolean transitionResult(boolean startsAlive, int liveNeighbors) {
        Resident target = new Resident();
        if (startsAlive) target.userClicked(null, null);

        Cell[] n = new Cell[8];
        for (int i = 0; i < 8; i++) {
            if (i < liveNeighbors) {
                Resident r = new Resident();
                r.userClicked(null, null);
                n[i] = r;
            } else {
                n[i] = Cell.DUMMY;
            }
        }

        target.figureNextState(n[0], n[1], n[2], n[3], n[4], n[5], n[6], n[7]);
        return target.transition();
    }
}
