package com.prorocketeers.bsc.payment.tracker.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import com.prorocketeers.bsc.payment.tracker.domain.Payment;

/**
 * Payment entry parser based on the regular expression verification.
 *
 * @see com.prorocketeers.bsc.payment.tracker.service.FilePaymentReader
 * @see com.prorocketeers.bsc.payment.tracker.service.InteractivePaymentReader
 */
public class RegexPaymentParser implements PaymentParser {

    private final Pattern linePattern = Pattern.compile("\\s*[A-Z]{3}\\s+[-]?\\d+\\.?\\d{0,2}\\s*");

    @Override
    public Payment fromString(String paymentLine) throws ParseException {

        if (paymentLine == null || !linePattern.matcher(paymentLine).matches()) {
            throw new ParseException("Invalid payment line format.", 0);
        }
        StringTokenizer st = new StringTokenizer(paymentLine);
        return Payment.builder()
                .currency(st.nextToken())
                .amount(new BigDecimal(st.nextToken()))
                .build();
    }

}
