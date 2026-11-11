package com.holub.life;

/** 세포의 다음 세대 상태를 판정하는 규칙. (Strategy 패턴의 Strategy 역할) */
public interface Rule {

    /**
     * @param alive     지금 살아 있는가
     * @param neighbors 살아 있는 이웃의 수 (0~8)
     * @return 다음 세대에 살아 있으면 true
     */
    boolean nextState(boolean alive, int neighbors);
}