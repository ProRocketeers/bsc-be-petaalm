package com.prorocketeers.bsc.payment.tracker.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Immutable representation of a ledger summary (i.e. list of all currency balances
 * currently kept in the ledger) at the given time.
 *
 * @see com.prorocketeers.bsc.payment.tracker.domain.Ledger
 */
public class LedgerSummary {

    @Getter
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
        Predicate<Map.Entry<String, BigDecimal>> isNotZeroOrNull = entry ->
            BigDecimal.ZERO.compareTo(Optional.ofNullable(entry.getValue()).orElse(BigDecimal.ZERO)) != 0;

        return currencyBalance.entrySet()
            .stream()
            .filter(isNotZeroOrNull)
            .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
