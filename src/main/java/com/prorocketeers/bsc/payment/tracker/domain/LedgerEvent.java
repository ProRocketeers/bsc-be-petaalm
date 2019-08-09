package com.prorocketeers.bsc.payment.tracker.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Immutable representation of an event structure which is used
 * to deliver messages to LedgerProcessor implementation.
 *
 * @see com.prorocketeers.bsc.payment.tracker.service.LedgerProcessor
 */
@Builder
@Getter
@EqualsAndHashCode
public class LedgerEvent {

    private final LedgerEventType type;
    private final LocalDateTime dispatchTime;
    private final LedgerEventPayload payload;

}
