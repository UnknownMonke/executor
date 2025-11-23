package org.monke.executor;

import org.monke.executor.rejection.RejectedExecutionHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread pool executor with task monitoring capabilities.
 */
public class MonitoredThreadPoolExecutor extends FixedThreadPoolExecutor {

    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final AtomicLong completedTaskCount = new AtomicLong(0);
    private final AtomicLong totalTaskCount = new AtomicLong(0);


    public MonitoredThreadPoolExecutor(int poolSize, int queueCapacity, RejectedExecutionHandler rejectedExecutionHandler) {
        super(poolSize, queueCapacity, rejectedExecutionHandler);
    }

    public MonitoredThreadPoolExecutor(int poolSize) {
        super(poolSize);
    }

    public MonitoredThreadPoolExecutor() {
        super();
    }


    @Override
    public void execute(PrioritizedTask task) {
        totalTaskCount.incrementAndGet();
        super.execute(taskProxy(task));
    }

    private PrioritizedTask taskProxy(PrioritizedTask task) {
        return new PrioritizedTask(
            () -> {
                activeTaskCount.incrementAndGet();
                try {
                    task.run();
                } finally {
                    activeTaskCount.decrementAndGet();
                    completedTaskCount.incrementAndGet();
                }
            },
            task.getBasePriority(),
            task.getDeadline()
        );
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

    public List<String> getQueuedTasksDetails() {
        return super.getTaskQueue().stream()
            .filter(t -> t instanceof PrioritizedTask)
            .map(Object::toString)
            .toList();
    }

    public Map<String, Long> getTaskStatistics() {
        return Map.of(
            "Queue Size", (long) getQueueSize(),
            "Total Tasks Submitted", getTotalTaskCount(),
            "Completed Tasks", getCompletedTaskCount(),
            "Active Tasks", (long) getActiveCount()
        );
    }
}
