package com.holub.life;                                  // DirectionTest와 같은 패키지

import org.junit.jupiter.api.Test;                       // @Test 쓰려고
import static org.junit.jupiter.api.Assertions.*;       // assertTrue 쓰려고

class ResidentTest {

    @Test                                                // "이 메서드는 테스트다" 표시
    void 죽은_셀은_살아있는_이웃이_3개면_살아난다() {

        // ── 준비: 등장인물 만들기 ──────────────────────────
        Resident target = new Resident();                // 검사할 칸. 처음엔 죽어 있음

        Resident alive1 = new Resident();                // 이웃 3개를 만들고
        Resident alive2 = new Resident();
        Resident alive3 = new Resident();
        alive1.userClicked(null, null);                  // 클릭하면 죽음↔삶이 뒤집힘 → 셋 다 살아남
        alive2.userClicked(null, null);
        alive3.userClicked(null, null);

        Cell dead = Cell.DUMMY;                          // 영원히 죽어있는 칸. 나머지 5방향용

        // ── 실행: 다음 세대로 ──────────────────────────────
        target.figureNextState(                          // "주위 8칸이 이렇다. 다음 상태를 계산해"
                alive1,  // north
                alive2,  // south
                alive3,  // east
                dead,    // west
                dead,    // northeast
                dead,    // northwest
                dead,    // southeast
                dead );  // southwest
        target.transition();                             // "계산한 상태로 실제로 바뀌어"

        // ── 확인 ──────────────────────────────────────────
        assertTrue(target.isAlive());                    // 살아났어야 한다. 아니면 FAILED
    }

    @Test
    void 살아있는_셀은_이웃이_4개면_죽는다() {
        // ── 준비: 등장인물 만들기 ──────────────────────────
        Resident target = new Resident();
        target.userClicked(null, null);     // 검사할 칸. 살아있는 상태로 시작

        Resident alive1 = new Resident();                // 이웃 4개를 만들고
        Resident alive2 = new Resident();
        Resident alive3 = new Resident();
        Resident alive4 = new Resident();
        alive1.userClicked(null, null);     // 클릭하면 죽음 ↔ 삶이 뒤집힘 → 넷 다 살아남
        alive2.userClicked(null, null);
        alive3.userClicked(null, null);
        alive4.userClicked(null, null);

        Cell dead = Cell.DUMMY;                          // 영원히 죽어있는 칸. 나머지 5방향용

        // ── 실행: 다음 세대로 ──────────────────────────────
        target.figureNextState(                          // "주위 8칸이 이렇다. 다음 상태를 계산해"
                alive1,  // north
                alive2,  // south
                alive3,  // east
                alive4,  // west
                dead,    // northeast
                dead,    // northwest
                dead,    // southeast
                dead );  // southwest
        target.transition();                             // "계산한 상태로 실제로 바뀌어"

        // ── 확인 ──────────────────────────────────────────
        assertFalse(target.isAlive());                    // 죽어야 함
    }
}