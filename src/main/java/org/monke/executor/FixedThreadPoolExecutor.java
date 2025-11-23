package org.monke.executor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executes submitted {@link Runnable} tasks into a fixed-size pool of available threads.
 * An {@link Executor} is normally used instead of explicitly creating threads.
 */
public class FixedThreadPoolExecutor implements ExecutorService {

    private final BlockingQueue<Runnable> taskQueue;
    private final Set<Worker> workers = new HashSet<>();
    private final RejectedExecutionHandler rejectedExecutionHandler;

    private final AtomicBoolean isShutdown = new AtomicBoolean(false);


    public FixedThreadPoolExecutor(int poolSize, int queueCapacity,
                                   RejectedExecutionHandler rejectedExecutionHandler) {
        taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.rejectedExecutionHandler = rejectedExecutionHandler;

        // Inits n workers.
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker("Worker-" + i, taskQueue, isShutdown);
            workers.add(worker);
            worker.start();
        }
    }

    public FixedThreadPoolExecutor(int poolSize) {
        this(poolSize, ExecutorConfig.DEFAULT_QUEUE_CAPACITY, ExecutorConfig.DEFAULT_REJECTION_POLICY);
    }

    public FixedThreadPoolExecutor() {
        this(ExecutorConfig.DEFAULT_POOL_SIZE, ExecutorConfig.DEFAULT_QUEUE_CAPACITY, ExecutorConfig.DEFAULT_REJECTION_POLICY);
    }


    /**
     * Schedules task by adding it to the queue. When a worker is available, it will poll and execute the task.
     */
    @Override
    public void execute(Runnable task) {
        if (isShutdown.get()) {
            rejectedExecutionHandler.rejectedExecution(task, this);
            return;
        }
        boolean inserted = taskQueue.offer(task);

        if (!inserted) {
            rejectedExecutionHandler.rejectedExecution(task, this);
        }
    }

    /**
     * Submits a value-returning task for execution and returns a
     * {@link Future} representing the pending results of the task.
     * The {@link Future#get()} method will return the task result upon successful completion.
     *
     * <p> Calling {@code get} method immediately will block until task completion :
     *
     * <pre> {@code
     *     result = executor.submit(aCallable).get();
     * }
     *
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> futureTask = new FutureTask<>(task);

        execute(futureTask);

        return futureTask;
    }

    /**
     * Submits a task for execution and returns a {@link Future} representing that task.
     * The {@link Future#get()} method will always return {@code null} upon <em>successful</em> completion.
     */
    @Override
    public Future<?> submit(Runnable task) {
        return submit(Executors.callable(task, null));
    }

    /**
     * Submits a task for execution and returns a {@link Future} representing that task.
     * The {@link Future#get()} method will always return specified result upon successful completion.
     */
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return submit(Executors.callable(task, result));
    }

    /**
     * Executes the given tasks, returning a list of Futures holding their status and results when all complete.
     * {@link Future#isDone} is {@code true} for each element of the returned list.
     *
     * <p> <em>Completed</em> task could have terminated either normally or by throwing an exception.
     *
     * <p> The results of this method are undefined if the given collection is modified while this operation is in progress.
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        List<Future<T>> futures = new ArrayList<>();

        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }

        for (Future<T> future : futures) {
            try {
                future.get(); // Waits for task to complete.

            } catch (ExecutionException | CancellationException e) {
                future.cancel(true);
            }
        }
        return futures;
    }

    /**
     * Returns a list of Futures holding their status and results when all tasks complete or the timeout expires, whichever happens first.
     *
     * <p> Upon return, tasks that have not completed are cancelled.
     */
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        long endTime = System.nanoTime() + unit.toNanos(timeout);

        List<Future<T>> futures = new ArrayList<>();

        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }

        for (Future<T> future : futures) {
            long remaining = endTime - System.nanoTime();
            if (remaining <= 0) {
                break;
            }
            try {
                future.get(remaining, TimeUnit.NANOSECONDS);
            } catch (ExecutionException | CancellationException | TimeoutException e) {
                future.cancel(true);
            }
        }
        return futures;
    }

    /**
     * Returns a Future when any task complete.
     *
     * <p> Upon return, tasks that have not completed are cancelled.
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {

        List<Future<T>> futures = new ArrayList<>();
        try {
            for (Callable<T> task : tasks) {
                futures.add(submit(task));
            }

            while (true) {
                for (Future<T> future : futures) {
                    if (future.isDone()) {
                        return future.get(); // Returns first completed result.
                    }
                }
            }

        } finally {
            for (Future<T> future : futures) {
                future.cancel(true); // Cancels any remaining task.
            }
        }
    }

    /**
     * Returns a Future when any task complete or the timeout expires, whichever happens first.
     *
     * <p> Upon return, tasks that have not completed are cancelled.
     */
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {

        long endTime = System.nanoTime() + unit.toNanos(timeout);

        List<Future<T>> futures = new ArrayList<>();

        try {
            for (Callable<T> task : tasks) {
                futures.add(submit(task));
            }

            while (true) {
                for (Future<T> future : futures) {
                    if (future.isDone()) {
                        return future.get();
                    }
                }
                if (System.nanoTime() > endTime) {
                    throw new TimeoutException("invokeAny timed out");
                }
            }

        } finally {
            for (Future<T> future : futures) {
                future.cancel(true);
            }
        }
    }

    /**
     * Initiates an orderly shutdown in which previously submitted tasks are
     * executed, but no new tasks will be accepted. This method waits until all
     * tasks have completed execution and the executor has terminated.
     *
     * <p> If interrupted while waiting, this method stops all executing tasks as
     * if by invoking {@link #shutdownNow()}. It then continues to wait until all
     * actively executing tasks have completed. Tasks that were awaiting
     * execution are not executed. The interrupt status will be re-asserted
     * before this method returns.
     *
     * <p> If already terminated, invoking this method has no effect.
     */
    @Override
    public void close() {
        boolean terminated = isTerminated();
        if (!terminated) {
            shutdown();
            boolean interrupted = false;
            while (!terminated) {
                try {
                    terminated = awaitTermination(15L, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    if (!interrupted) {
                        shutdownNow();
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void shutdown() {
        isShutdown.set(true);
    }

    /**
     * Attempts to stop all actively executing tasks, halts the
     * processing of waiting tasks, and returns a list of the tasks
     * that were awaiting execution.
     *
     * <p> This method does not wait for actively executing tasks to
     * terminate. Use {@link #awaitTermination} to
     * do that.
     *
     * <p> There are no guarantees beyond best-effort attempts to stop
     * processing actively executing tasks. For example, typical
     * implementations will cancel via {@link Thread#interrupt}, so any
     * task that fails to respond to interrupts may never terminate.
     */
    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        for (Worker worker : workers) {
            worker.interrupt();
        }
        return new ArrayList<>(taskQueue);
    }

    @Override
    public boolean isShutdown() {
        return isShutdown.get();
    }

    /**
     * Returns {@code true} if all tasks have completed following shut down.
     *
     * <p> Never {@code true} unless either {@link #isShutdown} or {@link #shutdownNow} was called first.
     */
    @Override
    public boolean isTerminated() {
        return workers.stream().noneMatch(Thread::isAlive);
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown
     * request, or the timeout occurs, or the current thread is
     * interrupted, whichever happens first.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return {@code true} if this executor terminated and
     *         {@code false} if the timeout elapsed before termination
     * @throws InterruptedException If interrupted while waiting.
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long endTime = System.currentTimeMillis() + unit.toMillis(timeout);

        for (Worker worker : workers) {
            long timeLeft = endTime - System.currentTimeMillis();
            if (timeLeft <= 0) {
                return false;
            }
            worker.join(timeLeft);
        }
        return true;
    }

    BlockingQueue<Runnable> getTaskQueue() {
        return taskQueue;
    }
}
