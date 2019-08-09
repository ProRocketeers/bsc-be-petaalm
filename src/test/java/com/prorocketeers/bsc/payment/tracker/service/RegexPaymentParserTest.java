package com.prorocketeers.bsc.payment.tracker.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.text.ParseException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.prorocketeers.bsc.payment.tracker.domain.Payment;

public class RegexPaymentParserTest {

    private RegexPaymentParser parser;

    @BeforeEach
    public void setUp() {
        parser = new RegexPaymentParser();
    }

    @ParameterizedTest
    @CsvSource({
            "USD 23, USD, 23",
            "CZK -60, CZK, -60",
            "NOK 100., NOK, 100",
            "SEK 50.5, SEK, 50.5",
            "EUR -0.95, EUR, -0.95",
            "USD    23, USD, 23",
            "'  USD 23', USD, 23",
            "' USD 23 ', USD, 23"
    })
    public void fromText_givenValidInput(String line, String currency, BigDecimal amount) throws ParseException {
        Payment payment = parser.fromString(line);
        assertNotNull(payment);
        assertEquals(currency, payment.getCurrency());
        assertEquals(amount, payment.getAmount());
    }

    @ParameterizedTest
    @ValueSource(strings = { "USD ", "USD .", "USD -", "USDCZK 23", "usd 23", "USD 23,3", "USD 23.125", "US  23", "US% 23", "", "  "})
    public void fromText_givenInvalidInput(String line) {
        assertThrows(ParseException.class, () ->
            parser.fromString(line)
        );
    }

}
