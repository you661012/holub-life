package com.holub.life;

/** Conway의 기본 규칙 B3/S23. (Strategy 패턴의 ConcreteStrategy 역할) */
public class ConwayRule implements Rule {

    @Override
    public boolean nextState(boolean alive, int neighbors) {
        return neighbors == 3 || (alive && neighbors == 2);
    }
}