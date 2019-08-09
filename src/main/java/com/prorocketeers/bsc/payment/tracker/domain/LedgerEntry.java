package com.prorocketeers.bsc.payment.tracker.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Immutable representation of an entry in the ledger.
 * @see com.prorocketeers.bsc.payment.tracker.domain.Ledger
 */
@Getter
@Builder
@EqualsAndHashCode
public class LedgerEntry {

    private final String currency;
    private final BigDecimal amount;
    private final LocalDateTime dispatchTime;
    private final LocalDateTime bookingTime;

}
