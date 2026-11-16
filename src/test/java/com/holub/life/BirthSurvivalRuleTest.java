package com.holub.life;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BirthSurvivalRuleTest {

    @Test
    void B3_S23은_ConwayRule과_판정이_같다() {
        Rule mine   = new BirthSurvivalRule("B3/S23");   // 테스트 대상
        Rule conway = new ConwayRule();                  // 기준

        for (int n = 0; n <= 8; n++) {
            assertEquals(conway.nextState(true, n),  mine.nextState(true, n),  "살아 있음, 이웃 " + n + "개");
            assertEquals(conway.nextState(false, n), mine.nextState(false, n), "죽어 있음, 이웃 " + n + "개");
        }
    }

    @Test
    void B36_S23은_HighLifeRule과_판정이_같다() {
        Rule mine     = new BirthSurvivalRule("B36/S23");   // 테스트 대상
        Rule highLife = new HighLifeRule();                 // 기준

        for (int n = 0; n <= 8; n++) {
            assertEquals(highLife.nextState(true, n),  mine.nextState(true, n),  "살아 있음, 이웃 " + n + "개");
            assertEquals(highLife.nextState(false, n), mine.nextState(false, n), "죽어 있음, 이웃 " + n + "개");
        }
    }

    // ── Conway도 HighLife도 아닌 규칙: Seeds (B2/S). 이웃이 2개면 태어나고, 살아 있는 세포는 항상 죽는다 ──

    @Test
    void B2_S는_죽은_세포의_이웃이_2개일_때만_태어난다() {
        Rule seeds = new BirthSurvivalRule("B2/S");

        for (int n = 0; n <= 8; n++) {
            assertEquals(n == 2, seeds.nextState(false, n), "죽어 있음, 이웃 " + n + "개");
        }
    }

    @Test
    void B2_S는_살아_있는_세포가_이웃_수와_상관없이_죽는다() {
        Rule seeds = new BirthSurvivalRule("B2/S");      // S 뒤에 숫자가 없다 = 생존하는 경우가 없다

        for (int n = 0; n <= 8; n++) {
            assertFalse(seeds.nextState(true, n), "살아 있음, 이웃 " + n + "개");
        }
    }

    // ── 잘못된 입력: 규칙 객체를 만드는 순간에 거부한다 ──

    @Test
    void B와_S가_없는_글자는_거부한다() {
        assertThrows(IllegalArgumentException.class, () -> new BirthSurvivalRule("abc"));
        assertThrows(IllegalArgumentException.class, () -> new BirthSurvivalRule(""));
        assertThrows(IllegalArgumentException.class, () -> new BirthSurvivalRule("3/23"));
    }

    @Test
    void 슬래시나_S가_빠진_표기는_거부한다() {
        assertThrows(IllegalArgumentException.class, () -> new BirthSurvivalRule("B3"));
        assertThrows(IllegalArgumentException.class, () -> new BirthSurvivalRule("B3S23"));
        assertThrows(IllegalArgumentException.class, () -> new BirthSurvivalRule("B3/23"));
    }

    @Test
    void 이웃_수가_될_수_없는_9는_거부한다() {
        assertThrows(IllegalArgumentException.class, () -> new BirthSurvivalRule("B9/S23"));
        assertThrows(IllegalArgumentException.class, () -> new BirthSurvivalRule("B3/S29"));
    }

    @Test
    void 입력이_없으면_거부한다() {
        assertThrows(IllegalArgumentException.class, () -> new BirthSurvivalRule(null));
    }
}
