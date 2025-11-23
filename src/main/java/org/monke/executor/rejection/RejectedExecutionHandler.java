package org.monke.executor.rejection;

import org.monke.executor.FixedThreadPoolExecutor;

@FunctionalInterface
public interface RejectedExecutionHandler {

    void rejectedExecution(Runnable task, FixedThreadPoolExecutor executor);
}
