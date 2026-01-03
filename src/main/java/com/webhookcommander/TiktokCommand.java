package com.webhookcommander;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TiktokCommand {

        private static boolean debugEnabled = false;

        public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
                dispatcher.register(
                                CommandManager.literal("tiktok")
                                                .requires(source -> source.hasPermissionLevel(2)) // Require OP level 2
                                                .then(CommandManager.literal("debug")
                                                                .then(CommandManager
                                                                                .argument("enabled",
                                                                                                BoolArgumentType.bool())
                                                                                .executes(context -> {
                                                                                        debugEnabled = BoolArgumentType
                                                                                                        .getBool(context,
                                                                                                                        "enabled");
                                                                                        String status = debugEnabled
                                                                                                        ? "enabled"
                                                                                                        : "disabled";
                                                                                        context.getSource()
                                                                                                        .sendFeedback(
                                                                                                                        () -> Text.literal(
                                                                                                                                        "§a[Webhook Commander] Debug mode "
                                                                                                                                                        + status),
                                                                                                                        true);
                                                                                        WebhookCommanderMod.LOGGER.info(
                                                                                                        "Debug mode {}",
                                                                                                        status);
                                                                                        return 1;
                                                                                }))
                                                                .executes(context -> {
                                                                        String status = debugEnabled ? "enabled"
                                                                                        : "disabled";
                                                                        context.getSource().sendFeedback(
                                                                                        () -> Text.literal(
                                                                                                        "§e[Webhook Commander] Debug mode is currently "
                                                                                                                        + status),
                                                                                        false);
                                                                        return 1;
                                                                }))
                                                .then(CommandManager.literal("status")
                                                                .executes(context -> {
                                                                        int port = ModConfig.getPort();
                                                                        String authStatus = ModConfig.getAuthToken()
                                                                                        .isEmpty() ? "disabled"
                                                                                                        : "enabled";
                                                                        context.getSource().sendFeedback(
                                                                                        () -> Text.literal(
                                                                                                        "§b[Webhook Commander] Status:\n"
                                                                                                                        +
                                                                                                                        "§7  Port: §f"
                                                                                                                        + port
                                                                                                                        + "\n"
                                                                                                                        +
                                                                                                                        "§7  Auth: §f"
                                                                                                                        + authStatus
                                                                                                                        + "\n"
                                                                                                                        +
                                                                                                                        "§7  Debug: §f"
                                                                                                                        + (debugEnabled ? "enabled"
                                                                                                                                        : "disabled")),
                                                                                        false);
                                                                        return 1;
                                                                }))
                                                .then(CommandManager.literal("test")
                                                                .executes(context -> {
                                                                        context.getSource().sendFeedback(
                                                                                        () -> Text.literal(
                                                                                                        "§a[Webhook Commander] Webhook server is running on port "
                                                                                                                        + ModConfig.getPort()),
                                                                                        false);
                                                                        log("Test command executed by " + context
                                                                                        .getSource().getName());
                                                                        return 1;
                                                                })));
        }

        public static boolean isDebugEnabled() {
                return debugEnabled;
        }

        public static void setDebugEnabled(boolean enabled) {
                debugEnabled = enabled;
                WebhookCommanderMod.LOGGER.info("Debug mode {}", enabled ? "enabled" : "disabled");
        }

        public static void log(String message) {
                if (debugEnabled) {
                        WebhookCommanderMod.LOGGER.info("[DEBUG] {}", message);
                }
        }

        public static void log(String format, Object... args) {
                if (debugEnabled) {
                        WebhookCommanderMod.LOGGER.info("[DEBUG] " + format, args);
                }
        }
}
