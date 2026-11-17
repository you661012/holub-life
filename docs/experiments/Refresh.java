import com.holub.life.*;
/** 빈 판을 B0/S로 돌릴 때 판의 transition()이 무엇을 돌려주는지 본다. Universe의 tick()은 이 값이 true일 때만 화면을 다시 그린다. */
public class Refresh {
    public static void main(String[] a) {
        RuleHolder holder = new RuleHolder();
        holder.change(new BirthSurvivalRule("B0/S"));
        Cell board = new Neighborhood(8, new Neighborhood(8, new Resident(holder)));
        for (int gen = 1; gen <= 3; gen++) {
            boolean willChange = board.figureNextState(Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY, Cell.DUMMY);
            boolean transitionReturned = willChange && board.transition();
            System.out.println(gen + "세대로 넘어갈 때: figureNextState=" + willChange + ", transition()=" + transitionReturned + " → refreshNow() " + (transitionReturned ? "부른다" : "안 부른다"));
        }
    }
}
