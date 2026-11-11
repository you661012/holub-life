package com.holub.life;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RuleHolderTest {

    @Test
    void 처음에는_Conway_규칙을_보관한다() {
        RuleHolder holder = new RuleHolder();

        assertFalse(holder.current().nextState(false, 6));   // Conway: 죽은 셀 + 이웃 6 → 그대로
    }

    @Test
    void 규칙을_바꾸면_바꾼_규칙을_돌려준다() {
        RuleHolder holder = new RuleHolder();

        holder.change(new HighLifeRule());

        assertTrue(holder.current().nextState(false, 6));    // HighLife: 죽은 셀 + 이웃 6 → 탄생
    }

    @Test
    void 서로_다른_RuleHolder는_서로_영향을_주지_않는다() {
        RuleHolder first  = new RuleHolder();
        RuleHolder second = new RuleHolder();

        first.change(new HighLifeRule());

        assertTrue (first .current().nextState(false, 6));   // 바꾼 쪽은 HighLife
        assertFalse(second.current().nextState(false, 6));   // 다른 쪽은 Conway 그대로
    }
}