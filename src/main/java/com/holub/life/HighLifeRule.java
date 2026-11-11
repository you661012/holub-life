package com.holub.life;

/** HighLife 규칙 B36/S23. Conway 규칙에 "죽은 세포의 이웃이 6개면 탄생"을 더한 변형. (Strategy 패턴의 ConcreteStrategy 역할) */
public class HighLifeRule implements Rule {

    @Override
    public boolean nextState(boolean alive, int neighbors) {
        return neighbors == 3                    // 이웃 3개: 탄생 또는 생존
            || (alive && neighbors == 2)         // 살아 있고 이웃 2개: 생존
            || (!alive && neighbors == 6);       // 죽어 있고 이웃 6개: 탄생 (Conway와 다른 점)
    }
}
