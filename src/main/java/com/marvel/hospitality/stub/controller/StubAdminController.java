package com.marvel.hospitality.stub.controller;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.marvel.hospitality.stub.config.WireMockProperties;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Convenience endpoints on the Spring Boot port (8080).
 * The full WireMock admin API is still available on the stub port at /__admin.
 */
@RestController
@RequestMapping("/stub-admin")
public class StubAdminController {

    private final WireMockServer server;
    private final WireMockProperties props;

    public StubAdminController(WireMockServer server, WireMockProperties props) {
        this.server = server;
        this.props = props;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("stubPort", server.port());
        info.put("baseUrl", server.baseUrl());
        info.put("mappings", server.getStubMappings().size());
        info.put("requestsServed", server.getAllServeEvents().size());
        info.put("unmatchedRequests", server.findAllUnmatchedRequests().size());
        info.put("proxies", props.proxies());
        return info;
    }

    /** Reload mapping files from disk/classpath (picks up edits without restart). */
    @PostMapping("/reload")
    public Map<String, Object> reload() {
        server.resetToDefaultMappings();
        com.marvel.hospitality.stub.config.WireMockServerConfig.registerProxies(server, props);
        return Map.of("status", "reloaded", "mappings", server.getStubMappings().size());
    }

    /** Clear request journal and reset scenarios to "Started". */
    @PostMapping("/reset")
    public Map<String, String> reset() {
        server.resetRequests();
        server.resetScenarios();
        return Map.of("status", "requests and scenarios reset");
    }

    /** Requests that hit no stub — the quickest way to see what still needs stubbing. */
    @GetMapping("/unmatched")
    public List<Map<String, String>> unmatched() {
        return server.findAllUnmatchedRequests().stream()
                .map(StubAdminController::summary)
                .toList();
    }

    @GetMapping("/requests")
    public List<Map<String, String>> recent(@RequestParam(defaultValue = "20") int limit) {
        return server.getAllServeEvents().stream()
                .limit(limit)
                .map(ServeEvent::getRequest)
                .map(StubAdminController::summary)
                .toList();
    }

    private static Map<String, String> summary(LoggedRequest r) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("method", r.getMethod().getName());
        m.put("url", r.getUrl());
        m.put("loggedAt", String.valueOf(r.getLoggedDate()));
        m.put("body", r.getBodyAsString());
        return m;
    }
}
