package com.holub.life;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 예측 계산을 맡는 작업자. 스레드 하나로 계산하고, 기다리는 요청은 가장 최신 것 하나만 남긴다.
 * 이미 계산 중인 옛 요청은 끝까지 계산해서 결과를 전한다. 그 결과를 쓸지 버릴지는 받는 쪽이 변경 번호로 정한다.
 */
public class PredictionWorker {

    private final Function<PredictionRequest, Ending> calculator;    // 요청을 받아 결말을 계산하는 것
    private final BiConsumer<PredictionRequest, Ending> onDone;      // 계산이 끝나면 요청과 결말을 받는 쪽

    // 스레드를 하나만 둔다. 데몬 스레드라서 계산 중이어도 프로그램이 끝나는 것을 막지 않는다
    private final Executor thread = Executors.newSingleThreadExecutor(task -> {
        Thread t = new Thread(task, "prediction");
        t.setDaemon(true);
        return t;
    });

    private PredictionRequest waiting;   // 아직 계산을 시작하지 않은 요청. 없으면 null. 새 요청이 오면 덮어쓴다
    private boolean working;             // 작업자가 일하는 중인가

    public PredictionWorker(Function<PredictionRequest, Ending> calculator,
                            BiConsumer<PredictionRequest, Ending> onDone) {
        this.calculator = calculator;
        this.onDone = onDone;
    }

    /** 예측을 요청한다. 기다리지 않고 바로 돌아온다. 앞서 기다리던 요청이 있으면 그것은 계산하지 않는다. */
    public synchronized void request(PredictionRequest request) {
        waiting = request;
        if (!working) {
            working = true;
            thread.execute(this::work);
        }
    }

    /** 작업자 스레드가 실행한다. 기다리는 요청이 없어질 때까지 하나씩 꺼내 계산한다. */
    private void work() {
        while (true) {
            PredictionRequest request;
            synchronized (this) {
                request = waiting;
                waiting = null;
                if (request == null) {
                    working = false;
                    return;
                }
            }
            try {
                onDone.accept(request, calculator.apply(request));   // 계산은 잠금 밖에서 한다. 그래야 그동안 새 요청을 받는다
            } catch (RuntimeException e) {
                e.printStackTrace();                                 // 한 번 실패해도 다음 요청은 계산한다
            }
        }
    }
}
