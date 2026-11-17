public class StaticFieldDemo {
    static class Board {
        int own = 0;             // 그냥 필드: 판마다 하나씩 따로 있다
        static int shared = 0;   // static 필드: 판이 몇 개든 딱 하나뿐이다

        void work() {
            own = own + 1;
            shared = shared + 1;
        }
    }

    public static void main(String[] args) throws Exception {
        Board screen = new Board();   // 화면의 판
        Board ahead  = new Board();   // 미리 돌리는 판

        screen.work();
        ahead.work();
        System.out.println("각 판이 한 번씩 일한 뒤");
        System.out.println("  screen.own = " + screen.own + ",  ahead.own = " + ahead.own + "   (따로 센다)");
        System.out.println("  shared     = " + Board.shared + "                    (둘이 같은 변수를 올렸다)");

        // 이제 두 스레드가 각자 자기 판으로 10만 번씩 동시에 일한다
        screen.own = 0; ahead.own = 0; Board.shared = 0;
        Thread clock = new Thread(() -> { for (int i = 0; i < 100000; i++) screen.work(); });
        Thread swing = new Thread(() -> { for (int i = 0; i < 100000; i++) ahead.work(); });
        clock.start(); swing.start(); clock.join(); swing.join();
        System.out.println("두 스레드가 10만 번씩 동시에 일한 뒤");
        System.out.println("  screen.own = " + screen.own + ",  ahead.own = " + ahead.own + "   (각자 정확하다)");
        System.out.println("  shared     = " + Board.shared + "   (200000이어야 하는데 모자란다)");
    }
}
