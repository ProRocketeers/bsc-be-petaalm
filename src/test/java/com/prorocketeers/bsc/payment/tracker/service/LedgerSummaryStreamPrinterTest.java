package com.prorocketeers.bsc.payment.tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerSummary;

public class LedgerSummaryStreamPrinterTest {

    private LedgerSummaryStreamPrinter printer;
    @Mock
    private BlockingQueue<LedgerSummary> queue;
    @Mock
    private PrintStream printStream;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        printer = new LedgerSummaryStreamPrinter(queue, printStream, false);
    }

    @Test
    public void runLoop() throws InterruptedException {
        when(queue.take())
            .thenReturn(new LedgerSummary(Collections.emptyMap(), LocalDateTime.now()))
            .thenThrow(new InterruptedException());
        printer.run();
        verify(printStream).print(anyString());
        verify(printStream).flush();
        verifyNoMoreInteractions(printStream);
    }

    @Test
    public void prettyPrint() {
        final Map<String, BigDecimal> ledgerMap = Map.of(
            "USD", new BigDecimal("55.0"),
            "CZK", new BigDecimal("-100"),
            "EUR", new BigDecimal("100.")
        );
        final LocalDateTime now = LocalDateTime.now();
        final String expected = "CZK -100" + System.lineSeparator() + "EUR 100" + System.lineSeparator() + "USD 55.0" + System.lineSeparator();
        assertEquals(expected, printer.prettyPrint(new LedgerSummary(ledgerMap, now)));
    }

}
