package com.holub.life;

/** Conway의 기본 규칙 B3/S23. (Strategy 패턴의 ConcreteStrategy 역할) */
public class ConwayRule implements Rule {

    @Override
    public boolean nextState(boolean alive, int neighbors) {
        return neighbors == 3 || (alive && neighbors == 2);
    }

    /** 이 규칙의 B/S 표기. 창 아래에 띄운다. */
    @Override
    public String toString() {
        return "B3/S23";
    }
}
