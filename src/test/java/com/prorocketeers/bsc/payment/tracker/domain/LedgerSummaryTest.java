package com.prorocketeers.bsc.payment.tracker.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LedgerSummaryTest {

    @ParameterizedTest
    @MethodSource("provideAmountsForCurrencies")
    public void getAmountForCurrency(LedgerSummary ledgerSummary, String currency, BigDecimal amount) {
        assertEquals(amount, ledgerSummary.getAmountForCurrency(currency));
    }

    static Stream<Arguments> provideAmountsForCurrencies() {
        final LedgerSummary ledgerSummary = new LedgerSummary(Map.of("USD", BigDecimal.TEN), LocalDateTime.now());
        return Stream.of(
            arguments(ledgerSummary, "USD", BigDecimal.TEN),
            arguments(ledgerSummary, "CZK", BigDecimal.ZERO)
        );
    }

    @ParameterizedTest
    @MethodSource("provideLedgerSummaries")
    public void getNonZeroEntries(LedgerSummary ledgerSummary, Map<String, BigDecimal> nonZeroEntries) {
        assertEquals(nonZeroEntries, ledgerSummary.getNonZeroEntries());
    }

    static Stream<Arguments> provideLedgerSummaries() {
        return Stream.of(
            arguments(
                new LedgerSummary(Map.of("USD", BigDecimal.TEN, "CZK", BigDecimal.ZERO), LocalDateTime.now()),
                Map.of("USD", BigDecimal.TEN)
            ),
            arguments(
                new LedgerSummary(Map.of("USD", BigDecimal.TEN, "CZK", BigDecimal.ONE), LocalDateTime.now()),
                Map.of("USD", BigDecimal.TEN, "CZK", BigDecimal.ONE)
            ),
            arguments(
                new LedgerSummary(Map.of("USD", BigDecimal.TEN, "CZK", new BigDecimal("0.00")), LocalDateTime.now()),
                Map.of("USD", BigDecimal.TEN)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideLedgerUpdateTimes")
    public void getLastUpdateTime(LedgerSummary ledgerSummary, LocalDateTime lastUpdateTime) {
        assertEquals(lastUpdateTime, ledgerSummary.getLastUpdateTime());
    }

    static Stream<Arguments> provideLedgerUpdateTimes() {
        final LocalDateTime now = LocalDateTime.now();
        final Map<String, BigDecimal> currencyBalance = Map.of("USD", BigDecimal.TEN);
        return Stream.of(
                arguments(new LedgerSummary(currencyBalance, now), now),
                arguments(new LedgerSummary(currencyBalance, null), null)
        );
    }
}
