package org.monke.executor;

/**
 * Static configuration class to avoid magic numbers, and provide readability.
 */
public class ExecutorConfig {

    public static final int DEFAULT_POOL_SIZE = 10;
    public static final int DEFAULT_QUEUE_CAPACITY = 100;
    public static final RejectedExecutionHandler DEFAULT_REJECTION_POLICY = new RejectionPolicy.AbortPolicy();

    public static int ofPoolSize(int poolSize) {
        return poolSize;
    }

    public static int ofQueueCapacity(int queueCapacity) {
        return queueCapacity;
    }
}
