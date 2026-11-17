package com.holub.life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** 창 아래 한 줄에 띄울 규칙 이름. 규칙 객체마다 toString()이 B/S 표기를 돌려주는지 확인한다. */
class RuleToStringTest {

    @Test
    void Conway는_B3_S23() {
        assertEquals("B3/S23", new ConwayRule().toString());
    }

    @Test
    void HighLife는_B36_S23() {
        assertEquals("B36/S23", new HighLifeRule().toString());
    }

    @Test
    void 직접_입력한_규칙은_입력한_표기() {
        assertEquals("B36/S23", new BirthSurvivalRule("B36/S23").toString());
        assertEquals("B2/S", new BirthSurvivalRule("B2/S").toString());
    }
}
