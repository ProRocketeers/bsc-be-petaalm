package com.prorocketeers.bsc.payment.tracker.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import lombok.experimental.UtilityClass;

/**
 * Utility class providing means of managing executor services and their tasks.
 */
@UtilityClass
public class ExecutorManager {

    public static void waitForCompletion(Future<?> future) {
        try {
            future.get();
        } catch (InterruptedException | ExecutionException ex) {
            future.cancel(true);
        }
    }

    public static void waitForCompletion(Future<?> future, long timeout) {
        try {
            future.get(timeout, TimeUnit.SECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            future.cancel(true);
        }
    }

    public static void shutdown(ScheduledExecutorService scheduledExecutorService, ExecutorService executorService, Future<?>...futures) {
        for (Future<?> future : futures) {
            future.cancel(true);
        }
        scheduledExecutorService.shutdown();
        executorService.shutdown();
    }

}
