package org.monke.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Runs tasks from provided queue.</p>
 * <p>Separate class benefits :
 * <ul>
 *     <li>Separation of concerns : thread pool handles lifecycle, worker handles execution.</li>
 *     <li>Better readability :	cleaner executor code.</li>
 *     <li>Easier to extend.</li>
 *     <li>Testable.</li>
 * </ul>
 * </p>
 */
public class Worker extends Thread {

    private final BlockingQueue<Runnable> taskQueue;
    private final AtomicBoolean stopping;

    public Worker(String name, BlockingQueue<Runnable> taskQueue, AtomicBoolean stopping) {
        super(name);
        this.taskQueue = taskQueue;
        this.stopping = stopping;
    }

    /**
     * Tries running next scheduled task. Interrupts on error.
     */
    @Override
    public void run() {
        while (!stopping.get() || !taskQueue.isEmpty()) {
            try {
                Runnable task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                if (task != null) {
                    task.run();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
