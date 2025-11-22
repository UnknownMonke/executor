package org.monke.executor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread pool executor with task monitoring capabilities.
 */
public class MonitoredThreadPoolExecutor extends CustomThreadPoolExecutor {

    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final AtomicLong completedTaskCount = new AtomicLong(0);
    private final AtomicLong totalTaskCount = new AtomicLong(0);


    public MonitoredThreadPoolExecutor(int poolSize) {
        super(poolSize);
    }

    @Override
    public void execute(Runnable task) {
        totalTaskCount.incrementAndGet();
        super.execute(taskProxy(task));
    }

    private Runnable taskProxy(Runnable task) {
        return () -> {
            activeTaskCount.incrementAndGet();
            try {
                task.run();
            } finally {
                activeTaskCount.decrementAndGet();
                completedTaskCount.incrementAndGet();
            }
        };
    }

    public int getActiveCount() {
        return activeTaskCount.get();
    }

    public long getCompletedTaskCount() {
        return completedTaskCount.get();
    }

    public long getTotalTaskCount() {
        return totalTaskCount.get();
    }

    public int getQueueSize() {
        return super.getTaskQueue().size();
    }
}
