package com.prorocketeers.bsc.payment.tracker.domain;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Immutable representation of a LedgerEventPayload for
 * for LedgerEventType.ADD_ENTRY events.
 *
 * @see com.prorocketeers.bsc.payment.tracker.domain.LedgerEventType
 * @see com.prorocketeers.bsc.payment.tracker.domain.LedgerEventPayload
 */
@Builder
@Getter
@EqualsAndHashCode
public class Payment implements LedgerEventPayload {

    private final String currency;
    private final BigDecimal amount;

}
