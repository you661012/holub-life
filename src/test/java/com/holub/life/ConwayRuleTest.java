package com.holub.life;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConwayRuleTest {

    private final Rule rule = new ConwayRule();   // 테스트 대상

    @Test
    void 살아있는_셀은_이웃이_2개면_산다() {
        assertTrue(rule.nextState(true, 2));
    }

    @Test
    void 죽은_셀은_이웃이_3개면_탄생한다() {
        assertTrue(rule.nextState(false, 3));
    }
    @Test
    void 살아있는_셀은_이웃이_3개면_산다() {
        assertTrue(rule.nextState(true, 3));
    }

    @Test
    void 살아있는_셀은_이웃이_0개나_1개면_죽는다() {
        assertFalse(rule.nextState(true, 0));
        assertFalse(rule.nextState(true, 1));
    }

    @Test
    void 살아있는_셀은_이웃이_4개_이상이면_죽는다() {
        for (int n = 4; n <= 8; n++) {
            assertFalse(rule.nextState(true, n), "이웃 " + n + "개");
        }
    }

    @Test
    void 죽은_셀은_이웃이_3개가_아니면_탄생하지_않는다() {
        for (int n = 0; n <= 8; n++) {
            if (n == 3) continue;                       // 3개일 때만 탄생
            assertFalse(rule.nextState(false, n), "이웃 " + n + "개");
        }
    }
}