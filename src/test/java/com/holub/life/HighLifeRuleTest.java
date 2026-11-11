package com.holub.life;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HighLifeRuleTest {

    private final Rule rule = new HighLifeRule();   // 테스트 대상

    // ── Conway와 같은 부분 (S23, B3) ─────────────────────────

    @Test
    void 살아있는_셀은_이웃이_2개나_3개면_산다() {
        assertTrue(rule.nextState(true, 2));
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
    void 죽은_셀은_이웃이_3개면_탄생한다() {
        assertTrue(rule.nextState(false, 3));
    }

    // ── Conway와 다른 부분 (B36의 6) ─────────────────────────

    @Test
    void 죽은_셀은_이웃이_6개면_탄생한다() {
        assertTrue(rule.nextState(false, 6));
    }

    @Test
    void 살아있는_셀은_이웃이_6개여도_죽는다() {
        assertFalse(rule.nextState(true, 6));           // 6은 탄생 조건일 뿐 생존 조건이 아니다
    }

    @Test
    void 죽은_셀은_이웃이_3개나_6개가_아니면_탄생하지_않는다() {
        for (int n = 0; n <= 8; n++) {
            if (n == 3 || n == 6) continue;
            assertFalse(rule.nextState(false, n), "이웃 " + n + "개");
        }
    }

    // ── 두 규칙이 실제로 다른 규칙임을 보이는 테스트 ─────────

    @Test
    void Conway와_HighLife는_죽은_셀_이웃_6개에서만_결과가_다르다() {
        Rule conway = new ConwayRule();
        for (int n = 0; n <= 8; n++) {
            assertEquals(conway.nextState(true, n), rule.nextState(true, n), "살아 있음, 이웃 " + n + "개");
            if (n == 6) {
                assertNotEquals(conway.nextState(false, n), rule.nextState(false, n), "죽어 있음, 이웃 6개");
            } else {
                assertEquals(conway.nextState(false, n), rule.nextState(false, n), "죽어 있음, 이웃 " + n + "개");
            }
        }
    }
}
