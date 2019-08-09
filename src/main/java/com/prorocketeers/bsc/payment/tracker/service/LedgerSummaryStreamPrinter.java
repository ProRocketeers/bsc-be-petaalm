package com.prorocketeers.bsc.payment.tracker.service;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerSummary;

/**
 * Simple implementation of LedgerSummaryPrinter. Consumes LedgerSummary entries
 * from the input BlockingQueue. This implementation is supposed to be run as
 * a single instance (in it's own Thread) within an application.
 */
public class LedgerSummaryStreamPrinter implements LedgerSummaryPrinter {

    private static final String VERBOSE_HEADER = "%n========== Ledger summary ==========%n%nLast update: %tF %tT%n------------------------------------";
    private static final String VERBOSE_FOOTER = String.format("------------------------------------%n");
    private static final String VERBOSE_PROMPT = "Enter new payment entry or type \"quit\" to stop the application:";

    private final BlockingQueue<LedgerSummary> ledgerSummaryQueue;
    private final PrintStream printStream;
    private final boolean verboseMode;
    private boolean keepRunning;

    public LedgerSummaryStreamPrinter(BlockingQueue<LedgerSummary> ledgerSummaryQueue, PrintStream printStream, boolean verboseMode) {
        this.ledgerSummaryQueue = ledgerSummaryQueue;
        this.printStream = printStream;
        this.verboseMode = verboseMode;
        this.keepRunning = true;
    }

    @Override
    public void run() {
        while (keepRunning) {
            try {
                LedgerSummary ledgerSummary = ledgerSummaryQueue.take();
                verboseMessage(() -> String.format(VERBOSE_HEADER, ledgerSummary.getLastUpdateTime(), ledgerSummary.getLastUpdateTime()));
                printStream.print(prettyPrint(ledgerSummary));
                verboseMessage(() -> VERBOSE_FOOTER);
                printStream.flush();
            } catch (InterruptedException e) {
                keepRunning = false;
            }
            verboseMessage(() -> VERBOSE_PROMPT);
        }
    }

    protected String prettyPrint(LedgerSummary ledgerSummary) {
        Map<String, BigDecimal> entries = ledgerSummary.getNonZeroEntries();
        List<String> currencies = new ArrayList<>(entries.keySet());
        currencies.sort(null);
        StringBuilder result = new StringBuilder();
        for (String currency : currencies) {
            result.append(currency).append(" ").append(entries.get(currency)).append(System.lineSeparator());
        }
        return result.toString();
    }

    private void verboseMessage(Supplier<String> messageSupplier) {
        if (verboseMode) {
            printStream.println(messageSupplier.get());
        }
    }

}
