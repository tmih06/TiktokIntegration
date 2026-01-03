package com.webhookcommander;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final String CONFIG_FILE = "webhook_commander.json";

    private static int port = 8080;
    private static String authToken = "";

    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(CONFIG_FILE);

        if (Files.exists(configFile)) {
            try {
                String content = Files.readString(configFile, StandardCharsets.UTF_8);
                parseConfig(content);
                WebhookCommanderMod.LOGGER.info("Loaded config: port={}, authToken={}",
                        port, authToken.isEmpty() ? "(none)" : "(set)");
            } catch (IOException e) {
                WebhookCommanderMod.LOGGER.error("Failed to load config", e);
            }
        } else {
            // Create default config
            save();
            WebhookCommanderMod.LOGGER.info("Created default config file at {}", configFile);
        }
    }

    public static void save() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(CONFIG_FILE);

        String content = String.format("""
                {
                  "port": %d,
                  "authToken": "%s"
                }
                """, port, authToken);

        try {
            Files.createDirectories(configDir);
            Files.writeString(configFile, content, StandardCharsets.UTF_8);
            WebhookCommanderMod.LOGGER.info("Saved config: port={}, authToken={}",
                    port, authToken.isEmpty() ? "(none)" : "(set)");
        } catch (IOException e) {
            WebhookCommanderMod.LOGGER.error("Failed to save config", e);
        }
    }

    private static void parseConfig(String json) {
        // Simple JSON parsing
        port = parseIntValue(json, "port", 8080);
        authToken = parseStringValue(json, "authToken", "");
    }

    private static int parseIntValue(String json, String key, int defaultValue) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1)
            return defaultValue;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1)
            return defaultValue;

        // Find the number
        int start = colonIndex + 1;
        while (start < json.length() && !Character.isDigit(json.charAt(start)) && json.charAt(start) != '-') {
            start++;
        }

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }

        if (start == end)
            return defaultValue;

        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String parseStringValue(String json, String key, String defaultValue) {
        int keyIndex = json.indexOf("\"" + key + "\"");
        if (keyIndex == -1)
            return defaultValue;

        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1)
            return defaultValue;

        int valueStart = json.indexOf("\"", colonIndex);
        if (valueStart == -1)
            return defaultValue;

        int valueEnd = json.indexOf("\"", valueStart + 1);
        if (valueEnd == -1)
            return defaultValue;

        return json.substring(valueStart + 1, valueEnd);
    }

    // Getters
    public static int getPort() {
        return port;
    }

    public static String getAuthToken() {
        return authToken;
    }

    // Setters (for config screen)
    public static void setPort(int newPort) {
        port = newPort;
    }

    public static void setAuthToken(String newAuthToken) {
        authToken = newAuthToken != null ? newAuthToken : "";
    }
}
