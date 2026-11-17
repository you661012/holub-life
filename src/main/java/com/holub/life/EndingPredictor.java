package com.holub.life;

import java.util.HashMap;
import java.util.Map;

/**
 * 시작 배치와 규칙으로 판을 미리 돌려 결말을 예측한다.
 * Life Game은 같은 배치와 같은 규칙이면 항상 같게 진행한다. 그래서 전에 본 모양이 다시 나오면
 * 그 뒤는 같은 진행이 되풀이될 뿐이고, 결말이 정해진다.
 */
public class EndingPredictor {

    // 최대 몇 세대까지 미리 돌려 볼지.
    // 빈 판을 만나면 그 판이 계속 비어 있는지 보려고 다음 세대를 계산한다. 이 확인용 계산은 여기에 넣지 않는다.
    // 한도 세대에 판이 비었을 때도 소멸인지 확정할 수 있게 하려는 것이다.
    private final int limit;

    public EndingPredictor(int limit) {
        this.limit = limit;
    }

    /** start에서 시작해 rule로 진행했을 때의 결말을 돌려준다. */
    public Ending predict(Generation start, Rule rule) {
        Map<Generation, Integer> seenAt = new HashMap<>();   // 지나온 세대 → 그것이 몇 번째 세대였는지
        Generation current = start;

        for (int n = 0; ; n++) {                             // n: 지금 보고 있는 것이 몇 세대 뒤인지
            // 빈 판이 다음 세대에도 빈 판일 때만 소멸이다. 이웃이 0개여도 태어나는 규칙(B0)에서는 빈 판에서 모든 칸이 태어난다.
            // 비어서 멈춘 판은 "더는 변하지 않는 모양"이기도 하지만 정지가 아니라 소멸로 답한다. 그래서 이것을 먼저 본다.
            if (current.isEmpty() && current.next(rule).isEmpty()) {
                return new Ending(Ending.Kind.EXTINCT, n, 0);
            }

            Integer firstSeen = seenAt.get(current);         // 같은 모양을 전에 봤다면 그때의 세대 번호, 처음이면 null
            if (firstSeen != null) {
                int period = n - firstSeen;                  // 몇 세대 만에 같은 모양으로 돌아왔는가
                if (period == 1) {
                    return new Ending(Ending.Kind.STILL, firstSeen, period);
                }
                return new Ending(Ending.Kind.REPEATING, firstSeen, period);
            }

            if (n == limit) {                                // 한도만큼 돌렸는데도 결말이 없다
                return new Ending(Ending.Kind.UNDECIDED, limit, 0);
            }

            seenAt.put(current, n);
            current = current.next(rule);
        }
    }
}
