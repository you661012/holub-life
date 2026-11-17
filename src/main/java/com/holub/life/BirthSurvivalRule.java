package com.holub.life;

/** B/S 표기(예: "B3/S23")로 받은 규칙. (Strategy 패턴의 ConcreteStrategy 역할) */
public class BirthSurvivalRule implements Rule {

    private final String birth;      // 탄생하는 이웃 수들. "B36/S23"이면 "36"
    private final String survival;   // 생존하는 이웃 수들. "B36/S23"이면 "23"

    /**
     * @param notation B/S 표기. B 뒤에 탄생하는 이웃 수들, S 뒤에 생존하는 이웃 수들 (숫자는 0~8, 없어도 된다)
     * @throws IllegalArgumentException 표기가 이 모양이 아닐 때
     */
    public BirthSurvivalRule(String notation) {
        if (notation == null || !notation.matches("B[0-8]*/S[0-8]*")) {
            throw new IllegalArgumentException("B/S 표기가 아닙니다: " + notation);
        }
        String[] parts = notation.split("/");   // { "B36", "S23" }
        birth = parts[0].substring(1);          // "36"
        survival = parts[1].substring(1);       // "23"
    }

    @Override
    public boolean nextState(boolean alive, int neighbors) {
        if (alive) {
            return survival.contains("" + neighbors);   // 살아 있음: S 숫자들 안에 이웃 수가 있으면 생존
        }
        return birth.contains("" + neighbors);          // 죽어 있음: B 숫자들 안에 이웃 수가 있으면 탄생
    }

    /** 입력받은 B/S 표기. 창 아래 한 줄에 띄운다. */
    @Override
    public String toString() {
        return "B" + birth + "/S" + survival;
    }
}
