package com.holub.life;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PredictionWorker가 작업자 하나로 계산하고, 기다리는 요청은 최신 것 하나만 남기는지 확인한다.
 * 진짜 예측기 대신 테스트가 정한 때에 끝나는 가짜 계산을 넣는다. 시간을 재지 않고 끝나는 시점을 테스트가 정한다.
 */
class PredictionWorkerTest {

    private static final Ending ANY_ENDING = new Ending(Ending.Kind.EXTINCT, 0, 0);

    @Test
    void 계산하는_동안_들어온_요청은_최신_것만_계산한다() throws InterruptedException {
        PredictionRequest a = request(1), b = request(2), c = request(3);
        List<PredictionRequest> computed = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch aStarted = new CountDownLatch(1);
        CountDownLatch finishA = new CountDownLatch(1);
        CountDownLatch delivered = new CountDownLatch(2);   // A와 C

        PredictionWorker worker = new PredictionWorker(
                r -> {
                    computed.add(r);
                    if (r == a) {
                        aStarted.countDown();
                        awaitQuietly(finishA);              // 테스트가 신호를 줄 때까지 A는 끝나지 않는다
                    }
                    return ANY_ENDING;
                },
                (r, ending) -> delivered.countDown());

        worker.request(a);
        assertTrue(aStarted.await(5, TimeUnit.SECONDS));
        worker.request(b);                                  // A를 계산하는 동안 B, C가 들어온다
        worker.request(c);
        finishA.countDown();

        assertTrue(delivered.await(5, TimeUnit.SECONDS));
        assertEquals(List.of(a, c), computed);              // B는 계산하지 않는다
    }

    @Test
    void 결과는_그_요청과_함께_전한다() throws InterruptedException {
        PredictionRequest a = request(1);
        Ending ending = new Ending(Ending.Kind.STILL, 5, 1);
        List<PredictionRequest> requests = Collections.synchronizedList(new ArrayList<>());
        List<Ending> endings = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch delivered = new CountDownLatch(1);

        PredictionWorker worker = new PredictionWorker(
                r -> ending,
                (r, e) -> { requests.add(r); endings.add(e); delivered.countDown(); });

        worker.request(a);

        assertTrue(delivered.await(5, TimeUnit.SECONDS));
        assertEquals(List.of(a), requests);
        assertEquals(List.of(ending), endings);
    }

    @Test
    void 쉬고_있다가_새_요청이_오면_다시_계산한다() throws InterruptedException {
        PredictionRequest a = request(1), b = request(2);
        List<PredictionRequest> computed = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch first = new CountDownLatch(1);
        CountDownLatch second = new CountDownLatch(2);

        PredictionWorker worker = new PredictionWorker(
                r -> { computed.add(r); return ANY_ENDING; },
                (r, e) -> { first.countDown(); second.countDown(); });

        worker.request(a);
        assertTrue(first.await(5, TimeUnit.SECONDS));       // A를 끝내고 작업자가 쉰다
        worker.request(b);

        assertTrue(second.await(5, TimeUnit.SECONDS));
        assertEquals(List.of(a, b), computed);
    }

    @Test
    void 계산이_실패해도_다음_요청은_계산한다() throws InterruptedException {
        PredictionRequest a = request(1), b = request(2);
        CountDownLatch delivered = new CountDownLatch(1);
        List<PredictionRequest> requests = Collections.synchronizedList(new ArrayList<>());

        PredictionWorker worker = new PredictionWorker(
                r -> {
                    if (r == a) throw new IllegalStateException("일부러 낸 실패");
                    return ANY_ENDING;
                },
                (r, e) -> { requests.add(r); delivered.countDown(); });

        worker.request(a);
        worker.request(b);

        assertTrue(delivered.await(5, TimeUnit.SECONDS));
        assertEquals(List.of(b), requests);
    }

    /** 변경 번호만 다른 요청. 이 테스트에서는 판과 규칙의 내용은 쓰지 않는다. */
    private static PredictionRequest request(long change) {
        return new PredictionRequest(new Generation(Set.of(), 64), new ConwayRule(), 0, change);
    }

    private static void awaitQuietly(CountDownLatch latch) {
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
