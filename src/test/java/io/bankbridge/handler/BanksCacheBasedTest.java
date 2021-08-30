package io.bankbridge.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Request;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


class BanksCacheBasedTest {
    private static String BANKS_IN_JSON;

    private static final BanksCacheBased banksCacheBased = new BanksCacheBased();

    @BeforeEach
    void setUp() throws IOException {
        banksCacheBased.init();

        BANKS_IN_JSON = new ObjectMapper().readValue(
                Thread.currentThread().getContextClassLoader().getResource("banks-v1.json"), String.class);

    }

    @Test
    void handle() {
// I don't know, how cast spark Request to HttpRequest:)
//        assertEquals(BANKS_IN_JSON, banksCacheBased.handle());
    }

    @Test
    void filter() {
//        assertEquals(BANKS_IN_JSON, banksCacheBased.filter(filterBanksReq, filterBanksResp));
    }
}