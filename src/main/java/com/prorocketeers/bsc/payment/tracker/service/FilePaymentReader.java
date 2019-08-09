package com.prorocketeers.bsc.payment.tracker.service;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerEvent;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerEventType;

/**
 * Loads payment entries from a text file and submits them
 * to the ledger processor via event queue.
 */
public class FilePaymentReader implements PaymentReader {

    private final BlockingQueue<LedgerEvent> ledgerEventQueue;
    private final Path sourcePath;
    private final PaymentParser paymentParser;
    private final PrintStream errorStream;
    private final PrintStream printStream;
    private final boolean verboseMode;
    private int loaded;
    private int failed;

    public FilePaymentReader(
                BlockingQueue<LedgerEvent> ledgerEventQueue, Path sourcePath,
                PrintStream errorStream, PaymentParser paymentParser, PrintStream printStream, boolean verboseMode) {
        this.ledgerEventQueue = ledgerEventQueue;
        this.sourcePath = sourcePath;
        this.paymentParser = paymentParser;
        this.errorStream = errorStream;
        this.printStream = printStream;
        this.verboseMode = verboseMode;
    }

    @Override
    public void run() {
        verboseMessage(() -> "Loading payment entries from file \"" + sourcePath + "\"");
        try (Stream<String> lines = Files.lines(sourcePath)) {
            lines.forEachOrdered(line -> parseAndPost(line));
        } catch (IOException ex) {
            errorStream.println("Failed to read input from file \"" + ex.getMessage() + "\"");
            errorStream.flush();
        }
        verboseMessage(() -> "Entries loaded: " + loaded);
        verboseMessage(() -> "Entries failed: " + failed);
    }

    private void parseAndPost(String line) {
        try {
            ledgerEventQueue.put(
                    LedgerEvent.builder()
                            .type(LedgerEventType.ADD_ENTRY)
                            .payload(paymentParser.fromString(line))
                            .build()
            );
            loaded++;
        } catch (ParseException | InterruptedException  ex) {
            errorStream.println("Failed to parse and post line: " + line + System.lineSeparator() + "Error: " + ex.getMessage());
            errorStream.flush();
            failed++;
        }
    }

    private void verboseMessage(Supplier<String> messageSupplier) {
        if (verboseMode) {
            printStream.println(messageSupplier.get());
        }
    }

}
