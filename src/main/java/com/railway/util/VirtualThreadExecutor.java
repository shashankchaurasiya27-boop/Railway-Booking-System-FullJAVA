package com.railway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Virtual Thread Executor utility for Java 21.
 * Routes blocking I/O operations through virtual threads for improved
 * scalability.
 * 
 * Usage: VirtualThreadExecutor.execute(() -> blockingOperation());
 */
public class VirtualThreadExecutor {
    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadExecutor.class);

    // Virtual thread executor - creates a new virtual thread for each task
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    static {
        logger.info("Virtual Thread Executor initialized (Java 21)");

        // Register shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(() -> {
            logger.info("Shutting down Virtual Thread Executor");
            executor.shutdown();
        }));
    }

    /**
     * Execute a blocking operation on a virtual thread and wait for result.
     * Maintains synchronous semantics from caller's perspective.
     * 
     * @param task The blocking operation to execute
     * @param <T>  Return type
     * @return Result of the operation
     * @throws RuntimeException if execution fails
     */
    public static <T> T execute(Callable<T> task) {
        try {
            Future<T> future = executor.submit(task);
            return future.get();
        } catch (Exception e) {
            logger.error("Virtual thread execution failed", e);
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException("Virtual thread execution failed", e);
        }
    }

    /**
     * Execute a blocking operation without return value.
     * 
     * @param task The blocking operation to execute
     */
    public static void executeVoid(Runnable task) {
        try {
            Future<?> future = executor.submit(task);
            future.get();
        } catch (Exception e) {
            logger.error("Virtual thread execution failed", e);
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException("Virtual thread execution failed", e);
        }
    }

    /**
     * Check if current thread is a virtual thread.
     * Useful for debugging and verification.
     * 
     * @return true if running on a virtual thread
     */
    public static boolean isVirtualThread() {
        return Thread.currentThread().isVirtual();
    }

    /**
     * Log current thread info for debugging.
     */
    public static void logThreadInfo() {
        Thread t = Thread.currentThread();
        logger.debug("Thread: {} | Virtual: {} | ID: {}",
                t.getName(), t.isVirtual(), t.threadId());
    }
}
