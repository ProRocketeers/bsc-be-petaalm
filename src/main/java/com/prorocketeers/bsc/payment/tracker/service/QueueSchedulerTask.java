package com.prorocketeers.bsc.payment.tracker.service;

import java.io.PrintStream;
import java.util.concurrent.BlockingQueue;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerEvent;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerEventType;

/**
 * Simple task sending a POST_SUMMARY event to the LedgerProcessor. It's meant
 * to be triggered regularly by a scheduler.
 */
public class QueueSchedulerTask implements SchedulerTask {

    private final BlockingQueue<LedgerEvent> ledgerEventQueue;
    private final PrintStream errorStream;

    public QueueSchedulerTask(BlockingQueue<LedgerEvent> ledgerEventQueue, PrintStream errorStream) {
        this.ledgerEventQueue = ledgerEventQueue;
        this.errorStream = errorStream;
    }

    @Override
    public void run() {
        try {
            ledgerEventQueue.put(LedgerEvent.builder().type(LedgerEventType.POST_SUMMARY).build());
        } catch (InterruptedException e) {
            errorStream.println("Failed to submit the \"POST_SUMMARY\" event.");
            errorStream.flush();
        }
    }

}
