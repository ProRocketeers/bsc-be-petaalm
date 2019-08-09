package com.prorocketeers.bsc.payment.tracker.domain;

/**
 * Event types which must be handled by a LedgerProcessor
 * implementation
 *
 * @see com.prorocketeers.bsc.payment.tracker.service.LedgerProcessor
 */
public enum LedgerEventType {
    ADD_ENTRY,
    POST_SUMMARY,
    SHUTDOWN
}
