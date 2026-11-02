package com.marvel.hospitality.stub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * All settings live under the "stub.wiremock" prefix in application.yml.
 */
@ConfigurationProperties(prefix = "stub.wiremock")
public record WireMockProperties(
        /* HTTP port the stubbed third-party APIs listen on (0 = random). */
        int port,
        /* Optional HTTPS port (-1 / 0 disables). */
        int httpsPort,
        /* External folder holding mappings/ and __files/. Empty = load from classpath:wiremock. */
        String rootDir,
        /* Enable Handlebars response templating for every stub. */
        boolean globalTemplating,
        /* Log every request/response match at INFO. */
        boolean verbose,
        /* Max request journal entries kept in memory (avoid OOM on long runs). */
        int maxRequestJournalEntries,
        /*
         * Optional pass-through: unmatched requests whose URL starts with the key
         * are proxied to the real third-party base URL (value).
         * e.g. /payments -> https://sandbox.payments-provider.com
         */
        Map<String, String> proxies
) {
    public WireMockProperties {
        if (maxRequestJournalEntries <= 0) maxRequestJournalEntries = 1000;
        if (proxies == null) proxies = Map.of();
    }
}
