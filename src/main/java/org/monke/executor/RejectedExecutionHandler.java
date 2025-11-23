package org.monke.executor;

@FunctionalInterface
public interface RejectedExecutionHandler {

    void rejectedExecution(Runnable task, FixedThreadPoolExecutor executor);
}
