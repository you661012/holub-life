package com.holub.life;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Resident가 전달받은 RuleHolder의 규칙을 실제로 사용하는지 확인한다. */
class ResidentRuleTest {

    @Test
    void HighLife를_보관한_RuleHolder를_받은_Resident는_이웃_6개에서_탄생한다() {
        RuleHolder holder = new RuleHolder();
        holder.change(new HighLifeRule());
        Resident target = new Resident(holder);          // 죽은 상태로 시작

        runOneGeneration(target, 6);

        assertTrue(target.isAlive());
    }

    @Test
    void RuleHolder의_규칙을_바꾸면_이미_만들어진_Resident도_새_규칙을_따른다() {
        RuleHolder holder = new RuleHolder();            // Conway
        Resident target = new Resident(holder);

        runOneGeneration(target, 6);
        assertFalse(target.isAlive());                   // Conway: 이웃 6개여도 그대로 죽어 있음

        holder.change(new HighLifeRule());               // 실행 중에 규칙 교체

        runOneGeneration(target, 6);
        assertTrue(target.isAlive());                    // 같은 Resident가 이제 HighLife를 따름
    }

    @Test
    void create로_복제한_Resident는_같은_RuleHolder를_공유한다() {
        RuleHolder holder = new RuleHolder();
        Resident prototype = new Resident(holder);
        Resident copy = (Resident) prototype.create();   // Neighborhood가 하는 것과 같은 복제

        holder.change(new HighLifeRule());               // 프로토타입에 준 RuleHolder를 바꾸면

        runOneGeneration(copy, 6);
        assertTrue(copy.isAlive());                      // 복제본도 따라온다
    }

    // ── 헬퍼: 산 이웃 liveNeighbors개를 주고 한 세대 진행한다 ──
    private void runOneGeneration(Resident target, int liveNeighbors) {
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
        target.transition();
    }
}