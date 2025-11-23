package org.monke.executor;

import java.util.concurrent.RejectedExecutionException;

public class RejectionPolicy {

    /**
     * Throws upon rejection.
     */
    public static class AbortPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable task, FixedThreadPoolExecutor executor) {
            throw new RejectedExecutionException("Task " + task + " rejected from " + executor + ". Queue is full.");
        }
    }

    /**
     * Silently discards rejected tasks.
     */
    public static class DiscardPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable task, FixedThreadPoolExecutor executor) {
            // Silently discards.
        }
    }

    /**
     * Discards oldest task in queue and retries executing the new task.
     */
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable task, FixedThreadPoolExecutor executor) {
            executor.getTaskQueue().poll();
            executor.execute(task);
        }
    }

    /**
     * Runs task on caller thread upon rejection.
     */
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable task, FixedThreadPoolExecutor executor) {
            if (!executor.isShutdown()) {
                task.run(); // Runs on caller thread.
            }
        }
    }
}
