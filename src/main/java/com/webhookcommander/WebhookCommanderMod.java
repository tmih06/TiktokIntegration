package com.webhookcommander;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebhookCommanderMod implements ModInitializer {
    public static final String MOD_ID = "webhook_commander";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static MinecraftServer serverInstance;
    private static WebhookServer webhookServer;

    @Override
    public void onInitialize() {
        LOGGER.info("Webhook Commander initializing...");

        // Load configuration
        ModConfig.load();

        // Register /tiktok command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TiktokCommand.register(dispatcher);
            LOGGER.info("Registered /tiktok command");
        });

        // Register server lifecycle events (only runs on server/integrated server)
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            serverInstance = server;
            startWebhookServer();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            stopWebhookServer();
            serverInstance = null;
        });

        LOGGER.info("Webhook Commander initialized!");
    }

    private void startWebhookServer() {
        try {
            int port = ModConfig.getPort();
            webhookServer = new WebhookServer(port);
            webhookServer.start();
            LOGGER.info("========================================");
            LOGGER.info("Webhook Commander - Server Started!");
            LOGGER.info("  Listening on: http://0.0.0.0:{}", port);
            LOGGER.info("  Execute endpoint: POST /execute");
            LOGGER.info("  Health endpoint: GET /health");
            LOGGER.info("  Use '/tiktok debug true' for verbose logs");
            LOGGER.info("========================================");
        } catch (Exception e) {
            LOGGER.error("Failed to start webhook server", e);
        }
    }

    private void stopWebhookServer() {
        if (webhookServer != null) {
            webhookServer.stop();
            LOGGER.info("Webhook server stopped");
        }
    }

    public static MinecraftServer getServer() {
        return serverInstance;
    }
}
