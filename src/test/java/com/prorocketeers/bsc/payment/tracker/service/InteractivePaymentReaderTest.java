package com.prorocketeers.bsc.payment.tracker.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerEvent;

public class InteractivePaymentReaderTest {

    private static final String STOP_WORD = "quit";
    private static final String PAYMENT_LINE = "USD 200";
    private static final String INVALID_PAYMENT_LINE = "US 200";

    @Mock
    private BlockingQueue<LedgerEvent> queue;
    @Mock
    private PrintStream errorStream;
    @Mock
    private PaymentParser parser;
    @Mock
    private PrintStream printStream;

    private boolean verbose = false;

    private InteractivePaymentReader reader;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void runLoop_givenCorrectPayment() throws InterruptedException, ParseException {
        // setup
        reader = new InteractivePaymentReader(queue, getPaymentLineStream(PAYMENT_LINE, STOP_WORD), errorStream, parser, printStream, verbose);
        // execute
        reader.run();
        // verify
        verify(parser).fromString(PAYMENT_LINE);
        verify(queue).put(any(LedgerEvent.class));
        verifyNoMoreInteractions(parser, queue, errorStream);
    }

    @Test
    public void runLoop_givenInvalidPayment() throws InterruptedException, ParseException {
        // setup
        reader = new InteractivePaymentReader(
                queue, getPaymentLineStream(INVALID_PAYMENT_LINE, PAYMENT_LINE, STOP_WORD), errorStream, parser, printStream, verbose);
        when(parser.fromString(INVALID_PAYMENT_LINE)).thenThrow(new ParseException("runLoop_givenInvalidPayment unit test", 0));
        // execute
        reader.run();
        // verify
        // should parse two lines
        verify(parser).fromString(INVALID_PAYMENT_LINE);
        verify(parser).fromString(PAYMENT_LINE);
        // should submit one event
        verify(queue).put(any(LedgerEvent.class));
        // should report one parsing failure
        verify(errorStream).println(anyString());
        verify(errorStream).flush();
        verifyNoMoreInteractions(parser, queue, errorStream);
    }

    @Test
    public void runLoop_givenQuit() {
        // setup
        reader = new InteractivePaymentReader(queue, getPaymentLineStream(STOP_WORD), errorStream, parser, printStream, verbose);
        // execute
        reader.run();
        // verify
        verifyZeroInteractions(parser);
        verifyZeroInteractions(queue);
        verifyZeroInteractions(errorStream);
    }

    private ByteArrayInputStream getPaymentLineStream(String ... lines) {
        return new ByteArrayInputStream(String.join(System.lineSeparator(), lines).getBytes());
    }

}
