package com.prorocketeers.bsc.payment.tracker.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LedgerTest {

    @ParameterizedTest
    @MethodSource("ledgerProvider")
    public void getLedgerSummary(Ledger ledger, LocalDateTime lastUpdateTime) {
        final LedgerSummary summary = ledger.getLedgerSummary();
        assertEquals(lastUpdateTime, summary.getLastUpdateTime());
    }

    static Stream<Arguments> ledgerProvider() {
        final LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                arguments(new Ledger(), null),
                arguments(new Ledger().addLedgerEntry(LedgerEntry.builder().bookingTime(now).currency("USD").amount(BigDecimal.TEN).build()), now)
        );
    }
}
