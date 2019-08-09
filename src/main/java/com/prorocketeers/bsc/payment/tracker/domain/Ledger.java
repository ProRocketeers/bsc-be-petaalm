package com.prorocketeers.bsc.payment.tracker.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic representation of an in-memory ledger. Please note that it's not
 * thread-safe as it's meant to be used by a LedgerProcessor implementation
 * which should be responsible for keeping its Ledger instance private
 * and handling the thread-safety.
 *
 * @see com.prorocketeers.bsc.payment.tracker.service.LedgerProcessor
 */
public class Ledger {

    private final List<LedgerEntry> entryLog;
    private final Map<String, BigDecimal> currencyBalances;

    public Ledger() {
        entryLog = new ArrayList<>();
        currencyBalances = new HashMap<>();
    }

    public Ledger addLedgerEntry(LedgerEntry ledgerEntry) {
        entryLog.add(ledgerEntry);
        final String currency = ledgerEntry.getCurrency();
        currencyBalances.put(currency, currencyBalances.getOrDefault(currency, BigDecimal.ZERO).add(ledgerEntry.getAmount()));
        return this;
    }

    public LedgerSummary getLedgerSummary() {
        LocalDateTime lastUpdateTime = entryLog.isEmpty() ? null : entryLog.get(entryLog.size() - 1).getBookingTime();
        return new LedgerSummary(currencyBalances, lastUpdateTime);
    }

}
