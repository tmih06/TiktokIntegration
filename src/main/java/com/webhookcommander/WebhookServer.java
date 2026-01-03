package com.webhookcommander;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class WebhookServer {
    private final int port;
    private HttpServer server;

    public WebhookServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/execute", new CommandHandler());
        server.createContext("/health", new HealthHandler());
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        TiktokCommand.log("HTTP server started on 0.0.0.0:{}", port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            TiktokCommand.log("HTTP server stopped");
        }
    }

    private static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            TiktokCommand.log("Health check request from {}", exchange.getRemoteAddress());

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String response = "{\"status\":\"ok\",\"mod\":\"webhook_commander\",\"port\":" + ModConfig.getPort() + "}";
            sendResponse(exchange, 200, response);
        }
    }

    private static class CommandHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String remoteAddr = exchange.getRemoteAddress().toString();
            TiktokCommand.log("Incoming request from {} - Method: {}", remoteAddr, exchange.getRequestMethod());

            // Set CORS headers
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            // Handle preflight
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                TiktokCommand.log("CORS preflight request handled");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Only allow POST
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                TiktokCommand.log("Rejected non-POST request: {}", exchange.getRequestMethod());
                sendResponse(exchange, 405, "{\"success\":false,\"message\":\"Method not allowed. Use POST.\"}");
                return;
            }

            // Check authentication if configured
            String authToken = ModConfig.getAuthToken();
            if (authToken != null && !authToken.isEmpty()) {
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                TiktokCommand.log("Auth check - Expected: Bearer {}, Got: {}",
                        authToken.substring(0, Math.min(4, authToken.length())) + "...",
                        authHeader != null ? authHeader.substring(0, Math.min(15, authHeader.length())) + "..."
                                : "null");
                if (authHeader == null || !authHeader.equals("Bearer " + authToken)) {
                    WebhookCommanderMod.LOGGER.warn("Unauthorized request from {}", remoteAddr);
                    sendResponse(exchange, 401, "{\"success\":false,\"message\":\"Unauthorized\"}");
                    return;
                }
            }

            // Read request body
            String requestBody;
            try (InputStream is = exchange.getRequestBody();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                requestBody = sb.toString();
            }

            TiktokCommand.log("Request body: {}", requestBody);

            // Parse command from JSON (simple parsing without external library)
            String command = parseCommandFromJson(requestBody);
            if (command == null || command.isEmpty()) {
                TiktokCommand.log("Failed to parse command from JSON");
                sendResponse(exchange, 400,
                        "{\"success\":false,\"message\":\"Missing 'command' field in request body\"}");
                return;
            }

            WebhookCommanderMod.LOGGER.info("Received command via webhook: {}", command);
            TiktokCommand.log("Parsed command: {}", command);

            // Execute command
            try {
                CommandExecutor.CommandResult result = CommandExecutor.execute(command);
                TiktokCommand.log("Command result: success={}, message={}, code={}",
                        result.success(), result.message(), result.resultCode());

                String response = String.format(
                        "{\"success\":%s,\"message\":\"%s\",\"result\":%d}",
                        result.success(),
                        escapeJson(result.message()),
                        result.resultCode());
                sendResponse(exchange, result.success() ? 200 : 400, response);
            } catch (Exception e) {
                WebhookCommanderMod.LOGGER.error("Error executing command: {}", command, e);
                sendResponse(exchange, 500, "{\"success\":false,\"message\":\"Internal server error: "
                        + escapeJson(e.getMessage()) + "\"}");
            }
        }

        private String parseCommandFromJson(String json) {
            // Simple JSON parsing for {"command": "value"}
            // Looking for: "command" : "value" or "command": "value"
            int commandKeyIndex = json.indexOf("\"command\"");
            if (commandKeyIndex == -1) {
                return null;
            }

            int colonIndex = json.indexOf(":", commandKeyIndex);
            if (colonIndex == -1) {
                return null;
            }

            int valueStart = json.indexOf("\"", colonIndex);
            if (valueStart == -1) {
                return null;
            }

            int valueEnd = findClosingQuote(json, valueStart + 1);
            if (valueEnd == -1) {
                return null;
            }

            return unescapeJson(json.substring(valueStart + 1, valueEnd));
        }

        private int findClosingQuote(String json, int start) {
            for (int i = start; i < json.length(); i++) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    i++; // Skip escaped character
                } else if (c == '"') {
                    return i;
                }
            }
            return -1;
        }

        private String unescapeJson(String value) {
            return value
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
        }

        private String escapeJson(String value) {
            if (value == null)
                return "";
            return value
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        TiktokCommand.log("Sending response: status={}, body={}", statusCode, response);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
