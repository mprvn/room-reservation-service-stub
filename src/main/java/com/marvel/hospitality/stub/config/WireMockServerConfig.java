package com.marvel.hospitality.stub.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Configuration
public class WireMockServerConfig {

    private static final Logger log = LoggerFactory.getLogger(WireMockServerConfig.class);

    /** Proxy fall-throughs sit below every file-based stub (default priority 5). */
    private static final int PROXY_PRIORITY = 10;

    @Bean(destroyMethod = "stop")
    public WireMockServer wireMockServer(WireMockProperties props) {
        WireMockConfiguration options = WireMockConfiguration.options()
                .port(props.port())
                .globalTemplating(props.globalTemplating())
                .maxRequestJournalEntries(props.maxRequestJournalEntries())
                .notifier(new Slf4jNotifier(props.verbose()));

        if (props.httpsPort() > 0) {
            options.httpsPort(props.httpsPort());
        }

        if (StringUtils.hasText(props.rootDir())) {
            options.usingFilesUnderDirectory(props.rootDir());
            log.info("WireMock loading stubs from directory: {}", props.rootDir());
        } else {
            options.usingFilesUnderClasspath("wiremock");
            log.info("WireMock loading stubs from classpath:wiremock");
        }

        WireMockServer server = new WireMockServer(options);
        server.start();
        registerProxies(server, props);

        log.info("WireMock started on http port {}{} with {} stub mappings",
                server.port(),
                props.httpsPort() > 0 ? " / https port " + server.httpsPort() : "",
                server.getStubMappings().size());
        return server;
    }

    /** Re-applied after a reset/reload, since programmatic stubs are not on disk. */
    public static void registerProxies(WireMockServer server, WireMockProperties props) {
        props.proxies().forEach((prefix, target) -> {
            server.stubFor(any(urlPathMatching(prefix + ".*"))
                    .atPriority(PROXY_PRIORITY)
                    .willReturn(aResponse().proxiedFrom(target)));
            log.info("Unmatched {}** will be proxied to {}", prefix, target);
        });
    }
}
