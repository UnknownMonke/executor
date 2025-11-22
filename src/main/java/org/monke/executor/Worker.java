package org.monke.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs tasks from provided queue.
 *
 * <p>Separate class benefits :
 * <ul>
 *     <li>Separation of concerns : thread pool handles lifecycle, worker handles execution.
 *     <li>Better readability :	cleaner executor code.
 *     <li>Easier to extend.
 *     <li>Testable.
 * </ul>
 */
public class Worker extends Thread {

    private final BlockingQueue<Runnable> taskQueue;
    private final AtomicBoolean isShutdown;

    public Worker(String name, BlockingQueue<Runnable> taskQueue, AtomicBoolean isShutdown) {
        super(name);
        this.taskQueue = taskQueue;
        this.isShutdown = isShutdown;
    }

    /**
     * Tries running next scheduled task. Interrupts on error.
     */
    @Override
    public void run() {
        while (!isShutdown.get() || !taskQueue.isEmpty()) {
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
