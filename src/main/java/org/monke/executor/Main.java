package org.monke.executor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Main {

    public static void main(String[] args) {

        MonitoredThreadPoolExecutor executorService = new MonitoredThreadPoolExecutor(3);

        for (int i = 1; i <= 10; i++) {
            executorService.execute(() -> {
                try {
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread could not go to sleep");
                }
            });
        }

        while (!executorService.isShutdown()) {

            System.out.println("Active: " + executorService.getActiveCount() +
                " | Completed: " + executorService.getCompletedTaskCount() +
                " | Total: " + executorService.getTotalTaskCount() +
                " | Queue: " + executorService.getQueueSize());

            try {
                Thread.sleep(300);

            } catch (InterruptedException e) {
                throw new RuntimeException("Thread could not go to sleep");
            }

            if (executorService.getCompletedTaskCount() == executorService.getTotalTaskCount()) {
                executorService.shutdown();
            }
        }

        if (!executorService.isTerminated()) {
            executorService.close();
        }
    }

    private void basicExample() {
        ExecutorService executorService = new CustomThreadPoolExecutor(3);

        for (int i = 1; i <= 10; i++) {
            int taskId = i;

            Future<Integer> future = executorService.submit(() -> {
                System.out.println(Thread.currentThread().getName() + " executing task " + taskId);

                try {
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread could not go to sleep");
                }
                return 42;
            });

            try {
                System.out.println("Result : " + future.get());

            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        executorService.close();
    }
}
