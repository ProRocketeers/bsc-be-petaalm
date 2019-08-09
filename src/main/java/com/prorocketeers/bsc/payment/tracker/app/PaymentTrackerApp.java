package com.prorocketeers.bsc.payment.tracker.app;

import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.prorocketeers.bsc.payment.tracker.domain.LedgerEvent;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerEventType;
import com.prorocketeers.bsc.payment.tracker.domain.LedgerSummary;
import com.prorocketeers.bsc.payment.tracker.service.FilePaymentReader;
import com.prorocketeers.bsc.payment.tracker.service.InteractivePaymentReader;
import com.prorocketeers.bsc.payment.tracker.service.QueueSchedulerTask;
import com.prorocketeers.bsc.payment.tracker.service.InMemoryLedgerProcessor;
import com.prorocketeers.bsc.payment.tracker.service.LedgerSummaryStreamPrinter;
import com.prorocketeers.bsc.payment.tracker.service.RegexPaymentParser;
import com.prorocketeers.bsc.payment.tracker.util.ExecutorManager;

/**
 * Main Payment Tracker class. Holds "component wiring", boot-up and shutdown logic.
 * See README.md for usage instructions.
 */
public class PaymentTrackerApp {

    private static final long SUMMARY_PRINT_INTERVAL = 1;   //minutes
    private static final long SHUTDOWN_TIMEOUT = 10;        //seconds
    private static final int LEDGER_EVENT_QUEUE_CAPACITY = 1000;
    private static final int LEDGER_SUMMARY_QUEUE_CAPACITY = 5;
    private static final int WORKER_THREAD_POOL_CAPACITY = 3;
    private static final String VERBOSE_SYSTEM_PROPERTY = "payment.tracker.verbose";
    private static final String DEFAULT_VERBOSE_VALUE = "true";


    private final Path paymentLogFilePath;
    private final BlockingQueue<LedgerEvent> ledgerEventQueue;
    private final BlockingQueue<LedgerSummary> ledgerSummaryQueue;
    private final ExecutorService workerExecutor;
    private final ScheduledExecutorService schedulerExecutor;
    private final boolean verboseMode;


    public PaymentTrackerApp(String paymentLogFile) {
        paymentLogFilePath = paymentLogFile != null ? Path.of(paymentLogFile) : null;
        ledgerEventQueue = new ArrayBlockingQueue<>(LEDGER_EVENT_QUEUE_CAPACITY);
        ledgerSummaryQueue = new ArrayBlockingQueue<>(LEDGER_SUMMARY_QUEUE_CAPACITY);
        workerExecutor = Executors.newFixedThreadPool(WORKER_THREAD_POOL_CAPACITY);
        schedulerExecutor = Executors.newSingleThreadScheduledExecutor();
        verboseMode = Boolean.valueOf(System.getProperty(VERBOSE_SYSTEM_PROPERTY, DEFAULT_VERBOSE_VALUE));
    }

    public void run() {
        Future<?> ledger = startLedger();
        Future<?> reader = startPaymentReader();
        Future<?> printer = startSummaryPrinter();
        Future<?> scheduler = startScheduler();
        ExecutorManager.waitForCompletion(reader);
        try {
            ledgerEventQueue.put(LedgerEvent.builder().type(LedgerEventType.SHUTDOWN).build());
        } catch (InterruptedException e) {
            if (verboseMode) {
                System.err.println("Failed to shutdown the LedgerProcessor gracefully. Applying forced termination...");
            }
        }
        ExecutorManager.waitForCompletion(ledger, SHUTDOWN_TIMEOUT);
        ExecutorManager.shutdown(schedulerExecutor, workerExecutor, scheduler, printer);
    }

    /* No IOC framework - do the wiring work manually */
    private Future<?> startLedger() {
        Future<?> ledger = workerExecutor.submit(new InMemoryLedgerProcessor(ledgerEventQueue, ledgerSummaryQueue, System.err));
        if (paymentLogFilePath != null) {
            Future<?> fileReader = workerExecutor.submit(
                    new FilePaymentReader(ledgerEventQueue, paymentLogFilePath, System.err, new RegexPaymentParser(), System.out, verboseMode)
            );
            ExecutorManager.waitForCompletion(fileReader);
        }
        return ledger;
    }

    /* No IOC framework - do the wiring work manually */
    private Future<?> startPaymentReader() {
        return workerExecutor.submit(
                new InteractivePaymentReader(ledgerEventQueue, System.in, System.err, new RegexPaymentParser(), System.out, verboseMode)
        );
    }

    /* No IOC framework - do the wiring work manually */
    private Future<?> startSummaryPrinter() {
        return workerExecutor.submit(new LedgerSummaryStreamPrinter(ledgerSummaryQueue, System.out, verboseMode));
    }

    /* No IOC framework - do the wiring work manually */
    private Future<?> startScheduler() {
        return schedulerExecutor.scheduleWithFixedDelay(new QueueSchedulerTask(ledgerEventQueue, System.err), SUMMARY_PRINT_INTERVAL, SUMMARY_PRINT_INTERVAL, TimeUnit.MINUTES);
    }

    /**
     * Main application entry point
     * @param args the only recognized argument is a name of the payment log file.
     */
    public static void main(String[] args) {
        PaymentTrackerApp app = new PaymentTrackerApp(args.length > 0 ? args[0] : null);
        app.run();
    }

}
