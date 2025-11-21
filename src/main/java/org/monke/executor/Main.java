package org.monke.executor;

public class Main {

    public static void main(String[] args) {

        CustomThreadPoolExecutor pool = new CustomThreadPoolExecutor(3);

        for (int i = 1; i <= 10; i++) {
            int taskId = i;

            pool.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " executing task " + taskId);

                try {
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread could not go to sleep");
                }
            });
        }
        pool.shutdown();
    }
}
