package com.holub.life;

import java.awt.Color;

/**
 * 창 아래 두 줄에 띄울 글자를 만든다. 윗줄은 세대 수, 세포 수, 규칙. 아랫줄은 예측한 결말.
 * 한 줄에 다 넣으면 창 너비(512px)를 넘어 끝이 잘려서 둘로 나눴다.
 * Swing을 쓰지 않는다. 글자와 색을 실제로 화면에 올리는 것은 Universe가 한다.
 * 스레드 하나(Swing 스레드)에서만 부른다.
 */
public class StatusLine {

    // 아랫줄(결말)의 글자색. 결말의 종류가 바뀐 것이 글자를 읽기 전에 색으로 먼저 보이게 한다.
    // 밝은 회색 바탕에서 읽히도록 어두운 색으로 골랐다.
    public static final Color EXTINCT_COLOR   = new Color(180, 0, 0);       // 소멸: 빨강
    public static final Color STILL_COLOR     = new Color(0, 70, 170);      // 정지: 파랑
    public static final Color REPEATING_COLOR = new Color(0, 120, 0);       // 반복: 초록
    public static final Color UNKNOWN_COLOR   = new Color(110, 110, 110);   // 미확정, 계산 중: 회색

    private long generation;             // 지금 세대 번호
    private int cells;                   // 살아 있는 칸 수
    private String rule = "";            // 규칙의 B/S 표기
    private long change;                 // 지금 변경 번호

    private PredictionRequest request;   // 지금 띄우고 있는 결말이 어느 요청의 것인가. 결과를 기다리는 중이면 null
    private Ending ending;

    /**
     * 판의 지금 상태를 받는다.
     * 변경 번호가 올라갔으면 판이나 규칙이 바뀐 것이므로, 띄우고 있던 결말을 내리고 새 결과를 기다린다.
     * 지금 것보다 낮은 변경 번호는 무시한다. 세대 진행이 읽은 정보가 클릭보다 늦게 도착한 것이다.
     */
    public void update(long generation, int cells, String rule, long change) {
        if (change < this.change) {
            return;
        }
        if (change > this.change) {
            request = null;
            ending = null;
        }
        this.generation = generation;
        this.cells = cells;
        this.rule = rule;
        this.change = change;
    }

    /** 계산이 끝난 결말을 받는다. 변경 번호가 지금과 다르면 옛 판이나 옛 규칙으로 한 예측이므로 버린다. */
    public void accept(PredictionRequest request, Ending ending) {
        if (request.change() != change) {
            return;
        }
        this.request = request;
        this.ending = ending;
    }

    /** 윗줄: 세대 수, 세포 수, 규칙. */
    public String stateText() {
        return "Generation " + generation + " | Cells " + cells + " | Rule " + rule;
    }

    /** 아랫줄: 예측한 결말. */
    public String endingText() {
        return "Ending: " + ending();
    }

    /** 아랫줄의 글자색. 결말의 종류로 정한다. 아직 그 세대에 이르지 않았어도 같은 색이다. */
    public Color endingColor() {
        if (ending == null) {
            return UNKNOWN_COLOR;
        }
        switch (ending.kind()) {
            case EXTINCT:
                return EXTINCT_COLOR;
            case STILL:
                return STILL_COLOR;
            case REPEATING:
                return REPEATING_COLOR;
            default:   // UNDECIDED
                return UNKNOWN_COLOR;
        }
    }

    /**
     * 판이 더는 변하지 않는 결말(소멸, 정지)에 도달했는가. 도달했으면 시계를 더 돌릴 이유가 없다.
     * 반복은 도달해도 판이 계속 움직이므로 끝난 것으로 보지 않는다. 미확정과 계산 중은 결말을 모르는 상태다.
     */
    public boolean isFinished() {
        if (ending == null) {
            return false;
        }
        boolean stopsChanging = ending.kind() == Ending.Kind.EXTINCT || ending.kind() == Ending.Kind.STILL;
        return stopsChanging && generationsLeft() <= 0;
    }

    /**
     * 예측한 결말까지 남은 세대 수. 0 이하면 이미 도달했다. 결말이 정해졌을 때(ending이 있을 때)만 부른다.
     * 예측한 뒤로 진행한 세대만큼 줄인다. 같은 규칙으로 진행한 것은 예측한 길을 따라간 것이다.
     */
    private long generationsLeft() {
        long passed = Math.max(0, generation - request.generation());
        return ending.after() - passed;
    }

    private String ending() {
        if (ending == null) {
            return "calculating...";
        }
        if (ending.kind() == Ending.Kind.UNDECIDED) {
            return "undecided within " + ending.after() + " generations";
        }

        long left = generationsLeft();
        String in = (left > 0) ? " in " + left + (left == 1 ? " generation" : " generations") : "";

        switch (ending.kind()) {
            case EXTINCT:
                return "extinct" + in;
            case STILL:
                return "still" + in;
            default:   // REPEATING
                return (left > 0 ? "repeats" : "repeating") + " (period " + ending.period() + ")" + in;
        }
    }
}
