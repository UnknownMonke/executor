package org.monke.executor;

import java.time.Instant;

public class PrioritizedTask implements Runnable, Comparable<PrioritizedTask> {

    private final Runnable task;
    private final int basePriority;
    private final long enqueueTime;
    private final Long deadline; // null = no deadline.

    private static final long AGING_THRESHOLD_MS = 2000; // Every 2s of waiting, boosts priority.


    public PrioritizedTask(Runnable task, int basePriority, Long deadline) {
        this.task = task;
        this.basePriority = basePriority;
        this.deadline = deadline;
        enqueueTime = System.currentTimeMillis();
    }


    public int getBasePriority() {
        return basePriority;
    }

    public Long getDeadline() {
        return deadline;
    }

    private int getEffectivePriority() {
        int agingBoost = (int) (getWaitingTime() / AGING_THRESHOLD_MS);
        return Math.max(0, basePriority - agingBoost);
    }

    public long getWaitingTime() {
        return System.currentTimeMillis() - enqueueTime;
    }

    /**
     * Prioritizes in order :
     * <ul>
     *     <li> Task with closer deadline.
     *     <li> Task with higher effective priority (longest waiting time).
     *     <li> Task with higher base priority.
     * </ul>
     *
     * <p> FIFO for identical deadline and priority.
     */
    @Override
    public int compareTo(PrioritizedTask other) {
        if (this.deadline != null && other.deadline != null) {
            int result = this.deadline.compareTo(other.deadline);
            if (result != 0) return result;

        } else if (this.deadline != null) {
            return -1;

        } else if (other.deadline != null) {
            return 1;
        }
        return Integer.compare(this.getEffectivePriority(), other.getEffectivePriority());
    }

    @Override
    public void run() {
        task.run();
    }

    @Override
    public String toString() {
        return String.format(
            "PrioritizedTask(basePriority : %d, effectivePriority : %d, wait : %dms, deadline : %s)",
            basePriority, getEffectivePriority(), getWaitingTime(), deadline != null ? Instant.ofEpochMilli(deadline) : "none");
    }
}
