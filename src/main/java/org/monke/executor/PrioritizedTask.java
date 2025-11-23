package org.monke.executor;

public class PrioritizedTask implements Runnable, Comparable<PrioritizedTask> {

    private final Runnable task;
    private final int priority;

    public PrioritizedTask(Runnable task, int priority) {
        this.task = task;
        this.priority = priority;
    }

    @Override
    public void run() {
        task.run();
    }

    @Override
    public int compareTo(PrioritizedTask other) {
        return Integer.compare(other.priority, this.priority);
    }

    @Override
    public String toString() {
        return "PrioritizedTask(priority = " + priority + ")";
    }
}
