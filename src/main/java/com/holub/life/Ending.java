package com.holub.life;

/** 예측한 결말. EndingPredictor가 만들어 돌려준다. 만든 뒤에는 바뀌지 않는다. */
public class Ending {

    /** 결말의 종류. */
    public enum Kind {
        EXTINCT,     // 소멸: 살아 있는 칸이 하나도 없게 된다
        STILL,       // 정지: 더는 변하지 않는 모양이 된다
        REPEATING,   // 반복: 같은 모양들이 일정한 주기로 되풀이된다
        UNDECIDED    // 미확정: 한도까지 돌려 봤지만 위 셋 중 어느 것도 나오지 않았다
    }

    private final Kind kind;
    private final int after;
    private final int period;

    public Ending(Kind kind, int after, int period) {
        this.kind = kind;
        this.after = after;
        this.period = period;
    }

    public Kind kind() {
        return kind;
    }

    /**
     * 지금부터 몇 세대 뒤인가.
     * 소멸: 판이 비는 세대 / 정지: 멈춘 모양이 처음 나오는 세대 / 반복: 되풀이가 시작되는 세대 /
     * 미확정: 돌려 본 세대 수(한도).
     */
    public int after() {
        return after;
    }

    /** 몇 세대마다 같은 모양으로 돌아오는가. 반복은 2 이상, 정지는 1, 소멸과 미확정은 0. */
    public int period() {
        return period;
    }
}
