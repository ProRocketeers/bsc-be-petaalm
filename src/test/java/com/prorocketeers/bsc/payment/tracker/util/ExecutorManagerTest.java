package com.prorocketeers.bsc.payment.tracker.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ExecutorManagerTest {

    @Mock
    private Future<?> future;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void waitForCompletion() throws InterruptedException, ExecutionException {
        // setup - nothing to be done here
        // execute
        ExecutorManager.waitForCompletion(future);
        // verify
        Mockito.verify(future).get();
        Mockito.verifyNoMoreInteractions(future);
    }

    @ParameterizedTest
    @ValueSource(classes = {InterruptedException.class, ExecutionException.class})
    public void waitForCompletion_givenFutureWithException(Class<? extends Exception> clazz) throws InterruptedException, ExecutionException {
        // setup
        Mockito.doThrow(clazz).when(future).get();
        // execute
        ExecutorManager.waitForCompletion(future);
        // verify
        Mockito.verify(future).get();
        Mockito.verify(future).cancel(true);
    }

    @Test
    public void waitForCompletionWithTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        // setup - nothing to be done here
        // execute
        ExecutorManager.waitForCompletion(future, 1L);
        // verify
        Mockito.verify(future).get(1L, TimeUnit.SECONDS);
        Mockito.verifyNoMoreInteractions(future);
    }

    @ParameterizedTest
    @ValueSource(classes = {InterruptedException.class, ExecutionException.class, TimeoutException.class})
    public void waitForCompletionWithTimeout_givenFutureWithException(Class<? extends Exception> clazz) throws InterruptedException, ExecutionException, TimeoutException {
        // setup
        Mockito.doThrow(clazz).when(future).get(1L, TimeUnit.SECONDS);
        // execute
        ExecutorManager.waitForCompletion(future, 1L);
        // verify
        Mockito.verify(future).get(1L, TimeUnit.SECONDS);
        Mockito.verify(future).cancel(true);
    }

    @Test
    public void shutdown() {
        // setup
        ScheduledExecutorService scheduledExecutorService = Mockito.mock(ScheduledExecutorService.class);
        ExecutorService executorService = Mockito.mock(ExecutorService.class);
        // execute
        ExecutorManager.shutdown(scheduledExecutorService, executorService, future);
        // verify
        Mockito.verify(future).cancel(true);
        Mockito.verify(scheduledExecutorService).shutdown();
        Mockito.verify(executorService).shutdown();
        Mockito.verifyNoMoreInteractions(scheduledExecutorService, executorService, future);
    }

}
