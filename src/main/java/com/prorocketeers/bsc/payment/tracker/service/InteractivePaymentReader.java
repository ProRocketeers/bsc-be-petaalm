package com.prorocketeers.bsc.payment.tracker.service;

import java.io.InputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerEvent;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerEventType;

/**
 *  Loads payment entries from an input stream (e.g. console) and submits them
 *  to the ledger processor via event queue.
 */
public class InteractivePaymentReader implements PaymentReader {

    private static final String STOP_WORD = "quit";
    private static final String VERBOSE_PROMPT = "Enter new payment entry or type \"quit\" to stop the application:";
    private final BlockingQueue<LedgerEvent> ledgerEventQueue;
    private final InputStream inputStream;
    private final PrintStream errorStream;
    private final PrintStream printStream;
    private final PaymentParser paymentParser;
    private final boolean verboseMode;

    public InteractivePaymentReader(
            BlockingQueue<LedgerEvent> ledgerEventQueue, InputStream inputStream,
            PrintStream errorStream, PaymentParser paymentParser, PrintStream printStream, boolean verboseMode) {
        this.ledgerEventQueue = ledgerEventQueue;
        this.inputStream = inputStream;
        this.errorStream = errorStream;
        this.printStream = printStream;
        this.paymentParser = paymentParser;
        this.verboseMode = verboseMode;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(inputStream);
        verboseMessage(VERBOSE_PROMPT);
        AtomicReference<String> input = new AtomicReference<>(scanner.nextLine());
        while (!STOP_WORD.equals(input.get())) {
            parseAndPost(input.get());
            verboseMessage(VERBOSE_PROMPT);
            input.set(scanner.nextLine());
        }
    }

    private void parseAndPost(String line) {
        try {
            ledgerEventQueue.put(
                LedgerEvent.builder()
                    .type(LedgerEventType.ADD_ENTRY)
                    .payload(paymentParser.fromString(line))
                    .build()
            );
        } catch (ParseException | InterruptedException  ex) {
            errorStream.println("Failed to parse and post line: " + line + System.lineSeparator() + "Error: " + ex.getMessage());
            errorStream.flush();
        }
    }

    private void verboseMessage(String message) {
        if (verboseMode) {
            printStream.println(message);
        }
    }

}
