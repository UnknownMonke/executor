package org.monke.executor;

import org.monke.executor.rejection.RejectionPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.monke.executor.config.ExecutorConfig.*;

public class Main {

    public static void main(String[] args) {
        MonitoredThreadPoolExecutor executorService = new MonitoredThreadPoolExecutor(ofPoolSize(1), ofQueueCapacity(4),
            new RejectionPolicy.CallerRunsPolicy());

        long now = System.currentTimeMillis();

        executorService.schedule(setTask("LOW PRIORITY (no deadline)"), ofPriority(1), null);
        executorService.schedule(setTask("MEDIUM PRIORITY (deadline 30s)"), ofPriority(5), withDeadline(now + 30000L));
        executorService.schedule(setTask("HIGH PRIORITY (no deadline)"), ofPriority(9), null);
        executorService.schedule(setTask("URGENT (deadline 10s, low priority)"), ofPriority(1), withDeadline(now + 10000L));

        try {
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        executorService.schedule(setTask("Old low priority, now aged high"), ofPriority(1), null);

        executorService.getQueuedTasksDetails().forEach(System.out::println);

        executorService.close();
    }

    private static Runnable setTask(String message) {
        return () -> {
            System.out.println("Executing task : " + message);

            try {
                Thread.sleep(5000);

            } catch (InterruptedException e) {
                throw new RuntimeException("Thread could not go to sleep");
            }
        };
    }

    private void basicExample() {
        ExecutorService executorService = new FixedThreadPoolExecutor(ofPoolSize(3));

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

    private void monitoredExample() {
        MonitoredThreadPoolExecutor executorService = new MonitoredThreadPoolExecutor(ofPoolSize(3));

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

    private void rejectionExample() {
        ExecutorService executorService = new FixedThreadPoolExecutor(ofPoolSize(2), ofQueueCapacity(2),
            new RejectionPolicy.CallerRunsPolicy());

        for (int i = 1; i <= 10; i++) {
            int taskId = i;

            executorService.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " running task " + taskId);

                try {
                    Thread.sleep(500);

                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread could not go to sleep");
                }
            });
        }
        executorService.close();
    }

    private void simplePriorityExample() {
        FixedThreadPoolExecutor executorService = new FixedThreadPoolExecutor(ofPoolSize(3), ofQueueCapacity(2),
            new RejectionPolicy.CallerRunsPolicy());

        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 1; i <= 10; i++) {
            int priority = (int) (Math.random() * 10);
            map.put(i, priority);
        }

        System.out.println(map);

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {

            executorService.execute(() -> {
                System.out.println(Thread.currentThread().getName() + " executing task " + entry.getKey() + " with priority " + entry.getValue());

                try {
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    throw new RuntimeException("Thread could not go to sleep");
                }
            }, entry.getValue());
        }
        executorService.close();
    }
}
