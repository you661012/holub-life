package com.holub.life;

/**
 * 예측 요청 하나. 같은 잠금 안에서 함께 읽은 판, 규칙, 기준 세대 번호, 변경 번호의 묶음이다.
 * 만든 뒤에는 바뀌지 않는다. 그래서 잠금 밖의 다른 스레드에 넘겨 계산해도 된다.
 */
public class PredictionRequest {

    private final Generation start;   // 읽은 순간의 판
    private final Rule rule;          // 읽은 순간의 규칙
    private final long generation;    // 읽은 순간의 세대 번호 (기준 세대)
    private final long change;        // 읽은 순간의 변경 번호

    public PredictionRequest(Generation start, Rule rule, long generation, long change) {
        this.start = start;
        this.rule = rule;
        this.generation = generation;
        this.change = change;
    }

    public Generation start() {
        return start;
    }

    public Rule rule() {
        return rule;
    }

    /** 이 판을 읽었을 때의 세대 번호. 결과가 늦게 도착하면 그사이 진행한 세대를 여기서부터 센다. */
    public long generation() {
        return generation;
    }

    /** 이 판을 읽었을 때의 변경 번호. 클릭, Clear, Load, 규칙 변경 때 올라간다. 세대 진행으로는 올라가지 않는다. */
    public long change() {
        return change;
    }
}
