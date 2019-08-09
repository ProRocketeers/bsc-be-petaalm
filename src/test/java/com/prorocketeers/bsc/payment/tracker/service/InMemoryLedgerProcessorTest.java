package com.prorocketeers.bsc.payment.tracker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerEntry;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerEvent;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerEventType;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerSummary;
import com.prorocketeers.bsc.payment.tracker.domain.Payment;

public class InMemoryLedgerProcessorTest {

    @Mock
    private BlockingQueue<LedgerEvent> inputQueue;
    @Mock
    private BlockingQueue<LedgerSummary> outputQueue;
    @Mock
    private PrintStream errorStream;

    private InMemoryLedgerProcessor processor;

    private LocalDateTime now;
    private LedgerEvent shutdownEvent;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        processor = new InMemoryLedgerProcessor(inputQueue, outputQueue, errorStream);
        now = LocalDateTime.of(2019, 4, 22, 15, 0);
        shutdownEvent = LedgerEvent.builder().type(LedgerEventType.SHUTDOWN).dispatchTime(now.plusMinutes(1)).build();
    }

    @Test
    public void runLoop_givenAddEntryEvent() throws InterruptedException {
        // setup
        final LedgerEvent event = createAddEntryEvent();
        when(inputQueue.take()).thenReturn(event).thenReturn(shutdownEvent);
        // execute
        processor.run();
        // verify
        verify(inputQueue, times(2)).take();
        verifyNoMoreInteractions(inputQueue, outputQueue, errorStream);
    }

    @Test
    public void runLoop_givenPostSummaryEvent() throws InterruptedException {
        // setup
        final LedgerEvent event = LedgerEvent.builder().type(LedgerEventType.POST_SUMMARY).dispatchTime(now).build();
        when(inputQueue.take()).thenReturn(event).thenReturn(shutdownEvent);
        // execute
        processor.run();
        // verify
        verify(inputQueue, times(2)).take();
        verify(outputQueue).put(any(LedgerSummary.class));
        verifyNoMoreInteractions(inputQueue, outputQueue, errorStream);
    }

    @Test
    public void runLoop_givenPostSummaryEventFailed() throws InterruptedException {
        // setup
        final LedgerEvent event = LedgerEvent.builder().type(LedgerEventType.POST_SUMMARY).dispatchTime(now).build();
        when(inputQueue.take()).thenReturn(event).thenReturn(shutdownEvent);
        doThrow(new InterruptedException()).when(outputQueue).put(any(LedgerSummary.class));
        // execute
        processor.run();
        // verify
        verify(inputQueue, times(2)).take();
        verify(outputQueue).put(any(LedgerSummary.class));
        verify(errorStream).println(anyString());
        verifyNoMoreInteractions(inputQueue, outputQueue, errorStream);
    }

    @Test
    public void runLoop_givenGetNextEventFailed() throws InterruptedException {
        // setup
        when(inputQueue.take()).thenThrow(new InterruptedException()).thenReturn(shutdownEvent);
        // execute
        processor.run();
        // verify
        verify(inputQueue, times(2)).take();
        verify(errorStream).println(anyString());
        verifyNoMoreInteractions(inputQueue, outputQueue, errorStream);
    }

    @Test
    public void mapToLedgerEntry() {
        // setup
        final LedgerEvent event = createAddEntryEvent();
        // execute
        final LedgerEntry entry = processor.mapToLedgerEntry(event);
        // verify
        final Payment payment = (Payment)event.getPayload();
        assertEquals(payment.getAmount(), entry.getAmount());
        assertEquals(payment.getCurrency(), entry.getCurrency());
        assertEquals(event.getDispatchTime(), entry.getDispatchTime());
        assertNotNull(entry.getBookingTime());
        assertTrue(LocalDateTime.now().isAfter(entry.getBookingTime()));
    }

    private LedgerEvent createAddEntryEvent() {
        final Payment payment = Payment.builder()
                .currency("USD")
                .amount(BigDecimal.TEN)
                .build();
        return LedgerEvent.builder()
                .type(LedgerEventType.ADD_ENTRY)
                .dispatchTime(now)
                .payload(payment)
                .build();
    }
}
