package com.holub.life;

/**
 * 창 아래 한 줄에 띄울 글자를 만든다: 세대 수, 세포 수, 규칙, 예측한 결말.
 * Swing을 쓰지 않는다. 글자를 실제로 화면에 올리는 것은 Universe가 한다.
 * 스레드 하나(Swing 스레드)에서만 부른다.
 */
public class StatusLine {

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

    public String text() {
        return "Generation " + generation + " | Cells " + cells + " | Rule " + rule + " | Ending: " + endingText();
    }

    private String endingText() {
        if (ending == null) {
            return "calculating...";
        }
        if (ending.kind() == Ending.Kind.UNDECIDED) {
            return "undecided within " + ending.after() + " generations";
        }

        // 예측한 뒤로 진행한 세대만큼 남은 세대를 줄인다. 같은 규칙으로 진행한 것은 예측한 길을 따라간 것이다
        long passed = Math.max(0, generation - request.generation());
        long left = ending.after() - passed;
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
