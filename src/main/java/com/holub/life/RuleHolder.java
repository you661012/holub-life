package com.holub.life;

/**
 * 현재 적용 중인 Rule을 보관한다.
 * 모든 Resident가 같은 RuleHolder를 참조하므로, 여기의 규칙을 바꾸면
 * 모든 Resident가 다음 판정부터 새 규칙을 사용한다.
 */
public class RuleHolder {

    private volatile Rule current = new ConwayRule();   // 기본값은 Conway

    /** 현재 적용 중인 규칙을 돌려준다. */
    public Rule current() {
        return current;
    }

    /** 적용할 규칙을 바꾼다. */
    public void change(Rule rule) {
        current = rule;
    }
}