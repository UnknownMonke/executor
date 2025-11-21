package org.monke.executor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>Executes submitted {@link Runnable} tasks into a fixed-size pool of available threads.</p>
 * An {@code Executor} is normally used
 * instead of explicitly creating threads. For example, rather than
 * invoking {@code new Thread(new RunnableTask()).start()} for each
 * of a set of tasks, we might use:
 *
 * <pre>{@code
 * Executor executor = customExecutor();
 * executor.execute(new RunnableTask1());
 * executor.execute(new RunnableTask2());
 * ...
 * }</pre>
 *
 * However, the executor does not strictly require
 * that execution be asynchronous. In the simplest case, an executor
 * can run the submitted task immediately in the caller's thread :
 *
 * <pre>{@code
 * class DirectExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     r.run();
 *   }
 * }
 * }</pre>
 *
 * More typically, tasks are executed in some thread other than the
 * caller's thread.  The executor below spawns a new thread for each
 * task :
 *
 * <pre>{@code
 * class ThreadPerTaskExecutor implements Executor {
 *   public void execute(Runnable r) {
 *     new Thread(r).start();
 *   }
 * }
 * }</pre>
 *
 * Many executor implementations impose some sort of
 * limitation on how and when tasks are scheduled.  The executor below
 * serializes the submission of tasks to a second executor,
 * illustrating a composite executor :
 *
 * <pre>{@code
 * class SerialExecutor implements Executor {
 *   final Queue<Runnable> tasks = new ArrayDeque<>();
 *   final Executor executor;
 *   Runnable active;
 *
 *   SerialExecutor(Executor executor) {
 *     this.executor = executor;
 *   }
 *
 *   public synchronized void execute(Runnable r) {
 *     tasks.add(() -> {
 *       try {
 *         r.run();
 *       } finally {
 *         scheduleNext();
 *       }
 *     });
 *     if (active == null) {
 *       scheduleNext();
 *     }
 *   }
 *
 *   protected synchronized void scheduleNext() {
 *     if ((active = tasks.poll()) != null) {
 *       executor.execute(active);
 *     }
 *   }
 * }
 * }</pre>
 */
public class CustomThreadPoolExecutor implements Executor {

    private final BlockingQueue<Runnable> taskQueue;
    private final Set<Worker> workers = new HashSet<>();
    private final AtomicBoolean stopping = new AtomicBoolean(false);


    public CustomThreadPoolExecutor(int poolSize) {
        taskQueue = new LinkedBlockingQueue<>();

        // Init n workers.
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker("Worker-" + i, taskQueue, stopping);
            workers.add(worker);
            worker.start();
        }
    }

    /**
     * Schedules task by adding it to the queue. When a worker is available, it will poll and execute task.
     */
    @Override
    public void execute(Runnable task) {
        if (!stopping.get()) {
            taskQueue.offer(task);
        } else {
            throw new RejectedExecutionException("Thread pool is shut down");
        }
    }

    public void shutdown() {
        stopping.set(true);
    }
}
