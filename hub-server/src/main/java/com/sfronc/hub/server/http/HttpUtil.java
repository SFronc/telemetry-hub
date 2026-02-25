package com.sfronc.hub.server.http;

import com.sfronc.hub.common.Json;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

final class HttpUtil {
    private HttpUtil() {}

    static byte[] readBody(HttpExchange ex, int maxBytes) throws IOException {
        byte[] b = ex.getRequestBody().readAllBytes();
        if (b.length > maxBytes) throw new IllegalArgumentException("Body too large");
        return b;
    }

    static void writeJson(HttpExchange ex, int code, Object body) throws IOException {
        byte[] bytes = Json.toBytes(body);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    static void writeError(HttpExchange ex, int code, String msg) throws IOException {
        writeJson(ex, code, Map.of("error", msg));
    }

    static String bearerToken(HttpExchange ex) {
        String h = ex.getRequestHeaders().getFirst("Authorization");
        if (h == null) return null;
        String p = "Bearer ";
        if (!h.startsWith(p)) return null;
        return h.substring(p.length()).trim();
    }

    static String decode(String s) {
        return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
