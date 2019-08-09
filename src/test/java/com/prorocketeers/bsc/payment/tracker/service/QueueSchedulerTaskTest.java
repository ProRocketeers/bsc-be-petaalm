package com.prorocketeers.bsc.payment.tracker.service;

import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerEvent;

public class QueueSchedulerTaskTest {

    @Mock
    private BlockingQueue<LedgerEvent> ledgerEventQueue;
    @Mock
    private PrintStream errorStream;
    private QueueSchedulerTask task;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        task = new QueueSchedulerTask(ledgerEventQueue, errorStream);
    }

    @Test
    public void run_OK() throws InterruptedException {
        // setup
        Mockito.doNothing().when(ledgerEventQueue).put(Mockito.any(LedgerEvent.class));
        // execute
        task.run();
        // verify
        Mockito.verify(ledgerEventQueue).put(Mockito.any(LedgerEvent.class));
        Mockito.verifyZeroInteractions(errorStream);
    }

    @Test
    public void run_Failed() throws InterruptedException {
        // setup
        Mockito.doThrow(InterruptedException.class).when(ledgerEventQueue).put(Mockito.any(LedgerEvent.class));
        // execute
        task.run();
        // verify
        Mockito.verify(ledgerEventQueue).put(Mockito.any(LedgerEvent.class));
        Mockito.verify(errorStream).println(Mockito.anyString());
        Mockito.verify(errorStream).flush();
    }

}
