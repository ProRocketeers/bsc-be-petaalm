package com.prorocketeers.bsc.payment.tracker.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerEvent;

public class FilePaymentReaderTest {

    private static final String PAYMENT_LINE_1 = "USD 200";
    private static final String PAYMENT_LINE_2 = "CZK -200";
    private static final String INVALID_PAYMENT_LINE = "US 200";

    @Mock
    private BlockingQueue<LedgerEvent> queue;
    @Mock
    private PrintStream errorStream;
    @Mock
    private PaymentParser parser;
    @Mock
    private PrintStream printStream;

    private FilePaymentReader reader;

    private boolean verbose = false;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void runLoop_givenCorrectPayment() throws InterruptedException, ParseException, URISyntaxException {
        // setup
        reader = new FilePaymentReader(queue, getPaymentFilePath("runLoop_givenCorrectPayment"), errorStream, parser, printStream, verbose);
        // execute
        reader.run();
        // verify
        // should parse two lines
        verify(parser).fromString(PAYMENT_LINE_1);
        verify(parser).fromString(PAYMENT_LINE_2);
        // should submit two events
        verify(queue, times(2)).put(any(LedgerEvent.class));
        verifyNoMoreInteractions(parser, queue, errorStream);
    }

    @Test
    public void runLoop_givenInvalidPayment() throws InterruptedException, ParseException, URISyntaxException {
        // setup
        reader = new FilePaymentReader(queue, getPaymentFilePath("runLoop_givenInvalidPayment"), errorStream, parser, printStream, verbose);
        when(parser.fromString(INVALID_PAYMENT_LINE)).thenThrow(new ParseException("runLoop_givenInvalidPayment unit test", 0));
        // execute
        reader.run();
        // verify
        // should parse 3 lines
        verify(parser).fromString(PAYMENT_LINE_1);
        verify(parser).fromString(INVALID_PAYMENT_LINE);
        verify(parser).fromString(PAYMENT_LINE_2);
        // should submit 2 two events
        verify(queue, times(2)).put(any(LedgerEvent.class));
        // should report one parsing failure
        verify(errorStream).println(anyString());
        verify(errorStream).flush();
        verifyNoMoreInteractions(parser, queue, errorStream);
    }

    @Test
    public void runLoop_givenEmptyFile() throws URISyntaxException {
        // setup
        reader = new FilePaymentReader(queue, getPaymentFilePath("runLoop_givenEmptyFile"), errorStream, parser, printStream, verbose);
        // execute
        reader.run();
        // verify
        verifyZeroInteractions(parser);
        verifyZeroInteractions(queue);
        verifyZeroInteractions(errorStream);
    }

    @Test
    public void runLoop_givenNonExistentFile() throws URISyntaxException {
        // setup
        reader = new FilePaymentReader(queue, Path.of("runLoop_givenNonExistentFile"), errorStream, parser, printStream, verbose);
        // execute
        reader.run();
        // verify
        verifyZeroInteractions(parser);
        verifyZeroInteractions(queue);
        // should report file read failure
        verify(errorStream).println(anyString());
        verify(errorStream).flush();
        verifyNoMoreInteractions(errorStream);
    }

    private Path getPaymentFilePath(String testName) throws URISyntaxException {
        final String resourceFileName = "file_payment_reader_" + testName + ".txt";
        return Path.of(getClass().getClassLoader().getResource(resourceFileName).toURI());
    }

}
