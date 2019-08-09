package com.prorocketeers.bsc.payment.tracker.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable representation of a ledger summary (i.e. list of all currency balances
 * currently kept in the ledger) at the given time.
 *
 * @see com.prorocketeers.bsc.payment.tracker.domain.Ledger
 */
public class LedgerSummary {

    private final LocalDateTime lastUpdateTime;
    private final Map<String, BigDecimal> currencyBalance;

    public LedgerSummary(Map<String, BigDecimal> currencyBalance, LocalDateTime lastUpdateTime) {
        this.currencyBalance = new HashMap<>(currencyBalance);
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * Retrieves amount associated with given currency code using
     * zero as the default value in case no value for the currency
     * is present.
     */
    public BigDecimal getAmountForCurrency(String currency) {
        return currencyBalance.getOrDefault(currency, BigDecimal.ZERO);
    }

    /**
     * Returns a new Map instance holding only currency / amount
     * entries where the amount associated with the value differs
     * from zero (and isn't null).
     */
    public Map<String, BigDecimal> getNonZeroEntries() {
        Map<String, BigDecimal> result = new HashMap<>();
        for (String currency : currencyBalance.keySet()) {
            BigDecimal value = getAmountForCurrency(currency);
            if (BigDecimal.ZERO.compareTo(value) != 0) {
                result.put(currency, value);
            }
        }
        return result;
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

}
