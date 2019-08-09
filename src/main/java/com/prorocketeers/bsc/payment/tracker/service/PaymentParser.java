package com.prorocketeers.bsc.payment.tracker.service;

import java.text.ParseException;

import com.prorocketeers.bsc.payment.tracker.domain.Payment;

/**
 * API specification for an implementation of "payment line parser".
 */
public interface PaymentParser {

    Payment fromString(String paymentLine) throws ParseException;
}
