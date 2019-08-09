package com.prorocketeers.bsc.payment.tracker.service;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

import com.prorocketeers.bsc.payment.tracker.domain.Ledger;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerEntry;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerEvent;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerSummary;
import com.prorocketeers.bsc.payment.tracker.domain.Payment;

/**
 * This implementation of the LedgerProcessor keeps its own private
 * copy of a ledger in the memory. It's meant to be run as an exclusive
 * consumer of the LedgerEvents from the inbound BlockingQueue.
 *
 * Therefore the only recommended way of use is as a singleton.
 */
public class InMemoryLedgerProcessor implements LedgerProcessor {

    private final BlockingQueue<LedgerEvent> inputQueue;
    private final BlockingQueue<LedgerSummary> outputQueue;
    private final PrintStream errorStream;
    private final Ledger ledger;
    private boolean keepRunning;

    public InMemoryLedgerProcessor(BlockingQueue<LedgerEvent> inputQueue, BlockingQueue<LedgerSummary> outputQueue, PrintStream errorStream) {
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.errorStream = errorStream;
        this.ledger = new Ledger();
        this.keepRunning = true;
    }

    @Override
    public void run() {
        while (keepRunning) {
            final LedgerEvent event = getNextEvent();
            if (event == null) {
                continue;
            }
            switch (event.getType()) {
                case ADD_ENTRY:
                    ledger.addLedgerEntry(mapToLedgerEntry(event));
                    break;
                case POST_SUMMARY:
                    postLedgerSummary();
                    break;
                case SHUTDOWN:
                    keepRunning = false;
                    break;
            }
        }
    }

    private LedgerEvent getNextEvent() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            errorStream.println("Failed to retrieve ledger event.");
            return null;
        }
    }

    //TODO: could be moved to a separate class...
    protected LedgerEntry mapToLedgerEntry(LedgerEvent event) {
        final Payment payment = (Payment)event.getPayload();
        return LedgerEntry.builder()
            .currency(payment.getCurrency())
            .amount(payment.getAmount())
            .dispatchTime(event.getDispatchTime())
            .bookingTime(LocalDateTime.now())
            .build();
    }

    private void postLedgerSummary() {
        try {
            outputQueue.put(ledger.getLedgerSummary());
        } catch (InterruptedException e) {
            errorStream.println("Failed to post ledger summary event.");
        }
    }

}
